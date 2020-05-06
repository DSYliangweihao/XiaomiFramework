package com.android.commands.requestsync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SyncRequest;
import android.os.Bundle;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class RequestSync {
    /* access modifiers changed from: private */
    public Account mAccount;
    private String mAccountName;
    private String mAccountType;
    private String[] mArgs;
    /* access modifiers changed from: private */
    public String mAuthority;
    private String mCurArgData;
    /* access modifiers changed from: private */
    public int mExemptionFlag = 0;
    /* access modifiers changed from: private */
    public Bundle mExtras = new Bundle();
    private int mNextArg;
    private Operation mOperation;
    /* access modifiers changed from: private */
    public int mPeriodicIntervalSeconds;

    enum Operation {
        REQUEST_SYNC {
            /* access modifiers changed from: package-private */
            public void invoke(RequestSync caller) {
                int flag = caller.mExemptionFlag;
                caller.mExtras.putInt("v_exemption", flag);
                if (flag == 0) {
                    System.out.println("Making a sync request as a background app.\nNote: request may be throttled by App Standby.\nTo override this behavior and run a sync immediately, pass a -f or -F option (use -h for help).\n");
                }
                ContentResolver.requestSync(new SyncRequest.Builder().setSyncAdapter(caller.mAccount, caller.mAuthority).setExtras(caller.mExtras).syncOnce().build());
            }
        },
        ADD_PERIODIC_SYNC {
            /* access modifiers changed from: package-private */
            public void invoke(RequestSync caller) {
                ContentResolver.addPeriodicSync(caller.mAccount, caller.mAuthority, caller.mExtras, (long) caller.mPeriodicIntervalSeconds);
            }
        },
        REMOVE_PERIODIC_SYNC {
            /* access modifiers changed from: package-private */
            public void invoke(RequestSync caller) {
                ContentResolver.removePeriodicSync(caller.mAccount, caller.mAuthority, caller.mExtras);
            }
        };

        /* access modifiers changed from: package-private */
        public abstract void invoke(RequestSync requestSync);
    }

    public static void main(String[] args) {
        try {
            new RequestSync().run(args);
        } catch (IllegalArgumentException e) {
            showUsage();
            PrintStream printStream = System.err;
            printStream.println("Error: " + e);
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000f, code lost:
        r3 = r10.mAccountType;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void run(java.lang.String[] r11) throws java.lang.Exception {
        /*
            r10 = this;
            r10.mArgs = r11
            r0 = 0
            r10.mNextArg = r0
            boolean r1 = r10.parseArgs()
            if (r1 == 0) goto L_0x0097
            java.lang.String r2 = r10.mAccountName
            if (r2 == 0) goto L_0x0019
            java.lang.String r3 = r10.mAccountType
            if (r3 == 0) goto L_0x0019
            android.accounts.Account r4 = new android.accounts.Account
            r4.<init>(r2, r3)
            goto L_0x001a
        L_0x0019:
            r4 = 0
        L_0x001a:
            r2 = r4
            java.io.PrintStream r3 = java.lang.System.out
            java.lang.Object[] r4 = new java.lang.Object[r0]
            java.lang.String r5 = "Requesting sync for: \n"
            r3.printf(r5, r4)
            r3 = 2
            r4 = 1
            if (r2 == 0) goto L_0x003a
            java.io.PrintStream r5 = java.lang.System.out
            java.lang.Object[] r6 = new java.lang.Object[r3]
            java.lang.String r7 = r2.name
            r6[r0] = r7
            java.lang.String r7 = r2.type
            r6[r4] = r7
            java.lang.String r7 = "  Account: %s (%s)\n"
            r5.printf(r7, r6)
            goto L_0x0043
        L_0x003a:
            java.io.PrintStream r5 = java.lang.System.out
            java.lang.Object[] r6 = new java.lang.Object[r0]
            java.lang.String r7 = "  Account: all\n"
            r5.printf(r7, r6)
        L_0x0043:
            java.io.PrintStream r5 = java.lang.System.out
            java.lang.Object[] r6 = new java.lang.Object[r4]
            java.lang.String r7 = r10.mAuthority
            if (r7 == 0) goto L_0x004c
            goto L_0x004e
        L_0x004c:
            java.lang.String r7 = "All"
        L_0x004e:
            r6[r0] = r7
            java.lang.String r7 = "  Authority: %s\n"
            r5.printf(r7, r6)
            android.os.Bundle r5 = r10.mExtras
            int r5 = r5.size()
            if (r5 <= 0) goto L_0x0090
            java.io.PrintStream r5 = java.lang.System.out
            java.lang.Object[] r6 = new java.lang.Object[r0]
            java.lang.String r7 = "  Extras:\n"
            r5.printf(r7, r6)
            android.os.Bundle r5 = r10.mExtras
            java.util.Set r5 = r5.keySet()
            java.util.Iterator r5 = r5.iterator()
        L_0x0070:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x0090
            java.lang.Object r6 = r5.next()
            java.lang.String r6 = (java.lang.String) r6
            java.io.PrintStream r7 = java.lang.System.out
            java.lang.Object[] r8 = new java.lang.Object[r3]
            r8[r0] = r6
            android.os.Bundle r9 = r10.mExtras
            java.lang.Object r9 = r9.get(r6)
            r8[r4] = r9
            java.lang.String r9 = "    %s: %s\n"
            r7.printf(r9, r8)
            goto L_0x0070
        L_0x0090:
            r10.mAccount = r2
            com.android.commands.requestsync.RequestSync$Operation r0 = r10.mOperation
            r0.invoke(r10)
        L_0x0097:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.requestsync.RequestSync.run(java.lang.String[]):void");
    }

    private boolean parseArgs() throws URISyntaxException {
        this.mOperation = Operation.REQUEST_SYNC;
        String[] strArr = this.mArgs;
        if (strArr.length > 0) {
            String str = strArr[0];
            char c = 65535;
            int hashCode = str.hashCode();
            if (hashCode != -1439021497) {
                if (hashCode == 810481092 && str.equals("remove-periodic")) {
                    c = 1;
                }
            } else if (str.equals("add-periodic")) {
                c = 0;
            }
            if (c == 0) {
                this.mNextArg++;
                this.mOperation = Operation.ADD_PERIODIC_SYNC;
                this.mPeriodicIntervalSeconds = Integer.parseInt(nextArgRequired());
            } else if (c == 1) {
                this.mNextArg++;
                this.mOperation = Operation.REMOVE_PERIODIC_SYNC;
            }
        }
        while (true) {
            String nextOption = nextOption();
            String opt = nextOption;
            if (nextOption != null) {
                if (opt.equals("-h") || opt.equals("--help")) {
                    showUsage();
                } else if (opt.equals("-n") || opt.equals("--account-name")) {
                    this.mAccountName = nextArgRequired();
                } else if (opt.equals("-t") || opt.equals("--account-type")) {
                    this.mAccountType = nextArgRequired();
                } else if (opt.equals("-a") || opt.equals("--authority")) {
                    this.mAuthority = nextArgRequired();
                } else if (opt.equals("--is") || opt.equals("--ignore-settings")) {
                    this.mExtras.putBoolean("ignore_settings", true);
                } else if (opt.equals("--ib") || opt.equals("--ignore-backoff")) {
                    this.mExtras.putBoolean("ignore_backoff", true);
                } else if (opt.equals("--dd") || opt.equals("--discard-deletions")) {
                    this.mExtras.putBoolean("discard_deletions", true);
                } else if (opt.equals("--nr") || opt.equals("--no-retry")) {
                    this.mExtras.putBoolean("do_not_retry", true);
                } else if (opt.equals("--ex") || opt.equals("--expedited")) {
                    this.mExtras.putBoolean("expedited", true);
                } else if (opt.equals("-i") || opt.equals("--initialize")) {
                    this.mExtras.putBoolean("initialize", true);
                } else if (opt.equals("-m") || opt.equals("--manual")) {
                    this.mExtras.putBoolean("force", true);
                } else if (opt.equals("--od") || opt.equals("--override-deletions")) {
                    this.mExtras.putBoolean("deletions_override", true);
                } else if (opt.equals("-u") || opt.equals("--upload-only")) {
                    this.mExtras.putBoolean("upload", true);
                } else if (opt.equals("--rc") || opt.equals("--require-charging")) {
                    this.mExtras.putBoolean("require_charging", true);
                } else if (opt.equals("-e") || opt.equals("--es") || opt.equals("--extra-string")) {
                    this.mExtras.putString(nextArgRequired(), nextArgRequired());
                } else if (opt.equals("--esn") || opt.equals("--extra-string-null")) {
                    this.mExtras.putString(nextArgRequired(), (String) null);
                } else if (opt.equals("--ei") || opt.equals("--extra-int")) {
                    this.mExtras.putInt(nextArgRequired(), Integer.valueOf(nextArgRequired()).intValue());
                } else if (opt.equals("--el") || opt.equals("--extra-long")) {
                    this.mExtras.putLong(nextArgRequired(), Long.parseLong(nextArgRequired()));
                } else if (opt.equals("--ef") || opt.equals("--extra-float")) {
                    this.mExtras.putFloat(nextArgRequired(), (float) Long.parseLong(nextArgRequired()));
                } else if (opt.equals("--ed") || opt.equals("--extra-double")) {
                    this.mExtras.putFloat(nextArgRequired(), (float) Long.parseLong(nextArgRequired()));
                } else if (opt.equals("--ez") || opt.equals("--extra-bool")) {
                    this.mExtras.putBoolean(nextArgRequired(), Boolean.valueOf(nextArgRequired()).booleanValue());
                } else if (opt.equals("-f") || opt.equals("--foreground")) {
                    this.mExemptionFlag = 1;
                } else if (opt.equals("-F") || opt.equals("--top")) {
                    this.mExemptionFlag = 2;
                } else {
                    System.err.println("Error: Unknown option: " + opt);
                    showUsage();
                    return false;
                }
            } else if (this.mNextArg >= this.mArgs.length) {
                return true;
            } else {
                showUsage();
                return false;
            }
        }
        showUsage();
        return false;
    }

    private String nextOption() {
        if (this.mCurArgData == null) {
            int i = this.mNextArg;
            String[] strArr = this.mArgs;
            if (i >= strArr.length) {
                return null;
            }
            String arg = strArr[i];
            if (!arg.startsWith("-")) {
                return null;
            }
            this.mNextArg++;
            if (arg.equals("--")) {
                return null;
            }
            if (arg.length() <= 1 || arg.charAt(1) == '-') {
                this.mCurArgData = null;
                return arg;
            } else if (arg.length() > 2) {
                this.mCurArgData = arg.substring(2);
                return arg.substring(0, 2);
            } else {
                this.mCurArgData = null;
                return arg;
            }
        } else {
            throw new IllegalArgumentException("No argument expected after \"" + this.mArgs[this.mNextArg - 1] + "\"");
        }
    }

    private String nextArg() {
        if (this.mCurArgData != null) {
            String arg = this.mCurArgData;
            this.mCurArgData = null;
            return arg;
        }
        int i = this.mNextArg;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        this.mNextArg = i + 1;
        return strArr[i];
    }

    private String nextArgRequired() {
        String arg = nextArg();
        if (arg != null) {
            return arg;
        }
        String prev = this.mArgs[this.mNextArg - 1];
        throw new IllegalArgumentException("Argument expected after \"" + prev + "\"");
    }

    private static void showUsage() {
        System.err.println("Usage:\n\n  requestsync [options]\n    With no options, a sync will be requested for all account and all sync\n    authorities with no extras.\n    Basic options:\n       -h|--help: Display this message\n       -n|--account-name <ACCOUNT-NAME>\n       -t|--account-type <ACCOUNT-TYPE>\n       -a|--authority <AUTHORITY>\n    App-standby related options\n\n       -f|--foreground (cause WORKING_SET, FREQUENT sync adapters to run immediately)\n       -F|--top (cause even RARE sync adapters to run immediately)\n    ContentResolver extra options:\n      --is|--ignore-settings: Add SYNC_EXTRAS_IGNORE_SETTINGS\n      --ib|--ignore-backoff: Add SYNC_EXTRAS_IGNORE_BACKOFF\n      --dd|--discard-deletions: Add SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS\n      --nr|--no-retry: Add SYNC_EXTRAS_DO_NOT_RETRY\n      --ex|--expedited: Add SYNC_EXTRAS_EXPEDITED\n      -i|--initialize: Add SYNC_EXTRAS_INITIALIZE\n      --m|--manual: Add SYNC_EXTRAS_MANUAL\n      --od|--override-deletions: Add SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS\n      -u|--upload-only: Add SYNC_EXTRAS_UPLOAD\n      --rc|--require-charging: Add SYNC_EXTRAS_REQUIRE_CHARGING\n    Custom extra options:\n      -e|--es|--extra-string <KEY> <VALUE>\n      --esn|--extra-string-null <KEY>\n      --ei|--extra-int <KEY> <VALUE>\n      --el|--extra-long <KEY> <VALUE>\n      --ef|--extra-float <KEY> <VALUE>\n      --ed|--extra-double <KEY> <VALUE>\n      --ez|--extra-bool <KEY> <VALUE>\n\n  requestsync add-periodic INTERVAL-SECOND [options]\n  requestsync remove-periodic [options]\n    Mandatory options:\n      -n|--account-name <ACCOUNT-NAME>\n      -t|--account-type <ACCOUNT-TYPE>\n      -a|--authority <AUTHORITY>\n    Also takes the above extra options.\n");
    }
}
