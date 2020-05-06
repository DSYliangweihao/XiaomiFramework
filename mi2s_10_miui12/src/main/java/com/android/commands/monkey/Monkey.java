package com.android.commands.monkey;

import android.app.ActivityManager;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.view.IWindowManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Monkey {
    private static final int DEBUG_ALLOW_ANY_RESTARTS = 0;
    private static final int DEBUG_ALLOW_ANY_STARTS = 0;
    private static int NUM_READ_TOMBSTONE_RETRIES = 5;
    private static final File TOMBSTONES_PATH = new File("/data/tombstones");
    private static final String TOMBSTONE_PREFIX = "tombstone_";
    public static Intent currentIntent;
    public static String currentPackage;
    /* access modifiers changed from: private */
    public boolean mAbort;
    private IActivityManager mAm;
    private String[] mArgs;
    private long mBugreportFrequency = 10;
    int mCount = 1000;
    private boolean mCountEvents = true;
    private String mCurArgData;
    long mDeviceSleepTime = 30000;
    private boolean mDisableLogs = false;
    long mDroppedFlipEvents = 0;
    long mDroppedKeyEvents = 0;
    long mDroppedPointerEvents = 0;
    long mDroppedRotationEvents = 0;
    long mDroppedTrackballEvents = 0;
    private volatile ErrorHandler mErrorHandler = null;
    private HandlerThread mErrorThread = null;
    MonkeyEventSource mEventSource;
    float[] mFactors = new float[12];
    private boolean mGenerateHprof;
    private boolean mGetPeriodicBugreport = false;
    /* access modifiers changed from: private */
    public boolean mIgnoreCrashes;
    private boolean mIgnoreNativeCrashes;
    private boolean mIgnoreSecurityExceptions;
    /* access modifiers changed from: private */
    public boolean mIgnoreTimeouts;
    /* access modifiers changed from: private */
    public boolean mKillProcessAfterError;
    private ArrayList<ComponentName> mMainApps = new ArrayList<>();
    private ArrayList<String> mMainCategories = new ArrayList<>();
    /* access modifiers changed from: private */
    public String mMatchDescription;
    private boolean mMonitorNativeCrashes;
    private MonkeyNetworkMonitor mNetworkMonitor = new MonkeyNetworkMonitor();
    private int mNextArg;
    private final Object mObject = new Object();
    private boolean mPermissionTargetSystem = false;
    private String mPkgBlacklistFile;
    private String mPkgWhitelistFile;
    /* access modifiers changed from: private */
    public IPackageManager mPm;
    long mProfileWaitTime = 5000;
    Random mRandom = null;
    boolean mRandomizeScript = false;
    boolean mRandomizeThrottle = false;
    /* access modifiers changed from: private */
    public String mReportProcessName;
    /* access modifiers changed from: private */
    public boolean mRequestAnrBugreport = false;
    /* access modifiers changed from: private */
    public boolean mRequestAnrTraces = false;
    /* access modifiers changed from: private */
    public boolean mRequestAppCrashBugreport = false;
    /* access modifiers changed from: private */
    public boolean mRequestBugreport = false;
    /* access modifiers changed from: private */
    public boolean mRequestDumpsysMemInfo = false;
    private boolean mRequestPeriodicBugreport = false;
    /* access modifiers changed from: private */
    public boolean mRequestProcRank = false;
    /* access modifiers changed from: private */
    public boolean mRequestWatchdogBugreport = false;
    private ArrayList<String> mScriptFileNames = new ArrayList<>();
    boolean mScriptLog = false;
    long mSeed = 0;
    private boolean mSendNoEvents;
    private int mServerPort = -1;
    private String mSetupFileName = null;
    long mThrottle = 0;
    private HashSet<Long> mTombstones = null;
    /* access modifiers changed from: private */
    public int mVerbose;
    /* access modifiers changed from: private */
    public boolean mWatchdogWaiting = false;
    private IWindowManager mWm;

    private final class ErrorHandler extends Handler {
        static final int APP_CRASHED = 0;
        static final int APP_NOT_RESPONDING = 1;
        static final int SYSTEM_NOT_RESPONDING = 2;

        public ErrorHandler(Looper looper) {
            super(looper, (Handler.Callback) null);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                String crashedProcessName = (String) msg.obj;
                synchronized (Monkey.this) {
                    if (!Monkey.this.mIgnoreCrashes) {
                        boolean unused = Monkey.this.mAbort = true;
                    }
                    if (Monkey.this.mRequestBugreport) {
                        boolean unused2 = Monkey.this.mRequestAppCrashBugreport = true;
                        String unused3 = Monkey.this.mReportProcessName = crashedProcessName;
                    }
                }
            } else if (i == 1) {
                String anrProcessName = (String) msg.obj;
                synchronized (Monkey.this) {
                    boolean unused4 = Monkey.this.mRequestAnrTraces = true;
                    boolean unused5 = Monkey.this.mRequestDumpsysMemInfo = true;
                    boolean unused6 = Monkey.this.mRequestProcRank = true;
                    if (Monkey.this.mRequestBugreport) {
                        boolean unused7 = Monkey.this.mRequestAnrBugreport = true;
                        String unused8 = Monkey.this.mReportProcessName = anrProcessName;
                    }
                }
                if (!Monkey.this.mIgnoreTimeouts) {
                    synchronized (Monkey.this) {
                        boolean unused9 = Monkey.this.mAbort = true;
                    }
                }
            } else if (i != 2) {
                System.out.println("    // wrong message received of AppCrashHandler");
            } else {
                String message = (String) msg.obj;
                synchronized (Monkey.this) {
                    if (Monkey.this.mMatchDescription == null || message.contains(Monkey.this.mMatchDescription)) {
                        if (!Monkey.this.mIgnoreCrashes) {
                            boolean unused10 = Monkey.this.mAbort = true;
                        }
                        if (Monkey.this.mRequestBugreport) {
                            boolean unused11 = Monkey.this.mRequestWatchdogBugreport = true;
                        }
                    }
                    boolean unused12 = Monkey.this.mWatchdogWaiting = true;
                }
                synchronized (Monkey.this) {
                    while (Monkey.this.mWatchdogWaiting) {
                        try {
                            Monkey.this.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendErrorMessage(int what, String obj) {
        if (this.mErrorHandler == null) {
            synchronized (this.mObject) {
                if (this.mErrorHandler == null) {
                    this.mErrorThread = new HandlerThread("ErrorHandleThread");
                    this.mErrorThread.start();
                    this.mErrorHandler = new ErrorHandler(this.mErrorThread.getLooper());
                }
            }
        }
        this.mErrorHandler.sendMessage(this.mErrorHandler.obtainMessage(what, obj));
    }

    private class ActivityController extends IActivityController.Stub {
        private ActivityController() {
        }

        public boolean activityStarting(Intent intent, String pkg) {
            boolean allow = isActivityStartingAllowed(intent, pkg);
            if (Monkey.this.mVerbose > 0) {
                StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
                Logger logger = Logger.out;
                StringBuilder sb = new StringBuilder();
                sb.append("    // ");
                sb.append(allow ? "Allowing" : "Rejecting");
                sb.append(" start of ");
                sb.append(intent);
                sb.append(" in package ");
                sb.append(pkg);
                logger.println(sb.toString());
                StrictMode.setThreadPolicy(savedPolicy);
            }
            Monkey.currentPackage = pkg;
            Monkey.currentIntent = intent;
            return allow;
        }

        private boolean isActivityStartingAllowed(Intent intent, String pkg) {
            if (MonkeyUtils.getPackageFilter().checkEnteringPackage(pkg)) {
                return true;
            }
            Set<String> categories = intent.getCategories();
            if (intent.getAction() == "android.intent.action.MAIN" && categories != null && categories.contains("android.intent.category.HOME")) {
                try {
                    if (pkg.equals(Monkey.this.mPm.resolveIntent(intent, intent.getType(), 0, ActivityManager.getCurrentUser()).activityInfo.packageName)) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Logger.err.println("** Failed talking with package manager!");
                    return false;
                }
            }
            return false;
        }

        public boolean activityResuming(String pkg) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger logger = Logger.out;
            logger.println("    // activityResuming(" + pkg + ")");
            boolean allow = MonkeyUtils.getPackageFilter().checkEnteringPackage(pkg);
            if (!allow && Monkey.this.mVerbose > 0) {
                Logger logger2 = Logger.out;
                StringBuilder sb = new StringBuilder();
                sb.append("    // ");
                sb.append(allow ? "Allowing" : "Rejecting");
                sb.append(" resume of package ");
                sb.append(pkg);
                logger2.println(sb.toString());
            }
            Monkey.currentPackage = pkg;
            StrictMode.setThreadPolicy(savedPolicy);
            return allow;
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger logger = Logger.err;
            logger.println("// CRASH: " + processName + " (pid " + pid + ")");
            Logger logger2 = Logger.err;
            StringBuilder sb = new StringBuilder();
            sb.append("// Short Msg: ");
            sb.append(shortMsg);
            logger2.println(sb.toString());
            Logger logger3 = Logger.err;
            logger3.println("// Long Msg: " + longMsg);
            Logger logger4 = Logger.err;
            logger4.println("// Build Label: " + Build.FINGERPRINT);
            Logger logger5 = Logger.err;
            logger5.println("// Build Changelist: " + Build.VERSION.INCREMENTAL);
            Logger logger6 = Logger.err;
            logger6.println("// Build Time: " + Build.TIME);
            Logger logger7 = Logger.err;
            logger7.println("// " + stackTrace.replace("\n", "\n// "));
            StrictMode.setThreadPolicy(savedPolicy);
            if ((Monkey.this.mMatchDescription != null && !shortMsg.contains(Monkey.this.mMatchDescription) && !longMsg.contains(Monkey.this.mMatchDescription) && !stackTrace.contains(Monkey.this.mMatchDescription)) || (Monkey.this.mIgnoreCrashes && !Monkey.this.mRequestBugreport)) {
                return false;
            }
            Monkey.this.sendErrorMessage(0, processName);
            return !Monkey.this.mKillProcessAfterError;
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            return 0;
        }

        public int appNotResponding(String processName, int pid, String processStats) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger logger = Logger.err;
            logger.println("// NOT RESPONDING: " + processName + " (pid " + pid + ")");
            Logger.err.println(processStats);
            StrictMode.setThreadPolicy(savedPolicy);
            if (Monkey.this.mMatchDescription == null || processStats.contains(Monkey.this.mMatchDescription)) {
                Monkey.this.sendErrorMessage(1, processName);
            }
            if (Monkey.this.mKillProcessAfterError) {
                return -1;
            }
            return 1;
        }

        public int systemNotResponding(String message) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger logger = Logger.err;
            logger.println("// WATCHDOG: " + message);
            StrictMode.setThreadPolicy(savedPolicy);
            Monkey.this.sendErrorMessage(2, message);
            return Monkey.this.mKillProcessAfterError ? -1 : 1;
        }
    }

    private void reportProcRank() {
        commandLineReport("procrank", "procrank");
    }

    private void reportAnrTraces() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        File[] recentTraces = new File("/data/anr/").listFiles();
        if (recentTraces != null) {
            File mostRecent = null;
            long mostRecentMtime = 0;
            for (File trace : recentTraces) {
                long mtime = trace.lastModified();
                if (mtime > mostRecentMtime) {
                    mostRecentMtime = mtime;
                    mostRecent = trace;
                }
            }
            if (mostRecent != null) {
                commandLineReport("anr traces", "cat " + mostRecent.getAbsolutePath());
            }
        }
    }

    private void reportDumpsysMemInfo() {
        commandLineReport("meminfo", "dumpsys meminfo");
    }

    private void commandLineReport(String reportName, String command) {
        if (!this.mDisableLogs) {
            Logger logger = Logger.err;
            logger.println(reportName + ":");
            Runtime runtime = Runtime.getRuntime();
            Writer logOutput = null;
            try {
                Process p = Runtime.getRuntime().exec(command);
                if (this.mRequestBugreport) {
                    logOutput = new BufferedWriter(new FileWriter(new File(Environment.getLegacyExternalStorageDirectory(), reportName), true));
                }
                BufferedReader inBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while (true) {
                    String readLine = inBuffer.readLine();
                    String s = readLine;
                    if (readLine == null) {
                        break;
                    } else if (this.mRequestBugreport) {
                        try {
                            logOutput.write(s);
                            logOutput.write("\n");
                        } catch (IOException e) {
                            while (inBuffer.readLine() != null) {
                            }
                            Logger.err.println(e.toString());
                        }
                    } else {
                        Logger.err.println(s);
                    }
                }
                int status = p.waitFor();
                Logger logger2 = Logger.err;
                logger2.println("// " + reportName + " status was " + status);
                if (logOutput != null) {
                    logOutput.close();
                }
            } catch (Exception e2) {
                Logger logger3 = Logger.err;
                logger3.println("// Exception from " + reportName + ":");
                Logger.err.println(e2.toString());
            }
        }
    }

    private void writeScriptLog(int count) {
        try {
            Writer output = new BufferedWriter(new FileWriter(new File(Environment.getLegacyExternalStorageDirectory(), "scriptlog.txt"), true));
            output.write("iteration: " + count + " time: " + MonkeyUtils.toCalendarTime(System.currentTimeMillis()) + "\n");
            output.close();
        } catch (IOException e) {
            Logger.err.println(e.toString());
        }
    }

    private void getBugreport(String reportName) {
        String bugreportName = (reportName + MonkeyUtils.toCalendarTime(System.currentTimeMillis())).replaceAll("[ ,:]", "_");
        commandLineReport(bugreportName + ".txt", "bugreport");
    }

    public static void main(String[] args) {
        Process.setArgV0("com.android.commands.monkey");
        Logger logger = Logger.err;
        logger.println("args: " + Arrays.toString(args));
        System.exit(new Monkey().run(args));
    }

    /* JADX INFO: finally extract failed */
    private int run(String[] args) {
        String[] strArr = args;
        for (String s : strArr) {
            if ("--wait-dbg".equals(s)) {
                Debug.waitForDebugger();
            }
        }
        this.mVerbose = 0;
        this.mCount = 1000;
        this.mSeed = 0;
        this.mThrottle = 0;
        this.mArgs = strArr;
        for (String a : strArr) {
            Logger.err.println(" arg: \"" + a + "\"");
        }
        this.mNextArg = 0;
        for (int i = 0; i < 12; i++) {
            this.mFactors[i] = 1.0f;
        }
        if (processOptions() == 0 || !loadPackageLists()) {
            return -1;
        }
        if (this.mMainCategories.size() == 0) {
            this.mMainCategories.add("android.intent.category.LAUNCHER");
            this.mMainCategories.add("android.intent.category.MONKEY");
        }
        if (this.mSeed == 0) {
            this.mSeed = System.currentTimeMillis() + ((long) System.identityHashCode(this));
        }
        if (this.mVerbose > 0) {
            Logger.out.println(":Monkey: seed=" + this.mSeed + " count=" + this.mCount);
            MonkeyUtils.getPackageFilter().dump();
            if (this.mMainCategories.size() != 0) {
                Iterator<String> it = this.mMainCategories.iterator();
                while (it.hasNext()) {
                    Logger.out.println(":IncludeCategory: " + it.next());
                }
            }
        }
        if (!checkInternalConfiguration()) {
            return -2;
        }
        if (!getSystemInterfaces()) {
            return -3;
        }
        if (!getMainApps()) {
            return -4;
        }
        this.mRandom = new Random(this.mSeed);
        ArrayList<String> arrayList = this.mScriptFileNames;
        if (arrayList == null || arrayList.size() != 1) {
            ArrayList<String> arrayList2 = this.mScriptFileNames;
            if (arrayList2 == null || arrayList2.size() <= 1) {
                int i2 = this.mServerPort;
                if (i2 != -1) {
                    try {
                        this.mEventSource = new MonkeySourceNetwork(i2);
                        this.mCount = Integer.MAX_VALUE;
                    } catch (IOException e) {
                        Logger.out.println("Error binding to network socket.");
                        return -5;
                    }
                } else {
                    if (this.mVerbose >= 2) {
                        Logger.out.println("// Seeded: " + this.mSeed);
                    }
                    this.mEventSource = new MonkeySourceRandom(this.mRandom, this.mMainApps, this.mThrottle, this.mRandomizeThrottle, this.mPermissionTargetSystem);
                    this.mEventSource.setVerbose(this.mVerbose);
                    for (int i3 = 0; i3 < 12; i3++) {
                        float[] fArr = this.mFactors;
                        if (fArr[i3] <= 0.0f) {
                            ((MonkeySourceRandom) this.mEventSource).setFactors(i3, fArr[i3]);
                        }
                    }
                    ((MonkeySourceRandom) this.mEventSource).generateActivity();
                }
            } else {
                String str = this.mSetupFileName;
                if (str != null) {
                    this.mEventSource = new MonkeySourceRandomScript(str, this.mScriptFileNames, this.mThrottle, this.mRandomizeThrottle, this.mRandom, this.mProfileWaitTime, this.mDeviceSleepTime, this.mRandomizeScript);
                    this.mCount++;
                } else {
                    this.mEventSource = new MonkeySourceRandomScript(this.mScriptFileNames, this.mThrottle, this.mRandomizeThrottle, this.mRandom, this.mProfileWaitTime, this.mDeviceSleepTime, this.mRandomizeScript);
                }
                this.mEventSource.setVerbose(this.mVerbose);
                this.mCountEvents = false;
            }
        } else {
            this.mEventSource = new MonkeySourceScript(this.mRandom, this.mScriptFileNames.get(0), this.mThrottle, this.mRandomizeThrottle, this.mProfileWaitTime, this.mDeviceSleepTime);
            this.mEventSource.setVerbose(this.mVerbose);
            this.mCountEvents = false;
        }
        if (!this.mEventSource.validate()) {
            return -5;
        }
        if (this.mGenerateHprof) {
            signalPersistentProcesses();
        }
        this.mNetworkMonitor.start();
        try {
            int crashedAtCycle = runMonkeyCycles();
            new MonkeyRotationEvent(0, false).injectEvent(this.mWm, this.mAm, this.mVerbose);
            this.mNetworkMonitor.stop();
            synchronized (this) {
                if (this.mRequestAnrTraces) {
                    reportAnrTraces();
                    this.mRequestAnrTraces = false;
                }
                if (this.mRequestAnrBugreport) {
                    Logger.out.println("Print the anr report");
                    getBugreport("anr_" + this.mReportProcessName + "_");
                    this.mRequestAnrBugreport = false;
                }
                if (this.mRequestWatchdogBugreport) {
                    Logger.out.println("Print the watchdog report");
                    getBugreport("anr_watchdog_");
                    this.mRequestWatchdogBugreport = false;
                }
                if (this.mRequestAppCrashBugreport) {
                    getBugreport("app_crash" + this.mReportProcessName + "_");
                    this.mRequestAppCrashBugreport = false;
                }
                if (this.mRequestDumpsysMemInfo) {
                    reportDumpsysMemInfo();
                    this.mRequestDumpsysMemInfo = false;
                }
                if (this.mRequestPeriodicBugreport) {
                    getBugreport("Bugreport_");
                    this.mRequestPeriodicBugreport = false;
                }
                if (this.mWatchdogWaiting) {
                    this.mWatchdogWaiting = false;
                    notifyAll();
                }
            }
            if (this.mGenerateHprof) {
                signalPersistentProcesses();
                if (this.mVerbose > 0) {
                    Logger.out.println("// Generated profiling reports in /data/misc");
                }
            }
            try {
                this.mAm.setActivityController((IActivityController) null, true);
                this.mNetworkMonitor.unregister(this.mAm);
            } catch (RemoteException e2) {
                int i4 = this.mCount;
                if (crashedAtCycle >= i4) {
                    crashedAtCycle = i4 - 1;
                }
            }
            if (this.mVerbose > 0) {
                Logger.out.println(":Dropped: keys=" + this.mDroppedKeyEvents + " pointers=" + this.mDroppedPointerEvents + " trackballs=" + this.mDroppedTrackballEvents + " flips=" + this.mDroppedFlipEvents + " rotations=" + this.mDroppedRotationEvents);
            }
            this.mNetworkMonitor.dump();
            if (crashedAtCycle < this.mCount - 1) {
                Logger.err.println("** System appears to have crashed at event " + crashedAtCycle + " of " + this.mCount + " using seed " + this.mSeed);
                return crashedAtCycle;
            } else if (this.mVerbose <= 0) {
                return 0;
            } else {
                Logger.out.println("// Monkey finished");
                return 0;
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            new MonkeyRotationEvent(0, false).injectEvent(this.mWm, this.mAm, this.mVerbose);
            throw th2;
        }
    }

    private boolean processOptions() {
        if (this.mArgs.length < 1) {
            showUsage();
            return false;
        }
        try {
            Set<String> validPackages = new HashSet<>();
            while (true) {
                String nextOption = nextOption();
                String opt = nextOption;
                if (nextOption == null) {
                    MonkeyUtils.getPackageFilter().addValidPackages(validPackages);
                    if (this.mServerPort == -1) {
                        String countStr = nextArg();
                        if (countStr == null) {
                            Logger.err.println("** Error: Count not specified");
                            showUsage();
                            return false;
                        }
                        try {
                            this.mCount = Integer.parseInt(countStr);
                        } catch (NumberFormatException e) {
                            Logger.err.println("** Error: Count is not a number: \"" + countStr + "\"");
                            showUsage();
                            return false;
                        }
                    }
                    return true;
                } else if (opt.equals("-s")) {
                    this.mSeed = nextOptionLong("Seed");
                } else if (opt.equals("-p")) {
                    validPackages.add(nextOptionData());
                } else if (opt.equals("-c")) {
                    this.mMainCategories.add(nextOptionData());
                } else if (opt.equals("-v")) {
                    this.mVerbose++;
                } else if (opt.equals("--ignore-crashes")) {
                    this.mIgnoreCrashes = true;
                } else if (opt.equals("--ignore-timeouts")) {
                    this.mIgnoreTimeouts = true;
                } else if (opt.equals("--ignore-security-exceptions")) {
                    this.mIgnoreSecurityExceptions = true;
                } else if (opt.equals("--monitor-native-crashes")) {
                    this.mMonitorNativeCrashes = true;
                } else if (opt.equals("--ignore-native-crashes")) {
                    this.mIgnoreNativeCrashes = true;
                } else if (opt.equals("--kill-process-after-error")) {
                    this.mKillProcessAfterError = true;
                } else if (opt.equals("--hprof")) {
                    this.mGenerateHprof = true;
                } else if (opt.equals("--match-description")) {
                    this.mMatchDescription = nextOptionData();
                } else if (opt.equals("--pct-touch")) {
                    this.mFactors[0] = (float) (-nextOptionLong("touch events percentage"));
                } else if (opt.equals("--pct-motion")) {
                    this.mFactors[1] = (float) (-nextOptionLong("motion events percentage"));
                } else if (opt.equals("--pct-trackball")) {
                    this.mFactors[3] = (float) (-nextOptionLong("trackball events percentage"));
                } else if (opt.equals("--pct-rotation")) {
                    this.mFactors[4] = (float) (-nextOptionLong("screen rotation events percentage"));
                } else if (opt.equals("--pct-syskeys")) {
                    this.mFactors[8] = (float) (-nextOptionLong("system (key) operations percentage"));
                } else if (opt.equals("--pct-nav")) {
                    this.mFactors[6] = (float) (-nextOptionLong("nav events percentage"));
                } else if (opt.equals("--pct-majornav")) {
                    this.mFactors[7] = (float) (-nextOptionLong("major nav events percentage"));
                } else if (opt.equals("--pct-appswitch")) {
                    this.mFactors[9] = (float) (-nextOptionLong("app switch events percentage"));
                } else if (opt.equals("--pct-flip")) {
                    this.mFactors[10] = (float) (-nextOptionLong("keyboard flip percentage"));
                } else if (opt.equals("--pct-anyevent")) {
                    this.mFactors[11] = (float) (-nextOptionLong("any events percentage"));
                } else if (opt.equals("--pct-pinchzoom")) {
                    this.mFactors[2] = (float) (-nextOptionLong("pinch zoom events percentage"));
                } else if (opt.equals("--pct-permission")) {
                    this.mFactors[5] = (float) (-nextOptionLong("runtime permission toggle events percentage"));
                } else if (opt.equals("--pkg-blacklist-file")) {
                    this.mPkgBlacklistFile = nextOptionData();
                } else if (opt.equals("--pkg-whitelist-file")) {
                    this.mPkgWhitelistFile = nextOptionData();
                } else if (opt.equals("--throttle")) {
                    this.mThrottle = nextOptionLong("delay (in milliseconds) to wait between events");
                } else if (opt.equals("--randomize-throttle")) {
                    this.mRandomizeThrottle = true;
                } else if (!opt.equals("--wait-dbg")) {
                    if (opt.equals("--dbg-no-events")) {
                        this.mSendNoEvents = true;
                    } else if (opt.equals("--port")) {
                        this.mServerPort = (int) nextOptionLong("Server port to listen on for commands");
                    } else if (opt.equals("--setup")) {
                        this.mSetupFileName = nextOptionData();
                    } else if (opt.equals("-f")) {
                        this.mScriptFileNames.add(nextOptionData());
                    } else if (opt.equals("--profile-wait")) {
                        this.mProfileWaitTime = nextOptionLong("Profile delay (in milliseconds) to wait between user action");
                    } else if (opt.equals("--device-sleep-time")) {
                        this.mDeviceSleepTime = nextOptionLong("Device sleep time(in milliseconds)");
                    } else if (opt.equals("--randomize-script")) {
                        this.mRandomizeScript = true;
                    } else if (opt.equals("--script-log")) {
                        this.mScriptLog = true;
                    } else if (opt.equals("--bugreport")) {
                        this.mRequestBugreport = true;
                    } else if (opt.equals("--periodic-bugreport")) {
                        this.mGetPeriodicBugreport = true;
                        this.mBugreportFrequency = nextOptionLong("Number of iterations");
                    } else if (opt.equals("--permission-target-system")) {
                        this.mPermissionTargetSystem = true;
                    } else if (opt.equals("-h")) {
                        showUsage();
                        return false;
                    } else if (opt.equals("--disable-logs")) {
                        Logger.err.println("** Monkey: all log disbled!");
                        this.mDisableLogs = true;
                        Logger.stdout = false;
                        Logger.logcat = false;
                    } else {
                        Logger.err.println("** Error: Unknown option: " + opt);
                        showUsage();
                        return false;
                    }
                }
            }
        } catch (RuntimeException ex) {
            Logger.err.println("** Error: " + ex.toString());
            showUsage();
            return false;
        }
    }

    private static boolean loadPackageListFromFile(String fileName, Set<String> list) {
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(fileName));
            while (true) {
                String readLine = reader2.readLine();
                String s = readLine;
                if (readLine != null) {
                    String s2 = s.trim();
                    if (s2.length() > 0 && !s2.startsWith("#")) {
                        list.add(s2);
                    }
                } else {
                    try {
                        reader2.close();
                        return true;
                    } catch (IOException ioe) {
                        Logger logger = Logger.err;
                        logger.println("" + ioe);
                        return true;
                    }
                }
            }
        } catch (IOException ioe2) {
            Logger logger2 = Logger.err;
            logger2.println("" + ioe2);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe3) {
                    Logger logger3 = Logger.err;
                    logger3.println("" + ioe3);
                }
            }
            return false;
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe4) {
                    Logger logger4 = Logger.err;
                    logger4.println("" + ioe4);
                }
            }
            throw th;
        }
    }

    private boolean loadPackageLists() {
        if ((this.mPkgWhitelistFile != null || MonkeyUtils.getPackageFilter().hasValidPackages()) && this.mPkgBlacklistFile != null) {
            Logger.err.println("** Error: you can not specify a package blacklist together with a whitelist or individual packages (via -p).");
            return false;
        }
        Set<String> validPackages = new HashSet<>();
        String str = this.mPkgWhitelistFile;
        if (str != null && !loadPackageListFromFile(str, validPackages)) {
            return false;
        }
        MonkeyUtils.getPackageFilter().addValidPackages(validPackages);
        Set<String> invalidPackages = new HashSet<>();
        String str2 = this.mPkgBlacklistFile;
        if (str2 != null && !loadPackageListFromFile(str2, invalidPackages)) {
            return false;
        }
        MonkeyUtils.getPackageFilter().addInvalidPackages(invalidPackages);
        return true;
    }

    private boolean checkInternalConfiguration() {
        return true;
    }

    private boolean getSystemInterfaces() {
        this.mAm = ActivityManager.getService();
        if (this.mAm == null) {
            Logger.err.println("** Error: Unable to connect to activity manager; is the system running?");
            return false;
        }
        this.mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        if (this.mWm == null) {
            Logger.err.println("** Error: Unable to connect to window manager; is the system running?");
            return false;
        }
        this.mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (this.mPm == null) {
            Logger.err.println("** Error: Unable to connect to package manager; is the system running?");
            return false;
        }
        try {
            this.mAm.setActivityController(new ActivityController(), true);
            this.mNetworkMonitor.register(this.mAm);
            return true;
        } catch (RemoteException e) {
            Logger.err.println("** Failed talking with activity manager!");
            return false;
        }
    }

    private boolean getMainApps() {
        try {
            int N = this.mMainCategories.size();
            for (int i = 0; i < N; i++) {
                Intent intent = new Intent("android.intent.action.MAIN");
                String category = this.mMainCategories.get(i);
                if (category.length() > 0) {
                    intent.addCategory(category);
                }
                List<ResolveInfo> mainApps = this.mPm.queryIntentActivities(intent, (String) null, 0, ActivityManager.getCurrentUser()).getList();
                if (mainApps != null) {
                    if (mainApps.size() != 0) {
                        int i2 = 2;
                        if (this.mVerbose >= 2) {
                            Logger logger = Logger.out;
                            logger.println("// Selecting main activities from category " + category);
                        }
                        int NA = mainApps.size();
                        int a = 0;
                        while (a < NA) {
                            ResolveInfo r = mainApps.get(a);
                            String packageName = r.activityInfo.applicationInfo.packageName;
                            if (MonkeyUtils.getPackageFilter().checkEnteringPackage(packageName)) {
                                if (this.mVerbose >= i2) {
                                    Logger logger2 = Logger.out;
                                    logger2.println("//   + Using main activity " + r.activityInfo.name + " (from package " + packageName + ")");
                                }
                                this.mMainApps.add(new ComponentName(packageName, r.activityInfo.name));
                            } else if (this.mVerbose >= 3) {
                                Logger logger3 = Logger.out;
                                logger3.println("//   - NOT USING main activity " + r.activityInfo.name + " (from package " + packageName + ")");
                            }
                            a++;
                            i2 = 2;
                        }
                    }
                }
                Logger logger4 = Logger.err;
                logger4.println("// Warning: no activities found for category " + category);
            }
            if (this.mMainApps.size() != 0) {
                return true;
            }
            Logger.out.println("** No activities found to run, monkey aborted.");
            return false;
        } catch (RemoteException e) {
            Logger.err.println("** Failed talking with package manager!");
            return false;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x019c, code lost:
        r5 = true ^ r13.mIgnoreSecurityExceptions;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int runMonkeyCycles() {
        /*
            r13 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
        L_0x0006:
            if (r5 != 0) goto L_0x01db
            int r6 = r13.mCount     // Catch:{ RuntimeException -> 0x01d4 }
            if (r1 >= r6) goto L_0x01db
            monitor-enter(r13)     // Catch:{ RuntimeException -> 0x01d4 }
            boolean r6 = r13.mRequestProcRank     // Catch:{ all -> 0x01d1 }
            r7 = 0
            if (r6 == 0) goto L_0x0017
            r13.reportProcRank()     // Catch:{ all -> 0x01d1 }
            r13.mRequestProcRank = r7     // Catch:{ all -> 0x01d1 }
        L_0x0017:
            boolean r6 = r13.mRequestAnrTraces     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x001e
            r13.mRequestAnrTraces = r7     // Catch:{ all -> 0x01d1 }
            r2 = 1
        L_0x001e:
            boolean r6 = r13.mRequestAnrBugreport     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x003f
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01d1 }
            r6.<init>()     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = "anr_"
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = r13.mReportProcessName     // Catch:{ all -> 0x01d1 }
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = "_"
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01d1 }
            r13.getBugreport(r6)     // Catch:{ all -> 0x01d1 }
            r13.mRequestAnrBugreport = r7     // Catch:{ all -> 0x01d1 }
        L_0x003f:
            boolean r6 = r13.mRequestWatchdogBugreport     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x0051
            com.android.commands.monkey.Logger r6 = com.android.commands.monkey.Logger.out     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = "Print the watchdog report"
            r6.println(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r6 = "anr_watchdog_"
            r13.getBugreport(r6)     // Catch:{ all -> 0x01d1 }
            r13.mRequestWatchdogBugreport = r7     // Catch:{ all -> 0x01d1 }
        L_0x0051:
            boolean r6 = r13.mRequestAppCrashBugreport     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x0072
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x01d1 }
            r6.<init>()     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = "app_crash"
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = r13.mReportProcessName     // Catch:{ all -> 0x01d1 }
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r8 = "_"
            r6.append(r8)     // Catch:{ all -> 0x01d1 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01d1 }
            r13.getBugreport(r6)     // Catch:{ all -> 0x01d1 }
            r13.mRequestAppCrashBugreport = r7     // Catch:{ all -> 0x01d1 }
        L_0x0072:
            boolean r6 = r13.mRequestPeriodicBugreport     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x007d
            java.lang.String r6 = "Bugreport_"
            r13.getBugreport(r6)     // Catch:{ all -> 0x01d1 }
            r13.mRequestPeriodicBugreport = r7     // Catch:{ all -> 0x01d1 }
        L_0x007d:
            boolean r6 = r13.mRequestDumpsysMemInfo     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x0084
            r13.mRequestDumpsysMemInfo = r7     // Catch:{ all -> 0x01d1 }
            r3 = 1
        L_0x0084:
            boolean r6 = r13.mMonitorNativeCrashes     // Catch:{ all -> 0x01d1 }
            r8 = 1
            if (r6 == 0) goto L_0x00b3
            boolean r6 = r13.checkNativeCrashes()     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00b3
            if (r0 <= 0) goto L_0x00b3
            com.android.commands.monkey.Logger r6 = com.android.commands.monkey.Logger.out     // Catch:{ all -> 0x01d1 }
            java.lang.String r9 = "** New native crash detected."
            r6.println(r9)     // Catch:{ all -> 0x01d1 }
            boolean r6 = r13.mRequestBugreport     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00a1
            java.lang.String r6 = "native_crash_"
            r13.getBugreport(r6)     // Catch:{ all -> 0x01d1 }
        L_0x00a1:
            boolean r6 = r13.mAbort     // Catch:{ all -> 0x01d1 }
            if (r6 != 0) goto L_0x00b0
            boolean r6 = r13.mIgnoreNativeCrashes     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00b0
            boolean r6 = r13.mKillProcessAfterError     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00ae
            goto L_0x00b0
        L_0x00ae:
            r6 = r7
            goto L_0x00b1
        L_0x00b0:
            r6 = r8
        L_0x00b1:
            r13.mAbort = r6     // Catch:{ all -> 0x01d1 }
        L_0x00b3:
            boolean r6 = r13.mAbort     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00b8
            r4 = 1
        L_0x00b8:
            boolean r6 = r13.mWatchdogWaiting     // Catch:{ all -> 0x01d1 }
            if (r6 == 0) goto L_0x00c1
            r13.mWatchdogWaiting = r7     // Catch:{ all -> 0x01d1 }
            r13.notifyAll()     // Catch:{ all -> 0x01d1 }
        L_0x00c1:
            monitor-exit(r13)     // Catch:{ all -> 0x01d1 }
            if (r2 == 0) goto L_0x00c8
            r2 = 0
            r13.reportAnrTraces()     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x00c8:
            if (r3 == 0) goto L_0x00ce
            r3 = 0
            r13.reportDumpsysMemInfo()     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x00ce:
            if (r4 == 0) goto L_0x00ef
            r4 = 0
            com.android.commands.monkey.Logger r6 = com.android.commands.monkey.Logger.out     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r7 = "** Monkey aborted due to error."
            r6.println(r7)     // Catch:{ RuntimeException -> 0x01d4 }
            com.android.commands.monkey.Logger r6 = com.android.commands.monkey.Logger.out     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x01d4 }
            r7.<init>()     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r8 = "Events injected: "
            r7.append(r8)     // Catch:{ RuntimeException -> 0x01d4 }
            r7.append(r0)     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r7 = r7.toString()     // Catch:{ RuntimeException -> 0x01d4 }
            r6.println(r7)     // Catch:{ RuntimeException -> 0x01d4 }
            return r0
        L_0x00ef:
            boolean r6 = r13.mSendNoEvents     // Catch:{ RuntimeException -> 0x01d4 }
            if (r6 == 0) goto L_0x00f9
            int r0 = r0 + 1
            int r1 = r1 + 1
            goto L_0x0006
        L_0x00f9:
            int r6 = r13.mVerbose     // Catch:{ RuntimeException -> 0x01d4 }
            if (r6 <= 0) goto L_0x0148
            int r6 = r0 % 100
            if (r6 != 0) goto L_0x0148
            if (r0 == 0) goto L_0x0148
            long r6 = java.lang.System.currentTimeMillis()     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r6 = com.android.commands.monkey.MonkeyUtils.toCalendarTime(r6)     // Catch:{ RuntimeException -> 0x01d4 }
            long r9 = android.os.SystemClock.elapsedRealtime()     // Catch:{ RuntimeException -> 0x01d4 }
            com.android.commands.monkey.Logger r7 = com.android.commands.monkey.Logger.out     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x01d4 }
            r11.<init>()     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r12 = "    //[calendar_time:"
            r11.append(r12)     // Catch:{ RuntimeException -> 0x01d4 }
            r11.append(r6)     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r12 = " system_uptime:"
            r11.append(r12)     // Catch:{ RuntimeException -> 0x01d4 }
            r11.append(r9)     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r12 = "]"
            r11.append(r12)     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r11 = r11.toString()     // Catch:{ RuntimeException -> 0x01d4 }
            r7.println(r11)     // Catch:{ RuntimeException -> 0x01d4 }
            com.android.commands.monkey.Logger r7 = com.android.commands.monkey.Logger.out     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x01d4 }
            r11.<init>()     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r12 = "    // Sending event #"
            r11.append(r12)     // Catch:{ RuntimeException -> 0x01d4 }
            r11.append(r0)     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r11 = r11.toString()     // Catch:{ RuntimeException -> 0x01d4 }
            r7.println(r11)     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x0148:
            com.android.commands.monkey.MonkeyEventSource r6 = r13.mEventSource     // Catch:{ RuntimeException -> 0x01d4 }
            com.android.commands.monkey.MonkeyEvent r6 = r6.getNextEvent()     // Catch:{ RuntimeException -> 0x01d4 }
            if (r6 == 0) goto L_0x01b6
            android.view.IWindowManager r7 = r13.mWm     // Catch:{ RuntimeException -> 0x01d4 }
            android.app.IActivityManager r9 = r13.mAm     // Catch:{ RuntimeException -> 0x01d4 }
            int r10 = r13.mVerbose     // Catch:{ RuntimeException -> 0x01d4 }
            int r7 = r6.injectEvent(r7, r9, r10)     // Catch:{ RuntimeException -> 0x01d4 }
            if (r7 != 0) goto L_0x018d
            com.android.commands.monkey.Logger r8 = com.android.commands.monkey.Logger.out     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r9 = "    // Injection Failed"
            r8.println(r9)     // Catch:{ RuntimeException -> 0x01d4 }
            boolean r8 = r6 instanceof com.android.commands.monkey.MonkeyKeyEvent     // Catch:{ RuntimeException -> 0x01d4 }
            r9 = 1
            if (r8 == 0) goto L_0x016f
            long r11 = r13.mDroppedKeyEvents     // Catch:{ RuntimeException -> 0x01d4 }
            long r11 = r11 + r9
            r13.mDroppedKeyEvents = r11     // Catch:{ RuntimeException -> 0x01d4 }
            goto L_0x01a9
        L_0x016f:
            boolean r8 = r6 instanceof com.android.commands.monkey.MonkeyMotionEvent     // Catch:{ RuntimeException -> 0x01d4 }
            if (r8 == 0) goto L_0x0179
            long r11 = r13.mDroppedPointerEvents     // Catch:{ RuntimeException -> 0x01d4 }
            long r11 = r11 + r9
            r13.mDroppedPointerEvents = r11     // Catch:{ RuntimeException -> 0x01d4 }
            goto L_0x01a9
        L_0x0179:
            boolean r8 = r6 instanceof com.android.commands.monkey.MonkeyFlipEvent     // Catch:{ RuntimeException -> 0x01d4 }
            if (r8 == 0) goto L_0x0183
            long r11 = r13.mDroppedFlipEvents     // Catch:{ RuntimeException -> 0x01d4 }
            long r11 = r11 + r9
            r13.mDroppedFlipEvents = r11     // Catch:{ RuntimeException -> 0x01d4 }
            goto L_0x01a9
        L_0x0183:
            boolean r8 = r6 instanceof com.android.commands.monkey.MonkeyRotationEvent     // Catch:{ RuntimeException -> 0x01d4 }
            if (r8 == 0) goto L_0x01a9
            long r11 = r13.mDroppedRotationEvents     // Catch:{ RuntimeException -> 0x01d4 }
            long r11 = r11 + r9
            r13.mDroppedRotationEvents = r11     // Catch:{ RuntimeException -> 0x01d4 }
            goto L_0x01a9
        L_0x018d:
            r9 = -1
            if (r7 != r9) goto L_0x0199
            r5 = 1
            com.android.commands.monkey.Logger r8 = com.android.commands.monkey.Logger.err     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r9 = "** Error: RemoteException while injecting event."
            r8.println(r9)     // Catch:{ RuntimeException -> 0x01d4 }
            goto L_0x01a9
        L_0x0199:
            r9 = -2
            if (r7 != r9) goto L_0x01a9
            boolean r9 = r13.mIgnoreSecurityExceptions     // Catch:{ RuntimeException -> 0x01d4 }
            r8 = r8 ^ r9
            r5 = r8
            if (r5 == 0) goto L_0x01a9
            com.android.commands.monkey.Logger r8 = com.android.commands.monkey.Logger.err     // Catch:{ RuntimeException -> 0x01d4 }
            java.lang.String r9 = "** Error: SecurityException while injecting event."
            r8.println(r9)     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x01a9:
            boolean r8 = r6 instanceof com.android.commands.monkey.MonkeyThrottleEvent     // Catch:{ RuntimeException -> 0x01d4 }
            if (r8 != 0) goto L_0x01b5
            int r0 = r0 + 1
            boolean r8 = r13.mCountEvents     // Catch:{ RuntimeException -> 0x01d4 }
            if (r8 == 0) goto L_0x01b5
            int r1 = r1 + 1
        L_0x01b5:
            goto L_0x01cf
        L_0x01b6:
            boolean r7 = r13.mCountEvents     // Catch:{ RuntimeException -> 0x01d4 }
            if (r7 != 0) goto L_0x01db
            int r1 = r1 + 1
            r13.writeScriptLog(r1)     // Catch:{ RuntimeException -> 0x01d4 }
            boolean r7 = r13.mGetPeriodicBugreport     // Catch:{ RuntimeException -> 0x01d4 }
            if (r7 == 0) goto L_0x01cf
            long r9 = (long) r1     // Catch:{ RuntimeException -> 0x01d4 }
            long r11 = r13.mBugreportFrequency     // Catch:{ RuntimeException -> 0x01d4 }
            long r9 = r9 % r11
            r11 = 0
            int r7 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
            if (r7 != 0) goto L_0x01cf
            r13.mRequestPeriodicBugreport = r8     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x01cf:
            goto L_0x0006
        L_0x01d1:
            r6 = move-exception
            monitor-exit(r13)     // Catch:{ all -> 0x01d1 }
            throw r6     // Catch:{ RuntimeException -> 0x01d4 }
        L_0x01d4:
            r6 = move-exception
            java.lang.String r7 = "** Error: A RuntimeException occurred:"
            com.android.commands.monkey.Logger.error(r7, r6)
            goto L_0x01dc
        L_0x01db:
        L_0x01dc:
            com.android.commands.monkey.Logger r6 = com.android.commands.monkey.Logger.out
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Events injected: "
            r7.append(r8)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            r6.println(r7)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.Monkey.runMonkeyCycles():int");
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    private void signalPersistentProcesses() {
        try {
            this.mAm.signalPersistentProcesses(10);
            synchronized (this) {
                wait(2000);
            }
        } catch (RemoteException e) {
            Logger.err.println("** Failed talking with activity manager!");
        } catch (InterruptedException e2) {
        }
    }

    private boolean checkNativeCrashes() {
        String[] tombstones = TOMBSTONES_PATH.list();
        if (tombstones == null || tombstones.length == 0) {
            this.mTombstones = null;
            return false;
        }
        HashSet<Long> newStones = new HashSet<>();
        boolean result = false;
        for (String t : tombstones) {
            if (t.startsWith(TOMBSTONE_PREFIX)) {
                File f = new File(TOMBSTONES_PATH, t);
                newStones.add(Long.valueOf(f.lastModified()));
                HashSet<Long> hashSet = this.mTombstones;
                if (hashSet == null || !hashSet.contains(Long.valueOf(f.lastModified()))) {
                    result = true;
                    waitForTombstoneToBeWritten(Paths.get(TOMBSTONES_PATH.getPath(), new String[]{t}));
                    Logger.out.println("** New tombstone found: " + f.getAbsolutePath() + ", size: " + f.length());
                }
            }
        }
        this.mTombstones = newStones;
        return result;
    }

    private void waitForTombstoneToBeWritten(Path path) {
        boolean isWritten = false;
        int i = 0;
        while (true) {
            try {
                if (i >= NUM_READ_TOMBSTONE_RETRIES) {
                    break;
                }
                long size = Files.size(path);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                if (size > 0) {
                    if (Files.size(path) == size) {
                        isWritten = true;
                        break;
                    }
                }
                i++;
            } catch (IOException e2) {
                Logger logger = Logger.err;
                logger.println("Failed to get tombstone file size: " + e2.toString());
            }
        }
        if (!isWritten) {
            Logger.err.println("Incomplete tombstone file.");
        }
    }

    private String nextOption() {
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
            Logger.err.println("arg=\"" + arg + "\" mCurArgData=\"" + this.mCurArgData + "\" mNextArg=" + this.mNextArg + " argwas=\"" + this.mArgs[this.mNextArg - 1] + "\" nextarg=\"" + this.mArgs[this.mNextArg] + "\"");
            return arg;
        } else if (arg.length() > 2) {
            this.mCurArgData = arg.substring(2);
            return arg.substring(0, 2);
        } else {
            this.mCurArgData = null;
            return arg;
        }
    }

    private String nextOptionData() {
        String str = this.mCurArgData;
        if (str != null) {
            return str;
        }
        int i = this.mNextArg;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        String data = strArr[i];
        Logger logger = Logger.err;
        logger.println("data=\"" + data + "\"");
        this.mNextArg = this.mNextArg + 1;
        return data;
    }

    private long nextOptionLong(String opt) {
        try {
            return Long.parseLong(nextOptionData());
        } catch (NumberFormatException e) {
            Logger logger = Logger.err;
            logger.println("** Error: " + opt + " is not a number");
            throw e;
        }
    }

    private String nextArg() {
        int i = this.mNextArg;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        String arg = strArr[i];
        this.mNextArg = i + 1;
        return arg;
    }

    private void showUsage() {
        StringBuffer usage = new StringBuffer();
        usage.append("usage: monkey [-p ALLOWED_PACKAGE [-p ALLOWED_PACKAGE] ...]\n");
        usage.append("              [-c MAIN_CATEGORY [-c MAIN_CATEGORY] ...]\n");
        usage.append("              [--ignore-crashes] [--ignore-timeouts]\n");
        usage.append("              [--ignore-security-exceptions]\n");
        usage.append("              [--monitor-native-crashes] [--ignore-native-crashes]\n");
        usage.append("              [--kill-process-after-error] [--hprof]\n");
        usage.append("              [--match-description TEXT]\n");
        usage.append("              [--pct-touch PERCENT] [--pct-motion PERCENT]\n");
        usage.append("              [--pct-trackball PERCENT] [--pct-syskeys PERCENT]\n");
        usage.append("              [--pct-nav PERCENT] [--pct-majornav PERCENT]\n");
        usage.append("              [--pct-appswitch PERCENT] [--pct-flip PERCENT]\n");
        usage.append("              [--pct-anyevent PERCENT] [--pct-pinchzoom PERCENT]\n");
        usage.append("              [--pct-permission PERCENT]\n");
        usage.append("              [--pkg-blacklist-file PACKAGE_BLACKLIST_FILE]\n");
        usage.append("              [--pkg-whitelist-file PACKAGE_WHITELIST_FILE]\n");
        usage.append("              [--wait-dbg] [--dbg-no-events]\n");
        usage.append("              [--setup scriptfile] [-f scriptfile [-f scriptfile] ...]\n");
        usage.append("              [--port port]\n");
        usage.append("              [-s SEED] [-v [-v] ...]\n");
        usage.append("              [--throttle MILLISEC] [--randomize-throttle]\n");
        usage.append("              [--profile-wait MILLISEC]\n");
        usage.append("              [--device-sleep-time MILLISEC]\n");
        usage.append("              [--randomize-script]\n");
        usage.append("              [--script-log]\n");
        usage.append("              [--bugreport]\n");
        usage.append("              [--periodic-bugreport]\n");
        usage.append("              [--permission-target-system]\n");
        usage.append("              COUNT\n");
        Logger.err.println(usage.toString());
    }
}
