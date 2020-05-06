package com.android.server.notification;

import android.app.Person;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.LruCache;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ValidateNotificationPeople implements NotificationSignalExtractor {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final boolean ENABLE_PEOPLE_VALIDATOR = true;
    private static final String[] LOOKUP_PROJECTION = {"_id", "starred"};
    private static final int MAX_PEOPLE = 10;
    static final float NONE = 0.0f;
    private static final int PEOPLE_CACHE_SIZE = 200;
    private static final String SETTING_ENABLE_PEOPLE_VALIDATOR = "validate_notification_people_enabled";
    static final float STARRED_CONTACT = 1.0f;
    private static final String TAG = "ValidateNoPeople";
    static final float VALID_CONTACT = 0.5f;
    /* access modifiers changed from: private */
    public static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private Context mBaseContext;
    protected boolean mEnabled;
    /* access modifiers changed from: private */
    public int mEvictionCount;
    private Handler mHandler;
    private ContentObserver mObserver;
    /* access modifiers changed from: private */
    public LruCache<String, LookupResult> mPeopleCache;
    /* access modifiers changed from: private */
    public NotificationUsageStats mUsageStats;
    private Map<Integer, Context> mUserToContextMap;

    static /* synthetic */ int access$108(ValidateNotificationPeople x0) {
        int i = x0.mEvictionCount;
        x0.mEvictionCount = i + 1;
        return i;
    }

    public void initialize(Context context, NotificationUsageStats usageStats) {
        if (DEBUG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
        this.mUserToContextMap = new ArrayMap();
        this.mBaseContext = context;
        this.mUsageStats = usageStats;
        this.mPeopleCache = new LruCache<>(200);
        this.mEnabled = 1 == Settings.Global.getInt(this.mBaseContext.getContentResolver(), SETTING_ENABLE_PEOPLE_VALIDATOR, 1);
        if (this.mEnabled) {
            this.mHandler = new Handler();
            this.mObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean selfChange, Uri uri, int userId) {
                    super.onChange(selfChange, uri, userId);
                    if ((ValidateNotificationPeople.DEBUG || ValidateNotificationPeople.this.mEvictionCount % 100 == 0) && ValidateNotificationPeople.VERBOSE) {
                        Slog.i(ValidateNotificationPeople.TAG, "mEvictionCount: " + ValidateNotificationPeople.this.mEvictionCount);
                    }
                    ValidateNotificationPeople.this.mPeopleCache.evictAll();
                    ValidateNotificationPeople.access$108(ValidateNotificationPeople.this);
                }
            };
            this.mBaseContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, this.mObserver, -1);
        }
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (!this.mEnabled) {
            if (VERBOSE) {
                Slog.i(TAG, "disabled");
            }
            return null;
        } else if (record == null || record.getNotification() == null) {
            if (VERBOSE) {
                Slog.i(TAG, "skipping empty notification");
            }
            return null;
        } else if (record.getUserId() == -1) {
            if (VERBOSE) {
                Slog.i(TAG, "skipping global notification");
            }
            return null;
        } else {
            Context context = getContextAsUser(record.getUser());
            if (context != null) {
                return validatePeople(context, record);
            }
            if (VERBOSE) {
                Slog.i(TAG, "skipping notification that lacks a context");
            }
            return null;
        }
    }

    public void setConfig(RankingConfig config) {
    }

    public void setZenHelper(ZenModeHelper helper) {
    }

    public float getContactAffinity(UserHandle userHandle, Bundle extras, int timeoutMs, float timeoutAffinity) {
        if (DEBUG) {
            Slog.d(TAG, "checking affinity for " + userHandle);
        }
        if (extras == null) {
            return NONE;
        }
        String key = Long.toString(System.nanoTime());
        float[] affinityOut = new float[1];
        Context context = getContextAsUser(userHandle);
        if (context == null) {
            return NONE;
        }
        final PeopleRankingReconsideration prr = validatePeople(context, key, extras, (List<String>) null, affinityOut);
        float affinity = affinityOut[0];
        if (prr == null) {
            return affinity;
        }
        final Semaphore s = new Semaphore(0);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                prr.work();
                s.release();
            }
        });
        try {
            if (s.tryAcquire((long) timeoutMs, TimeUnit.MILLISECONDS)) {
                return Math.max(prr.getContactAffinity(), affinity);
            }
            Slog.w(TAG, "Timeout while waiting for affinity: " + key + ". Returning timeoutAffinity=" + timeoutAffinity);
            return timeoutAffinity;
        } catch (InterruptedException e) {
            Slog.w(TAG, "InterruptedException while waiting for affinity: " + key + ". Returning affinity=" + affinity, e);
            return affinity;
        }
    }

    private Context getContextAsUser(UserHandle userHandle) {
        Context context = this.mUserToContextMap.get(Integer.valueOf(userHandle.getIdentifier()));
        if (context != null) {
            return context;
        }
        try {
            context = this.mBaseContext.createPackageContextAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, userHandle);
            this.mUserToContextMap.put(Integer.valueOf(userHandle.getIdentifier()), context);
            return context;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "failed to create package context for lookups", e);
            return context;
        }
    }

    private RankingReconsideration validatePeople(Context context, NotificationRecord record) {
        float[] affinityOut = new float[1];
        PeopleRankingReconsideration rr = validatePeople(context, record.getKey(), record.getNotification().extras, record.getPeopleOverride(), affinityOut);
        boolean z = false;
        float affinity = affinityOut[0];
        record.setContactAffinity(affinity);
        if (rr == null) {
            NotificationUsageStats notificationUsageStats = this.mUsageStats;
            boolean z2 = affinity > NONE;
            if (affinity == 1.0f) {
                z = true;
            }
            notificationUsageStats.registerPeopleAffinity(record, z2, z, true);
        } else {
            rr.setRecord(record);
        }
        return rr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0093  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.android.server.notification.ValidateNotificationPeople.PeopleRankingReconsideration validatePeople(android.content.Context r17, java.lang.String r18, android.os.Bundle r19, java.util.List<java.lang.String> r20, float[] r21) {
        /*
            r16 = this;
            r7 = r16
            r8 = r18
            r0 = 0
            r1 = 0
            if (r19 != 0) goto L_0x0009
            return r1
        L_0x0009:
            android.util.ArraySet r2 = new android.util.ArraySet
            r9 = r20
            r2.<init>(r9)
            r10 = r2
            java.lang.String[] r11 = getExtraPeople(r19)
            if (r11 == 0) goto L_0x001e
            java.util.List r2 = java.util.Arrays.asList(r11)
            r10.addAll(r2)
        L_0x001e:
            boolean r2 = VERBOSE
            if (r2 == 0) goto L_0x0044
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Validating: "
            r2.append(r3)
            r2.append(r8)
            java.lang.String r3 = " for "
            r2.append(r3)
            int r3 = r17.getUserId()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "ValidateNoPeople"
            android.util.Slog.i(r3, r2)
        L_0x0044:
            java.util.LinkedList r2 = new java.util.LinkedList
            r2.<init>()
            r12 = r2
            r2 = 0
            java.util.Iterator r3 = r10.iterator()
            r4 = r2
            r2 = r0
        L_0x0051:
            boolean r0 = r3.hasNext()
            if (r0 == 0) goto L_0x00aa
            java.lang.Object r0 = r3.next()
            r5 = r0
            java.lang.String r5 = (java.lang.String) r5
            boolean r0 = android.text.TextUtils.isEmpty(r5)
            if (r0 == 0) goto L_0x0065
            goto L_0x0051
        L_0x0065:
            android.util.LruCache<java.lang.String, com.android.server.notification.ValidateNotificationPeople$LookupResult> r6 = r7.mPeopleCache
            monitor-enter(r6)
            int r0 = r17.getUserId()     // Catch:{ all -> 0x00a7 }
            java.lang.String r0 = r7.getCacheKey(r0, r5)     // Catch:{ all -> 0x00a7 }
            android.util.LruCache<java.lang.String, com.android.server.notification.ValidateNotificationPeople$LookupResult> r13 = r7.mPeopleCache     // Catch:{ all -> 0x00a7 }
            java.lang.Object r13 = r13.get(r0)     // Catch:{ all -> 0x00a7 }
            com.android.server.notification.ValidateNotificationPeople$LookupResult r13 = (com.android.server.notification.ValidateNotificationPeople.LookupResult) r13     // Catch:{ all -> 0x00a7 }
            if (r13 == 0) goto L_0x008e
            boolean r14 = r13.isExpired()     // Catch:{ all -> 0x00a7 }
            if (r14 == 0) goto L_0x0081
            goto L_0x008e
        L_0x0081:
            boolean r14 = DEBUG     // Catch:{ all -> 0x00a7 }
            if (r14 == 0) goto L_0x0091
            java.lang.String r14 = "ValidateNoPeople"
            java.lang.String r15 = "using cached lookupResult"
            android.util.Slog.d(r14, r15)     // Catch:{ all -> 0x00a7 }
            goto L_0x0091
        L_0x008e:
            r12.add(r5)     // Catch:{ all -> 0x00a7 }
        L_0x0091:
            if (r13 == 0) goto L_0x009c
            float r14 = r13.getAffinity()     // Catch:{ all -> 0x00a7 }
            float r14 = java.lang.Math.max(r2, r14)     // Catch:{ all -> 0x00a7 }
            r2 = r14
        L_0x009c:
            monitor-exit(r6)     // Catch:{ all -> 0x00a7 }
            int r4 = r4 + 1
            r0 = 10
            if (r4 != r0) goto L_0x00a6
            r0 = r2
            r13 = r4
            goto L_0x00ac
        L_0x00a6:
            goto L_0x0051
        L_0x00a7:
            r0 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x00a7 }
            throw r0
        L_0x00aa:
            r0 = r2
            r13 = r4
        L_0x00ac:
            r2 = 0
            r21[r2] = r0
            boolean r2 = r12.isEmpty()
            if (r2 == 0) goto L_0x00d0
            boolean r2 = VERBOSE
            if (r2 == 0) goto L_0x00cf
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "final affinity: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "ValidateNoPeople"
            android.util.Slog.i(r3, r2)
        L_0x00cf:
            return r1
        L_0x00d0:
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x00ea
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Pending: future work scheduled for: "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "ValidateNoPeople"
            android.util.Slog.d(r2, r1)
        L_0x00ea:
            com.android.server.notification.ValidateNotificationPeople$PeopleRankingReconsideration r14 = new com.android.server.notification.ValidateNotificationPeople$PeopleRankingReconsideration
            r6 = 0
            r1 = r14
            r2 = r16
            r3 = r17
            r4 = r18
            r5 = r12
            r1.<init>(r3, r4, r5)
            return r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ValidateNotificationPeople.validatePeople(android.content.Context, java.lang.String, android.os.Bundle, java.util.List, float[]):com.android.server.notification.ValidateNotificationPeople$PeopleRankingReconsideration");
    }

    /* access modifiers changed from: private */
    public String getCacheKey(int userId, String handle) {
        return Integer.toString(userId) + ":" + handle;
    }

    public static String[] getExtraPeople(Bundle extras) {
        return combineLists(getExtraPeopleForKey(extras, "android.people"), getExtraPeopleForKey(extras, "android.people.list"));
    }

    private static String[] combineLists(String[] first, String[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        ArraySet<String> people = new ArraySet<>(first.length + second.length);
        for (String person : first) {
            people.add(person);
        }
        for (String person2 : second) {
            people.add(person2);
        }
        return (String[]) people.toArray();
    }

    private static String[] getExtraPeopleForKey(Bundle extras, String key) {
        Object people = extras.get(key);
        if (people instanceof String[]) {
            return (String[]) people;
        }
        if (people instanceof ArrayList) {
            ArrayList<String> arrayList = (ArrayList) people;
            if (arrayList.isEmpty()) {
                return null;
            }
            if (arrayList.get(0) instanceof String) {
                ArrayList<String> stringArray = arrayList;
                return (String[]) stringArray.toArray(new String[stringArray.size()]);
            } else if (arrayList.get(0) instanceof CharSequence) {
                ArrayList<String> charSeqList = arrayList;
                int N = charSeqList.size();
                String[] array = new String[N];
                for (int i = 0; i < N; i++) {
                    array[i] = charSeqList.get(i).toString();
                }
                return array;
            } else if (!(arrayList.get(0) instanceof Person)) {
                return null;
            } else {
                ArrayList<String> list = arrayList;
                int N2 = list.size();
                String[] array2 = new String[N2];
                for (int i2 = 0; i2 < N2; i2++) {
                    array2[i2] = ((Person) list.get(i2)).resolveToLegacyUri();
                }
                return array2;
            }
        } else if (people instanceof String) {
            return new String[]{(String) people};
        } else if (people instanceof char[]) {
            return new String[]{new String((char[]) people)};
        } else if (people instanceof CharSequence) {
            return new String[]{((CharSequence) people).toString()};
        } else if (!(people instanceof CharSequence[])) {
            return null;
        } else {
            CharSequence[] charSeqArray = (CharSequence[]) people;
            int N3 = charSeqArray.length;
            String[] array3 = new String[N3];
            for (int i3 = 0; i3 < N3; i3++) {
                array3[i3] = charSeqArray[i3].toString();
            }
            return array3;
        }
    }

    /* access modifiers changed from: private */
    public LookupResult resolvePhoneContact(Context context, String number) {
        return searchContacts(context, Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)));
    }

    /* access modifiers changed from: private */
    public LookupResult resolveEmailContact(Context context, String email) {
        return searchContacts(context, Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(email)));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
        if (r2 == null) goto L_0x003e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.server.notification.ValidateNotificationPeople.LookupResult searchContacts(android.content.Context r10, android.net.Uri r11) {
        /*
            r9 = this;
            java.lang.String r0 = "ValidateNoPeople"
            com.android.server.notification.ValidateNotificationPeople$LookupResult r1 = new com.android.server.notification.ValidateNotificationPeople$LookupResult
            r1.<init>()
            r2 = 0
            android.content.ContentResolver r3 = r10.getContentResolver()     // Catch:{ all -> 0x0034 }
            java.lang.String[] r5 = LOOKUP_PROJECTION     // Catch:{ all -> 0x0034 }
            r6 = 0
            r7 = 0
            r8 = 0
            r4 = r11
            android.database.Cursor r3 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0034 }
            r2 = r3
            if (r2 != 0) goto L_0x0025
            java.lang.String r3 = "Null cursor from contacts query."
            android.util.Slog.w(r0, r3)     // Catch:{ all -> 0x0034 }
            if (r2 == 0) goto L_0x0024
            r2.close()
        L_0x0024:
            return r1
        L_0x0025:
            boolean r3 = r2.moveToNext()     // Catch:{ all -> 0x0034 }
            if (r3 == 0) goto L_0x002f
            r1.mergeContact(r2)     // Catch:{ all -> 0x0034 }
            goto L_0x0025
        L_0x002f:
        L_0x0030:
            r2.close()
            goto L_0x003e
        L_0x0034:
            r3 = move-exception
            java.lang.String r4 = "Problem getting content resolver or performing contacts query."
            android.util.Slog.w(r0, r4, r3)     // Catch:{ all -> 0x003f }
            if (r2 == 0) goto L_0x003e
            goto L_0x0030
        L_0x003e:
            return r1
        L_0x003f:
            r0 = move-exception
            if (r2 == 0) goto L_0x0045
            r2.close()
        L_0x0045:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ValidateNotificationPeople.searchContacts(android.content.Context, android.net.Uri):com.android.server.notification.ValidateNotificationPeople$LookupResult");
    }

    private static class LookupResult {
        private static final long CONTACT_REFRESH_MILLIS = 3600000;
        private float mAffinity = ValidateNotificationPeople.NONE;
        private final long mExpireMillis = (System.currentTimeMillis() + 3600000);

        public void mergeContact(Cursor cursor) {
            this.mAffinity = Math.max(this.mAffinity, 0.5f);
            int idIdx = cursor.getColumnIndex("_id");
            if (idIdx >= 0) {
                int id = cursor.getInt(idIdx);
                if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "contact _ID is: " + id);
                }
            } else {
                Slog.i(ValidateNotificationPeople.TAG, "invalid cursor: no _ID");
            }
            int starIdx = cursor.getColumnIndex("starred");
            if (starIdx >= 0) {
                boolean isStarred = cursor.getInt(starIdx) != 0;
                if (isStarred) {
                    this.mAffinity = Math.max(this.mAffinity, 1.0f);
                }
                if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "contact STARRED is: " + isStarred);
                }
            } else if (ValidateNotificationPeople.DEBUG) {
                Slog.d(ValidateNotificationPeople.TAG, "invalid cursor: no STARRED");
            }
        }

        /* access modifiers changed from: private */
        public boolean isExpired() {
            return this.mExpireMillis < System.currentTimeMillis();
        }

        private boolean isInvalid() {
            return this.mAffinity == ValidateNotificationPeople.NONE || isExpired();
        }

        public float getAffinity() {
            if (isInvalid()) {
                return ValidateNotificationPeople.NONE;
            }
            return this.mAffinity;
        }
    }

    private class PeopleRankingReconsideration extends RankingReconsideration {
        private static final long LOOKUP_TIME = 1000;
        private float mContactAffinity;
        private final Context mContext;
        private final LinkedList<String> mPendingLookups;
        private NotificationRecord mRecord;

        private PeopleRankingReconsideration(Context context, String key, LinkedList<String> pendingLookups) {
            super(key, 1000);
            this.mContactAffinity = ValidateNotificationPeople.NONE;
            this.mContext = context;
            this.mPendingLookups = pendingLookups;
        }

        public void work() {
            LookupResult lookupResult;
            if (ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "Executing: validation for: " + this.mKey);
            }
            long timeStartMs = System.currentTimeMillis();
            Iterator it = this.mPendingLookups.iterator();
            while (it.hasNext()) {
                String handle = (String) it.next();
                Uri uri = Uri.parse(handle);
                if ("tel".equals(uri.getScheme())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking telephone URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.resolvePhoneContact(this.mContext, uri.getSchemeSpecificPart());
                } else if ("mailto".equals(uri.getScheme())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking mailto URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.resolveEmailContact(this.mContext, uri.getSchemeSpecificPart());
                } else if (handle.startsWith(ContactsContract.Contacts.CONTENT_LOOKUP_URI.toString())) {
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "checking lookup URI: " + handle);
                    }
                    lookupResult = ValidateNotificationPeople.this.searchContacts(this.mContext, uri);
                } else {
                    lookupResult = new LookupResult();
                    if (!com.android.server.pm.Settings.ATTR_NAME.equals(uri.getScheme())) {
                        Slog.w(ValidateNotificationPeople.TAG, "unsupported URI " + handle);
                    }
                }
                if (lookupResult != null) {
                    synchronized (ValidateNotificationPeople.this.mPeopleCache) {
                        ValidateNotificationPeople.this.mPeopleCache.put(ValidateNotificationPeople.this.getCacheKey(this.mContext.getUserId(), handle), lookupResult);
                    }
                    if (ValidateNotificationPeople.DEBUG) {
                        Slog.d(ValidateNotificationPeople.TAG, "lookup contactAffinity is " + lookupResult.getAffinity());
                    }
                    this.mContactAffinity = Math.max(this.mContactAffinity, lookupResult.getAffinity());
                } else if (ValidateNotificationPeople.DEBUG) {
                    Slog.d(ValidateNotificationPeople.TAG, "lookupResult is null");
                }
            }
            if (ValidateNotificationPeople.DEBUG) {
                Slog.d(ValidateNotificationPeople.TAG, "Validation finished in " + (System.currentTimeMillis() - timeStartMs) + "ms");
            }
            if (this.mRecord != null) {
                NotificationUsageStats access$1000 = ValidateNotificationPeople.this.mUsageStats;
                NotificationRecord notificationRecord = this.mRecord;
                boolean z = true;
                boolean z2 = this.mContactAffinity > ValidateNotificationPeople.NONE;
                if (this.mContactAffinity != 1.0f) {
                    z = false;
                }
                access$1000.registerPeopleAffinity(notificationRecord, z2, z, false);
            }
        }

        public void applyChangesLocked(NotificationRecord operand) {
            operand.setContactAffinity(Math.max(this.mContactAffinity, operand.getContactAffinity()));
            if (ValidateNotificationPeople.VERBOSE) {
                Slog.i(ValidateNotificationPeople.TAG, "final affinity: " + operand.getContactAffinity());
            }
        }

        public float getContactAffinity() {
            return this.mContactAffinity;
        }

        public void setRecord(NotificationRecord record) {
            this.mRecord = record;
        }
    }
}