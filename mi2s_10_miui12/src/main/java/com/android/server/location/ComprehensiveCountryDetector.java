package com.android.server.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.Geocoder;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComprehensiveCountryDetector extends CountryDetectorBase {
    static final boolean DEBUG = false;
    private static final long LOCATION_REFRESH_INTERVAL = 86400000;
    private static final int MAX_LENGTH_DEBUG_LOGS = 20;
    private static final String TAG = "CountryDetector";
    private int mCountServiceStateChanges;
    private Country mCountry;
    /* access modifiers changed from: private */
    public Country mCountryFromLocation;
    private final ConcurrentLinkedQueue<Country> mDebugLogs = new ConcurrentLinkedQueue<>();
    private Country mLastCountryAddedToLogs;
    private CountryListener mLocationBasedCountryDetectionListener = new CountryListener() {
        public void onCountryDetected(Country country) {
            Country unused = ComprehensiveCountryDetector.this.mCountryFromLocation = country;
            Country unused2 = ComprehensiveCountryDetector.this.detectCountry(true, false);
            ComprehensiveCountryDetector.this.stopLocationBasedDetector();
        }
    };
    protected CountryDetectorBase mLocationBasedCountryDetector;
    protected Timer mLocationRefreshTimer;
    private final Object mObject = new Object();
    private PhoneStateListener mPhoneStateListener;
    private long mStartTime;
    private long mStopTime;
    private boolean mStopped = false;
    private final TelephonyManager mTelephonyManager;
    private int mTotalCountServiceStateChanges;
    private long mTotalTime;

    static /* synthetic */ int access$308(ComprehensiveCountryDetector x0) {
        int i = x0.mCountServiceStateChanges;
        x0.mCountServiceStateChanges = i + 1;
        return i;
    }

    static /* synthetic */ int access$408(ComprehensiveCountryDetector x0) {
        int i = x0.mTotalCountServiceStateChanges;
        x0.mTotalCountServiceStateChanges = i + 1;
        return i;
    }

    public ComprehensiveCountryDetector(Context context) {
        super(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public Country detectCountry() {
        return detectCountry(false, !this.mStopped);
    }

    public void stop() {
        Slog.i(TAG, "Stop the detector.");
        cancelLocationRefresh();
        removePhoneStateListener();
        stopLocationBasedDetector();
        this.mListener = null;
        this.mStopped = true;
    }

    private Country getCountry() {
        Country result = getNetworkBasedCountry();
        if (result == null) {
            result = getLastKnownLocationBasedCountry();
        }
        if (result == null) {
            result = getSimBasedCountry();
        }
        if (result == null) {
            result = getLocaleCountry();
        }
        addToLogs(result);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        if (r2.mDebugLogs.size() < 20) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r2.mDebugLogs.poll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        r2.mDebugLogs.add(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002b, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addToLogs(android.location.Country r3) {
        /*
            r2 = this;
            if (r3 != 0) goto L_0x0003
            return
        L_0x0003:
            java.lang.Object r0 = r2.mObject
            monitor-enter(r0)
            android.location.Country r1 = r2.mLastCountryAddedToLogs     // Catch:{ all -> 0x002c }
            if (r1 == 0) goto L_0x0014
            android.location.Country r1 = r2.mLastCountryAddedToLogs     // Catch:{ all -> 0x002c }
            boolean r1 = r1.equals(r3)     // Catch:{ all -> 0x002c }
            if (r1 == 0) goto L_0x0014
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            return
        L_0x0014:
            r2.mLastCountryAddedToLogs = r3     // Catch:{ all -> 0x002c }
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            java.util.concurrent.ConcurrentLinkedQueue<android.location.Country> r0 = r2.mDebugLogs
            int r0 = r0.size()
            r1 = 20
            if (r0 < r1) goto L_0x0026
            java.util.concurrent.ConcurrentLinkedQueue<android.location.Country> r0 = r2.mDebugLogs
            r0.poll()
        L_0x0026:
            java.util.concurrent.ConcurrentLinkedQueue<android.location.Country> r0 = r2.mDebugLogs
            r0.add(r3)
            return
        L_0x002c:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.ComprehensiveCountryDetector.addToLogs(android.location.Country):void");
    }

    /* access modifiers changed from: private */
    public boolean isNetworkCountryCodeAvailable() {
        return this.mTelephonyManager.getPhoneType() == 1;
    }

    /* access modifiers changed from: protected */
    public Country getNetworkBasedCountry() {
        if (!isNetworkCountryCodeAvailable()) {
            return null;
        }
        String countryIso = this.mTelephonyManager.getNetworkCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, 0);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLastKnownLocationBasedCountry() {
        return this.mCountryFromLocation;
    }

    /* access modifiers changed from: protected */
    public Country getSimBasedCountry() {
        String countryIso = this.mTelephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, 2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLocaleCountry() {
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != null) {
            return new Country(defaultLocale.getCountry(), 3);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public Country detectCountry(boolean notifyChange, boolean startLocationBasedDetection) {
        Country country = getCountry();
        Country country2 = this.mCountry;
        if (country2 != null) {
            country2 = new Country(country2);
        }
        runAfterDetectionAsync(country2, country, notifyChange, startLocationBasedDetection);
        this.mCountry = country;
        return this.mCountry;
    }

    /* access modifiers changed from: protected */
    public void runAfterDetectionAsync(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        final Country country2 = country;
        final Country country3 = detectedCountry;
        final boolean z = notifyChange;
        final boolean z2 = startLocationBasedDetection;
        this.mHandler.post(new Runnable() {
            public void run() {
                ComprehensiveCountryDetector.this.runAfterDetection(country2, country3, z, z2);
            }
        });
    }

    public void setCountryListener(CountryListener listener) {
        CountryListener prevListener = this.mListener;
        this.mListener = listener;
        if (this.mListener == null) {
            removePhoneStateListener();
            stopLocationBasedDetector();
            cancelLocationRefresh();
            this.mStopTime = SystemClock.elapsedRealtime();
            this.mTotalTime += this.mStopTime;
        } else if (prevListener == null) {
            addPhoneStateListener();
            detectCountry(false, true);
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mStopTime = 0;
            this.mCountServiceStateChanges = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void runAfterDetection(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        if (notifyChange) {
            notifyIfCountryChanged(country, detectedCountry);
        }
        if (startLocationBasedDetection && ((detectedCountry == null || detectedCountry.getSource() > 1) && isAirplaneModeOff() && this.mListener != null && isGeoCoderImplemented())) {
            startLocationBasedDetector(this.mLocationBasedCountryDetectionListener);
        }
        if (detectedCountry == null || detectedCountry.getSource() >= 1) {
            scheduleLocationRefresh();
            return;
        }
        cancelLocationRefresh();
        stopLocationBasedDetector();
    }

    private synchronized void startLocationBasedDetector(CountryListener listener) {
        if (this.mLocationBasedCountryDetector == null) {
            this.mLocationBasedCountryDetector = createLocationBasedCountryDetector();
            this.mLocationBasedCountryDetector.setCountryListener(listener);
            this.mLocationBasedCountryDetector.detectCountry();
        }
    }

    /* access modifiers changed from: private */
    public synchronized void stopLocationBasedDetector() {
        if (this.mLocationBasedCountryDetector != null) {
            this.mLocationBasedCountryDetector.stop();
            this.mLocationBasedCountryDetector = null;
        }
    }

    /* access modifiers changed from: protected */
    public CountryDetectorBase createLocationBasedCountryDetector() {
        return new LocationBasedCountryDetector(this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean isAirplaneModeOff() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0;
    }

    private void notifyIfCountryChanged(Country country, Country detectedCountry) {
        if (detectedCountry != null && this.mListener != null) {
            if (country == null || !country.equals(detectedCountry)) {
                notifyListener(detectedCountry);
            }
        }
    }

    private synchronized void scheduleLocationRefresh() {
        if (this.mLocationRefreshTimer == null) {
            this.mLocationRefreshTimer = new Timer();
            this.mLocationRefreshTimer.schedule(new TimerTask() {
                public void run() {
                    ComprehensiveCountryDetector comprehensiveCountryDetector = ComprehensiveCountryDetector.this;
                    comprehensiveCountryDetector.mLocationRefreshTimer = null;
                    Country unused = comprehensiveCountryDetector.detectCountry(false, true);
                }
            }, 86400000);
        }
    }

    private synchronized void cancelLocationRefresh() {
        if (this.mLocationRefreshTimer != null) {
            this.mLocationRefreshTimer.cancel();
            this.mLocationRefreshTimer = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void addPhoneStateListener() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState serviceState) {
                    ComprehensiveCountryDetector.access$308(ComprehensiveCountryDetector.this);
                    ComprehensiveCountryDetector.access$408(ComprehensiveCountryDetector.this);
                    if (ComprehensiveCountryDetector.this.isNetworkCountryCodeAvailable()) {
                        Country unused = ComprehensiveCountryDetector.this.detectCountry(true, true);
                    }
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void removePhoneStateListener() {
        if (this.mPhoneStateListener != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mPhoneStateListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isGeoCoderImplemented() {
        return Geocoder.isPresent();
    }

    public String toString() {
        long currentTime = SystemClock.elapsedRealtime();
        long currentSessionLength = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("ComprehensiveCountryDetector{");
        if (this.mStopTime == 0) {
            currentSessionLength = currentTime - this.mStartTime;
            sb.append("timeRunning=" + currentSessionLength + ", ");
        } else {
            sb.append("lastRunTimeLength=" + (this.mStopTime - this.mStartTime) + ", ");
        }
        sb.append("totalCountServiceStateChanges=" + this.mTotalCountServiceStateChanges + ", ");
        sb.append("currentCountServiceStateChanges=" + this.mCountServiceStateChanges + ", ");
        sb.append("totalTime=" + (this.mTotalTime + currentSessionLength) + ", ");
        sb.append("currentTime=" + currentTime + ", ");
        sb.append("countries=");
        Iterator<Country> it = this.mDebugLogs.iterator();
        while (it.hasNext()) {
            sb.append("\n   " + it.next().toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
