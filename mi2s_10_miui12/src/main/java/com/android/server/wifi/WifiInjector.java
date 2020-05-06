package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.hardware.SystemSensorManager;
import android.net.IpMemoryStore;
import android.net.NetworkCapabilities;
import android.net.NetworkScoreManager;
import android.net.wifi.IWifiScanner;
import android.net.wifi.IWificond;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.security.KeyStore;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.PowerProfile;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.BatteryStatsService;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.NetworkRequestStoreData;
import com.android.server.wifi.NetworkSuggestionStoreData;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.aware.WifiAwareMetrics;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.hotspot2.PasspointObjectFactory;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.p2p.WifiP2pMetrics;
import com.android.server.wifi.p2p.WifiP2pMonitor;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.rtt.RttMetrics;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import java.util.Random;
import miui.app.constants.ThemeManagerConstants;

public class WifiInjector {
    private static final String BOOT_DEFAULT_WIFI_COUNTRY_CODE = "ro.boot.wificountrycode";
    private static final String WIFICOND_SERVICE_NAME = "wificond";
    static WifiInjector sWifiInjector = null;
    private final ActiveModeWarden mActiveModeWarden;
    private final BackupManagerProxy mBackupManagerProxy = new BackupManagerProxy();
    private final IBatteryStats mBatteryStats;
    private final BuildProperties mBuildProperties = new SystemBuildProperties();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final CarrierNetworkEvaluator mCarrierNetworkEvaluator;
    private CarrierNetworkNotifier mCarrierNetworkNotifier;
    private final CellularLinkLayerStatsCollector mCellularLinkLayerStatsCollector;
    private final ClientModeImpl mClientModeImpl;
    private final Clock mClock = new Clock();
    private final LocalLog mConnectivityLocalLog;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final DppManager mDppManager;
    private final DppMetrics mDppMetrics;
    private final FrameworkFacade mFrameworkFacade = new FrameworkFacade();
    private HalDeviceManager mHalDeviceManager;
    private final HostapdHal mHostapdHal;
    private final IpMemoryStore mIpMemoryStore;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final LinkProbeManager mLinkProbeManager;
    private final WifiLockManager mLockManager;
    private final NetworkScoreManager mNetworkScoreManager;
    private final NetworkSuggestionEvaluator mNetworkSuggestionEvaluator;
    private final INetworkManagementService mNwManagementService;
    private OpenNetworkNotifier mOpenNetworkNotifier;
    private final PasspointManager mPasspointManager;
    private final PasspointNetworkEvaluator mPasspointNetworkEvaluator;
    private final PropertyService mPropertyService = new SystemPropertyService();
    private HandlerThread mRttHandlerThread;
    private final SarManager mSarManager;
    private final SavedNetworkEvaluator mSavedNetworkEvaluator;
    private final ScanRequestProxy mScanRequestProxy;
    private final ScoredNetworkEvaluator mScoredNetworkEvaluator;
    private final ScoringParams mScoringParams;
    private final SelfRecovery mSelfRecovery;
    private final WifiSettingsStore mSettingsStore;
    private final SIMAccessor mSimAccessor;
    private final SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private final WakeupController mWakeupController;
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiAutoConnController mWifiAutoConnController;
    private HandlerThread mWifiAwareHandlerThread;
    private final WifiBackupRestore mWifiBackupRestore;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConnectivityHelper mWifiConnectivityHelper;
    private final WifiController mWifiController;
    private final HandlerThread mWifiCoreHandlerThread;
    private final WifiDataStall mWifiDataStall;
    private final BaseWifiDiagnostics mWifiDiagnostics;
    private final WifiKeyStore mWifiKeyStore;
    private WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private final WifiNative mWifiNative;
    private final WifiNetworkScoreCache mWifiNetworkScoreCache;
    private final WifiNetworkSelector mWifiNetworkSelector;
    private final WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private final WifiP2pMetrics mWifiP2pMetrics;
    private final WifiP2pMonitor mWifiP2pMonitor;
    private final WifiP2pNative mWifiP2pNative;
    private final HandlerThread mWifiP2pServiceHandlerThread;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiScanner mWifiScanner;
    private final WifiScoreCard mWifiScoreCard;
    private final HandlerThread mWifiServiceHandlerThread;
    private final WifiStateTracker mWifiStateTracker;
    private final WifiTrafficPoller mWifiTrafficPoller;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;

    public WifiInjector(Context context) {
        Context context2 = context;
        if (context2 == null) {
            throw new IllegalStateException("WifiInjector should not be initialized with a null Context.");
        } else if (sWifiInjector == null) {
            sWifiInjector = this;
            this.mContext = context2;
            this.mWifiScoreCard = new WifiScoreCard(this.mClock, Settings.Secure.getString(this.mContext.getContentResolver(), "android_id"));
            this.mSettingsStore = new WifiSettingsStore(this.mContext);
            this.mWifiPermissionsWrapper = new WifiPermissionsWrapper(this.mContext);
            this.mNetworkScoreManager = (NetworkScoreManager) this.mContext.getSystemService(NetworkScoreManager.class);
            this.mWifiNetworkScoreCache = new WifiNetworkScoreCache(this.mContext);
            this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mWifiNetworkScoreCache, 0);
            WifiPermissionsWrapper wifiPermissionsWrapper = this.mWifiPermissionsWrapper;
            Context context3 = this.mContext;
            this.mWifiPermissionsUtil = new WifiPermissionsUtil(wifiPermissionsWrapper, context3, UserManager.get(context3), this);
            this.mWifiBackupRestore = new WifiBackupRestore(this.mWifiPermissionsUtil);
            this.mBatteryStats = IBatteryStats.Stub.asInterface(this.mFrameworkFacade.getService("batterystats"));
            this.mWifiStateTracker = new WifiStateTracker(this.mBatteryStats);
            this.mWifiServiceHandlerThread = new HandlerThread("WifiService");
            this.mWifiServiceHandlerThread.start();
            this.mWifiCoreHandlerThread = new HandlerThread("ClientModeImpl");
            this.mWifiCoreHandlerThread.start();
            this.mWifiP2pServiceHandlerThread = new HandlerThread("WifiP2pService");
            this.mWifiP2pServiceHandlerThread.start();
            Looper clientModeImplLooper = this.mWifiCoreHandlerThread.getLooper();
            this.mCarrierNetworkConfig = new CarrierNetworkConfig(this.mContext, clientModeImplLooper, this.mFrameworkFacade);
            WifiAwareMetrics awareMetrics = new WifiAwareMetrics(this.mClock);
            RttMetrics rttMetrics = new RttMetrics(this.mClock);
            this.mWifiP2pMetrics = new WifiP2pMetrics(this.mClock);
            this.mDppMetrics = new DppMetrics();
            this.mCellularLinkLayerStatsCollector = new CellularLinkLayerStatsCollector(this.mContext);
            this.mWifiMetrics = new WifiMetrics(this.mContext, this.mFrameworkFacade, this.mClock, clientModeImplLooper, awareMetrics, rttMetrics, new WifiPowerMetrics(), this.mWifiP2pMetrics, this.mDppMetrics, this.mCellularLinkLayerStatsCollector);
            this.mWifiMonitor = new WifiMonitor(this);
            this.mHalDeviceManager = new HalDeviceManager(this.mClock);
            this.mWifiVendorHal = new WifiVendorHal(this.mHalDeviceManager, this.mWifiCoreHandlerThread.getLooper());
            this.mSupplicantStaIfaceHal = new SupplicantStaIfaceHal(this.mContext, this.mWifiMonitor, this.mPropertyService, clientModeImplLooper);
            this.mHostapdHal = new HostapdHal(this.mContext, clientModeImplLooper);
            this.mWificondControl = new WificondControl(this, this.mWifiMonitor, this.mCarrierNetworkConfig, (AlarmManager) this.mContext.getSystemService(ThemeManagerConstants.COMPONENT_CODE_ALARM), clientModeImplLooper, this.mClock);
            this.mNwManagementService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            this.mWifiNative = new WifiNative(this.mWifiVendorHal, this.mSupplicantStaIfaceHal, this.mHostapdHal, this.mWificondControl, this.mWifiMonitor, this.mNwManagementService, this.mPropertyService, this.mWifiMetrics, new Handler(this.mWifiCoreHandlerThread.getLooper()), new Random());
            this.mWifiP2pMonitor = new WifiP2pMonitor(this);
            this.mSupplicantP2pIfaceHal = new SupplicantP2pIfaceHal(this.mWifiP2pMonitor);
            this.mWifiP2pNative = new WifiP2pNative(this.mWifiVendorHal, this.mSupplicantP2pIfaceHal, this.mHalDeviceManager, this.mPropertyService);
            this.mWifiTrafficPoller = new WifiTrafficPoller(clientModeImplLooper);
            this.mCountryCode = new WifiCountryCode(this.mContext, this.mWifiNative, SystemProperties.get(BOOT_DEFAULT_WIFI_COUNTRY_CODE), this.mContext.getResources().getBoolean(17891600));
            this.mWifiApConfigStore = new WifiApConfigStore(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mBackupManagerProxy, this.mFrameworkFacade);
            this.mWifiKeyStore = new WifiKeyStore(this.mKeyStore);
            this.mWifiConfigStore = new WifiConfigStore(this.mContext, clientModeImplLooper, this.mClock, this.mWifiMetrics, WifiConfigStore.createSharedFile());
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
            Context context4 = this.mContext;
            WifiConfigManager wifiConfigManager = r0;
            Looper clientModeImplLooper2 = clientModeImplLooper;
            WifiConfigManager wifiConfigManager2 = new WifiConfigManager(context4, this.mClock, UserManager.get(context4), makeTelephonyManager(), this.mWifiKeyStore, this.mWifiConfigStore, this.mWifiPermissionsUtil, this.mWifiPermissionsWrapper, this, new NetworkListSharedStoreData(this.mContext), new NetworkListUserStoreData(this.mContext), new DeletedEphemeralSsidsStoreData(this.mClock), new RandomizedMacStoreData(), this.mFrameworkFacade, this.mWifiCoreHandlerThread.getLooper());
            this.mWifiConfigManager = wifiConfigManager;
            this.mWifiMetrics.setWifiConfigManager(this.mWifiConfigManager);
            this.mWifiConnectivityHelper = new WifiConnectivityHelper(this.mWifiNative);
            this.mConnectivityLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 256 : 512);
            Looper clientModeImplLooper3 = clientModeImplLooper2;
            this.mScoringParams = new ScoringParams(this.mContext, this.mFrameworkFacade, new Handler(clientModeImplLooper3));
            this.mWifiMetrics.setScoringParams(this.mScoringParams);
            this.mWifiNetworkSelector = new WifiNetworkSelector(this.mContext, this.mWifiScoreCard, this.mScoringParams, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog, this.mWifiMetrics, this.mWifiNative);
            CompatibilityScorer compatibilityScorer = new CompatibilityScorer(this.mScoringParams);
            this.mWifiNetworkSelector.registerCandidateScorer(compatibilityScorer);
            ScoreCardBasedScorer scoreCardBasedScorer = new ScoreCardBasedScorer(this.mScoringParams);
            this.mWifiNetworkSelector.registerCandidateScorer(scoreCardBasedScorer);
            BubbleFunScorer bubbleFunScorer = new BubbleFunScorer(this.mScoringParams);
            this.mWifiNetworkSelector.registerCandidateScorer(bubbleFunScorer);
            this.mWifiMetrics.setWifiNetworkSelector(this.mWifiNetworkSelector);
            this.mSavedNetworkEvaluator = new SavedNetworkEvaluator(this.mContext, this.mScoringParams, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog, this.mWifiConnectivityHelper, subscriptionManager);
            this.mWifiNetworkSuggestionsManager = new WifiNetworkSuggestionsManager(this.mContext, new Handler(this.mWifiCoreHandlerThread.getLooper()), this, this.mWifiPermissionsUtil, this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiMetrics);
            this.mNetworkSuggestionEvaluator = new NetworkSuggestionEvaluator(this.mWifiNetworkSuggestionsManager, this.mWifiConfigManager, this.mConnectivityLocalLog);
            this.mScoredNetworkEvaluator = new ScoredNetworkEvaluator(context, clientModeImplLooper3, this.mFrameworkFacade, this.mNetworkScoreManager, this.mWifiConfigManager, this.mConnectivityLocalLog, this.mWifiNetworkScoreCache, this.mWifiPermissionsUtil);
            this.mCarrierNetworkEvaluator = new CarrierNetworkEvaluator(this.mWifiConfigManager, this.mCarrierNetworkConfig, this.mConnectivityLocalLog, this);
            this.mSimAccessor = new SIMAccessor(this.mContext);
            PasspointManager passpointManager = r0;
            BubbleFunScorer bubbleFunScorer2 = bubbleFunScorer;
            ScoreCardBasedScorer scoreCardBasedScorer2 = scoreCardBasedScorer;
            CompatibilityScorer compatibilityScorer2 = compatibilityScorer;
            PasspointManager passpointManager2 = new PasspointManager(this.mContext, this, new Handler(this.mWifiCoreHandlerThread.getLooper()), this.mWifiNative, this.mWifiKeyStore, this.mClock, this.mSimAccessor, new PasspointObjectFactory(), this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiMetrics, makeTelephonyManager(), subscriptionManager);
            this.mPasspointManager = passpointManager;
            this.mPasspointNetworkEvaluator = new PasspointNetworkEvaluator(this.mPasspointManager, this.mWifiConfigManager, this.mConnectivityLocalLog, this.mCarrierNetworkConfig, this, subscriptionManager);
            this.mWifiMetrics.setPasspointManager(this.mPasspointManager);
            Context context5 = this.mContext;
            Looper clientModeImplLooper4 = clientModeImplLooper2;
            this.mScanRequestProxy = new ScanRequestProxy(context5, (AppOpsManager) context5.getSystemService("appops"), (ActivityManager) this.mContext.getSystemService("activity"), this, this.mWifiConfigManager, this.mWifiPermissionsUtil, this.mWifiMetrics, this.mClock, this.mFrameworkFacade, new Handler(clientModeImplLooper4));
            this.mSarManager = new SarManager(this.mContext, makeTelephonyManager(), clientModeImplLooper4, this.mWifiNative, new SystemSensorManager(this.mContext, clientModeImplLooper4), this.mWifiMetrics);
            this.mWifiDiagnostics = new WifiDiagnostics(this.mContext, this, this.mWifiNative, this.mBuildProperties, new LastMileLogger(this), this.mClock);
            this.mWifiDataStall = new WifiDataStall(this.mContext, this.mFrameworkFacade, this.mWifiMetrics);
            this.mWifiMetrics.setWifiDataStall(this.mWifiDataStall);
            this.mLinkProbeManager = new LinkProbeManager(this.mClock, this.mWifiNative, this.mWifiMetrics, this.mFrameworkFacade, this.mWifiCoreHandlerThread.getLooper(), this.mContext);
            Context context6 = this.mContext;
            Looper looper = clientModeImplLooper4;
            this.mClientModeImpl = new ClientModeImpl(context6, this.mFrameworkFacade, looper, UserManager.get(context6), this, this.mBackupManagerProxy, this.mCountryCode, this.mWifiNative, new WrongPasswordNotifier(this.mContext, this.mFrameworkFacade), this.mSarManager, this.mWifiTrafficPoller, this.mLinkProbeManager);
            Context context7 = this.mContext;
            this.mActiveModeWarden = new ActiveModeWarden(this, context7, looper, this.mWifiNative, new DefaultModeManager(context7, clientModeImplLooper4), this.mBatteryStats);
            Context context8 = this.mContext;
            WifiConfigManager wifiConfigManager3 = this.mWifiConfigManager;
            Context context9 = context8;
            WifiConfigManager wifiConfigManager4 = wifiConfigManager3;
            this.mWakeupController = new WakeupController(this.mContext, this.mWifiCoreHandlerThread.getLooper(), new WakeupLock(this.mWifiConfigManager, this.mWifiMetrics.getWakeupMetrics(), this.mClock), new WakeupEvaluator(this.mScoringParams), new WakeupOnboarding(context9, wifiConfigManager4, this.mWifiCoreHandlerThread.getLooper(), this.mFrameworkFacade, new WakeupNotificationFactory(this.mContext, this.mFrameworkFacade)), this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiNetworkSuggestionsManager, this.mWifiMetrics.getWakeupMetrics(), this, this.mFrameworkFacade, this.mClock);
            ClientModeImpl clientModeImpl = this.mClientModeImpl;
            if (clientModeImpl != null) {
                clientModeImpl.setWifiDiagnostics(this.mWifiDiagnostics);
            }
            this.mLockManager = new WifiLockManager(this.mContext, BatteryStatsService.getService(), this.mClientModeImpl, this.mFrameworkFacade, new Handler(clientModeImplLooper4), this.mWifiNative, this.mClock, this.mWifiMetrics);
            Looper looper2 = clientModeImplLooper4;
            this.mWifiController = new WifiController(this.mContext, this.mClientModeImpl, clientModeImplLooper4, this.mSettingsStore, this.mWifiServiceHandlerThread.getLooper(), this.mFrameworkFacade, this.mActiveModeWarden, this.mWifiPermissionsUtil);
            this.mSelfRecovery = new SelfRecovery(this.mWifiController, this.mClock);
            this.mWifiMulticastLockManager = new WifiMulticastLockManager(this.mClientModeImpl.getMcastLockManagerFilterController(), BatteryStatsService.getService());
            this.mDppManager = new DppManager(this.mWifiCoreHandlerThread.getLooper(), this.mWifiNative, this.mWifiConfigManager, this.mContext, this.mDppMetrics);
            this.mIpMemoryStore = IpMemoryStore.getMemoryStore(this.mContext);
            this.mWifiNetworkSelector.registerNetworkEvaluator(this.mSavedNetworkEvaluator);
            this.mWifiNetworkSelector.registerNetworkEvaluator(this.mNetworkSuggestionEvaluator);
            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                this.mWifiNetworkSelector.registerNetworkEvaluator(this.mPasspointNetworkEvaluator);
            }
            this.mWifiNetworkSelector.registerNetworkEvaluator(this.mCarrierNetworkEvaluator);
            this.mWifiNetworkSelector.registerNetworkEvaluator(this.mScoredNetworkEvaluator);
            this.mWifiAutoConnController = new WifiAutoConnController(this.mWifiConfigManager, this.mContext, this.mClientModeImpl.getHandler());
            this.mClientModeImpl.start();
        } else {
            throw new IllegalStateException("WifiInjector was already created, use getInstance instead.");
        }
    }

    public static WifiInjector getInstance() {
        WifiInjector wifiInjector = sWifiInjector;
        if (wifiInjector != null) {
            return wifiInjector;
        }
        throw new IllegalStateException("Attempted to retrieve a WifiInjector instance before constructor was called.");
    }

    public void enableVerboseLogging(int verbose) {
        this.mWifiLastResortWatchdog.enableVerboseLogging(verbose);
        this.mWifiBackupRestore.enableVerboseLogging(verbose);
        this.mHalDeviceManager.enableVerboseLogging(verbose);
        this.mScanRequestProxy.enableVerboseLogging(verbose);
        this.mWakeupController.enableVerboseLogging(verbose);
        this.mCarrierNetworkConfig.enableVerboseLogging(verbose);
        this.mWifiNetworkSuggestionsManager.enableVerboseLogging(verbose);
        LogcatLog.enableVerboseLogging(verbose);
        this.mDppManager.enableVerboseLogging(verbose);
    }

    public UserManager getUserManager() {
        return UserManager.get(this.mContext);
    }

    public WifiMetrics getWifiMetrics() {
        return this.mWifiMetrics;
    }

    public WifiP2pMetrics getWifiP2pMetrics() {
        return this.mWifiP2pMetrics;
    }

    public SupplicantStaIfaceHal getSupplicantStaIfaceHal() {
        return this.mSupplicantStaIfaceHal;
    }

    public HostapdHal getHostadpHal() {
        return this.mHostapdHal;
    }

    public BackupManagerProxy getBackupManagerProxy() {
        return this.mBackupManagerProxy;
    }

    public FrameworkFacade getFrameworkFacade() {
        return this.mFrameworkFacade;
    }

    public HandlerThread getWifiServiceHandlerThread() {
        return this.mWifiServiceHandlerThread;
    }

    public HandlerThread getWifiP2pServiceHandlerThread() {
        return this.mWifiP2pServiceHandlerThread;
    }

    public HandlerThread getWifiCoreHandlerThread() {
        return this.mWifiCoreHandlerThread;
    }

    public WifiTrafficPoller getWifiTrafficPoller() {
        return this.mWifiTrafficPoller;
    }

    public WifiCountryCode getWifiCountryCode() {
        return this.mCountryCode;
    }

    public WifiApConfigStore getWifiApConfigStore() {
        return this.mWifiApConfigStore;
    }

    public SarManager getSarManager() {
        return this.mSarManager;
    }

    public ClientModeImpl getClientModeImpl() {
        return this.mClientModeImpl;
    }

    public Handler getClientModeImplHandler() {
        return this.mClientModeImpl.getHandler();
    }

    public ActiveModeWarden getActiveModeWarden() {
        return this.mActiveModeWarden;
    }

    public WifiSettingsStore getWifiSettingsStore() {
        return this.mSettingsStore;
    }

    public WifiLockManager getWifiLockManager() {
        return this.mLockManager;
    }

    public WifiController getWifiController() {
        return this.mWifiController;
    }

    public WifiLastResortWatchdog getWifiLastResortWatchdog() {
        return this.mWifiLastResortWatchdog;
    }

    public Clock getClock() {
        return this.mClock;
    }

    public PropertyService getPropertyService() {
        return this.mPropertyService;
    }

    public BuildProperties getBuildProperties() {
        return this.mBuildProperties;
    }

    public KeyStore getKeyStore() {
        return this.mKeyStore;
    }

    public WifiBackupRestore getWifiBackupRestore() {
        return this.mWifiBackupRestore;
    }

    public WifiMulticastLockManager getWifiMulticastLockManager() {
        return this.mWifiMulticastLockManager;
    }

    public WifiConfigManager getWifiConfigManager() {
        return this.mWifiConfigManager;
    }

    public PasspointManager getPasspointManager() {
        return this.mPasspointManager;
    }

    public CarrierNetworkConfig getCarrierNetworkConfig() {
        return this.mCarrierNetworkConfig;
    }

    public WakeupController getWakeupController() {
        return this.mWakeupController;
    }

    public ScoringParams getScoringParams() {
        return this.mScoringParams;
    }

    public WifiScoreCard getWifiScoreCard() {
        return this.mWifiScoreCard;
    }

    public TelephonyManager makeTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public WifiStateTracker getWifiStateTracker() {
        return this.mWifiStateTracker;
    }

    public DppManager getDppManager() {
        return this.mDppManager;
    }

    public IWificond makeWificond() {
        return IWificond.Stub.asInterface(ServiceManager.getService(WIFICOND_SERVICE_NAME));
    }

    public SoftApManager makeSoftApManager(WifiManager.SoftApCallback callback, SoftApModeConfiguration config) {
        return new SoftApManager(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mFrameworkFacade, this.mWifiNative, this.mCountryCode.getCountryCode(), callback, this.mWifiApConfigStore, config, this.mWifiMetrics, this.mSarManager);
    }

    public ScanOnlyModeManager makeScanOnlyModeManager(ScanOnlyModeManager.Listener listener) {
        return new ScanOnlyModeManager(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mWifiNative, listener, this.mWifiMetrics, this.mWakeupController, this.mSarManager);
    }

    public ClientModeManager makeClientModeManager(ClientModeManager.Listener listener) {
        return new ClientModeManager(this.mContext, this.mWifiCoreHandlerThread.getLooper(), this.mWifiNative, listener, this.mWifiMetrics, this.mClientModeImpl);
    }

    public WifiLog makeLog(String tag) {
        return new LogcatLog(tag);
    }

    public BaseWifiDiagnostics getWifiDiagnostics() {
        return this.mWifiDiagnostics;
    }

    public synchronized WifiScanner getWifiScanner() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = new WifiScanner(this.mContext, IWifiScanner.Stub.asInterface(ServiceManager.getService("wifiscanner")), this.mWifiCoreHandlerThread.getLooper());
        }
        return this.mWifiScanner;
    }

    public WifiConnectivityManager makeWifiConnectivityManager(ClientModeImpl clientModeImpl) {
        ClientModeImpl clientModeImpl2 = clientModeImpl;
        Context context = this.mContext;
        Looper looper = this.mWifiCoreHandlerThread.getLooper();
        FrameworkFacade frameworkFacade = this.mFrameworkFacade;
        this.mOpenNetworkNotifier = new OpenNetworkNotifier(context, looper, frameworkFacade, this.mClock, this.mWifiMetrics, this.mWifiConfigManager, this.mWifiConfigStore, clientModeImpl, new ConnectToNetworkNotificationBuilder(this.mContext, frameworkFacade));
        Context context2 = this.mContext;
        Looper looper2 = this.mWifiCoreHandlerThread.getLooper();
        FrameworkFacade frameworkFacade2 = this.mFrameworkFacade;
        this.mCarrierNetworkNotifier = new CarrierNetworkNotifier(context2, looper2, frameworkFacade2, this.mClock, this.mWifiMetrics, this.mWifiConfigManager, this.mWifiConfigStore, clientModeImpl, new ConnectToNetworkNotificationBuilder(this.mContext, frameworkFacade2));
        this.mWifiLastResortWatchdog = new WifiLastResortWatchdog(this, this.mClock, this.mWifiMetrics, clientModeImpl, clientModeImpl.getHandler().getLooper());
        return new WifiConnectivityManager(this.mContext, getScoringParams(), clientModeImpl2, this, this.mWifiConfigManager, clientModeImpl.getWifiInfo(), this.mWifiNetworkSelector, this.mWifiConnectivityHelper, this.mWifiLastResortWatchdog, this.mOpenNetworkNotifier, this.mCarrierNetworkNotifier, this.mCarrierNetworkConfig, this.mWifiMetrics, this.mWifiCoreHandlerThread.getLooper(), this.mClock, this.mConnectivityLocalLog);
    }

    public WifiNetworkFactory makeWifiNetworkFactory(NetworkCapabilities nc, WifiConnectivityManager wifiConnectivityManager) {
        Looper looper = this.mWifiCoreHandlerThread.getLooper();
        Context context = this.mContext;
        return new WifiNetworkFactory(looper, context, nc, (ActivityManager) context.getSystemService("activity"), (AlarmManager) this.mContext.getSystemService(ThemeManagerConstants.COMPONENT_CODE_ALARM), (AppOpsManager) this.mContext.getSystemService("appops"), this.mClock, this, wifiConnectivityManager, this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiPermissionsUtil, this.mWifiMetrics);
    }

    public NetworkRequestStoreData makeNetworkRequestStoreData(NetworkRequestStoreData.DataSource dataSource) {
        return new NetworkRequestStoreData(dataSource);
    }

    public UntrustedWifiNetworkFactory makeUntrustedWifiNetworkFactory(NetworkCapabilities nc, WifiConnectivityManager wifiConnectivityManager) {
        return new UntrustedWifiNetworkFactory(this.mWifiCoreHandlerThread.getLooper(), this.mContext, nc, wifiConnectivityManager);
    }

    public NetworkSuggestionStoreData makeNetworkSuggestionStoreData(NetworkSuggestionStoreData.DataSource dataSource) {
        return new NetworkSuggestionStoreData(dataSource);
    }

    public WifiPermissionsUtil getWifiPermissionsUtil() {
        return this.mWifiPermissionsUtil;
    }

    public WifiPermissionsWrapper getWifiPermissionsWrapper() {
        return this.mWifiPermissionsWrapper;
    }

    public HandlerThread getWifiAwareHandlerThread() {
        if (this.mWifiAwareHandlerThread == null) {
            this.mWifiAwareHandlerThread = new HandlerThread("wifiAwareService");
            this.mWifiAwareHandlerThread.start();
        }
        return this.mWifiAwareHandlerThread;
    }

    public HandlerThread getRttHandlerThread() {
        if (this.mRttHandlerThread == null) {
            this.mRttHandlerThread = new HandlerThread("wifiRttService");
            this.mRttHandlerThread.start();
        }
        return this.mRttHandlerThread;
    }

    public HalDeviceManager getHalDeviceManager() {
        return this.mHalDeviceManager;
    }

    public WifiNative getWifiNative() {
        return this.mWifiNative;
    }

    public WifiMonitor getWifiMonitor() {
        return this.mWifiMonitor;
    }

    public WifiP2pNative getWifiP2pNative() {
        return this.mWifiP2pNative;
    }

    public WifiP2pMonitor getWifiP2pMonitor() {
        return this.mWifiP2pMonitor;
    }

    public SelfRecovery getSelfRecovery() {
        return this.mSelfRecovery;
    }

    public PowerProfile getPowerProfile() {
        return new PowerProfile(this.mContext, false);
    }

    public ScanRequestProxy getScanRequestProxy() {
        return this.mScanRequestProxy;
    }

    public Runtime getJavaRuntime() {
        return Runtime.getRuntime();
    }

    public ActivityManagerService getActivityManagerService() {
        return ActivityManager.getService();
    }

    public WifiDataStall getWifiDataStall() {
        return this.mWifiDataStall;
    }

    public WifiNetworkSuggestionsManager getWifiNetworkSuggestionsManager() {
        return this.mWifiNetworkSuggestionsManager;
    }

    public IpMemoryStore getIpMemoryStore() {
        return this.mIpMemoryStore;
    }

    public WifiAutoConnController getWifiAutoConnController() {
        return this.mWifiAutoConnController;
    }
}
