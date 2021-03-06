package com.android.server.broadcastradio.hal2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.broadcastradio.V2_0.AmFmRegionConfig;
import android.hardware.broadcastradio.V2_0.Announcement;
import android.hardware.broadcastradio.V2_0.DabTableEntry;
import android.hardware.broadcastradio.V2_0.IAnnouncementListener;
import android.hardware.broadcastradio.V2_0.IBroadcastRadio;
import android.hardware.broadcastradio.V2_0.ITunerCallback;
import android.hardware.broadcastradio.V2_0.ITunerSession;
import android.hardware.broadcastradio.V2_0.ProgramInfo;
import android.hardware.broadcastradio.V2_0.ProgramListChunk;
import android.hardware.broadcastradio.V2_0.ProgramSelector;
import android.hardware.broadcastradio.V2_0.VendorKeyValue;
import android.hardware.radio.IAnnouncementListener;
import android.hardware.radio.ICloseHandle;
import android.hardware.radio.RadioManager;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.MutableInt;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.broadcastradio.hal2.Utils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class RadioModule {
    private static final String TAG = "BcRadio2Srv.module";
    @GuardedBy({"mLock"})
    private final Set<TunerSession> mAidlTunerSessions = new HashSet();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public Boolean mAntennaConnected = null;
    private final ITunerCallback mHalTunerCallback = new ITunerCallback.Stub() {
        public void onTuneFailed(int result, ProgramSelector programSelector) {
            RadioModule.this.fanoutAidlCallback(new AidlCallbackRunnable(result, programSelector) {
                private final /* synthetic */ int f$0;
                private final /* synthetic */ ProgramSelector f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void run(android.hardware.radio.ITunerCallback iTunerCallback) {
                    iTunerCallback.onTuneFailed(this.f$0, Convert.programSelectorFromHal(this.f$1));
                }
            });
        }

        public void onCurrentProgramInfoChanged(ProgramInfo halProgramInfo) {
            RadioManager.ProgramInfo programInfo = Convert.programInfoFromHal(halProgramInfo);
            synchronized (RadioModule.this.mLock) {
                RadioManager.ProgramInfo unused = RadioModule.this.mProgramInfo = programInfo;
                RadioModule.this.fanoutAidlCallbackLocked(new AidlCallbackRunnable(programInfo) {
                    private final /* synthetic */ RadioManager.ProgramInfo f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run(android.hardware.radio.ITunerCallback iTunerCallback) {
                        iTunerCallback.onCurrentProgramInfoChanged(this.f$0);
                    }
                });
            }
        }

        public void onProgramListUpdated(ProgramListChunk programListChunk) {
            RadioModule.this.fanoutAidlCallback(new AidlCallbackRunnable() {
                public final void run(android.hardware.radio.ITunerCallback iTunerCallback) {
                    iTunerCallback.onProgramListUpdated(Convert.programListChunkFromHal(ProgramListChunk.this));
                }
            });
        }

        public void onAntennaStateChange(boolean connected) {
            synchronized (RadioModule.this.mLock) {
                Boolean unused = RadioModule.this.mAntennaConnected = Boolean.valueOf(connected);
                RadioModule.this.fanoutAidlCallbackLocked(new AidlCallbackRunnable(connected) {
                    private final /* synthetic */ boolean f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run(android.hardware.radio.ITunerCallback iTunerCallback) {
                        iTunerCallback.onAntennaState(this.f$0);
                    }
                });
            }
        }

        public void onParametersUpdated(ArrayList<VendorKeyValue> parameters) {
            RadioModule.this.fanoutAidlCallback(new AidlCallbackRunnable(parameters) {
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final void run(android.hardware.radio.ITunerCallback iTunerCallback) {
                    iTunerCallback.onParametersUpdated(Convert.vendorInfoFromHal(this.f$0));
                }
            });
        }
    };
    @GuardedBy({"mLock"})
    private ITunerSession mHalTunerSession;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public RadioManager.ProgramInfo mProgramInfo = null;
    public final RadioManager.ModuleProperties mProperties;
    private final IBroadcastRadio mService;

    interface AidlCallbackRunnable {
        void run(android.hardware.radio.ITunerCallback iTunerCallback) throws RemoteException;
    }

    private RadioModule(IBroadcastRadio service, RadioManager.ModuleProperties properties) throws RemoteException {
        this.mProperties = (RadioManager.ModuleProperties) Objects.requireNonNull(properties);
        this.mService = (IBroadcastRadio) Objects.requireNonNull(service);
    }

    public static RadioModule tryLoadingModule(int idx, String fqName) {
        try {
            IBroadcastRadio service = IBroadcastRadio.getService(fqName);
            if (service == null) {
                return null;
            }
            Mutable<AmFmRegionConfig> amfmConfig = new Mutable<>();
            service.getAmFmRegionConfig(false, new IBroadcastRadio.getAmFmRegionConfigCallback() {
                public final void onValues(int i, AmFmRegionConfig amFmRegionConfig) {
                    RadioModule.lambda$tryLoadingModule$0(Mutable.this, i, amFmRegionConfig);
                }
            });
            Mutable<List<DabTableEntry>> dabConfig = new Mutable<>();
            service.getDabRegionConfig(new IBroadcastRadio.getDabRegionConfigCallback() {
                public final void onValues(int i, ArrayList arrayList) {
                    RadioModule.lambda$tryLoadingModule$1(Mutable.this, i, arrayList);
                }
            });
            return new RadioModule(service, Convert.propertiesFromHal(idx, fqName, service.getProperties(), (AmFmRegionConfig) amfmConfig.value, (List) dabConfig.value));
        } catch (RemoteException ex) {
            Slog.e(TAG, "failed to load module " + fqName, ex);
            return null;
        }
    }

    static /* synthetic */ void lambda$tryLoadingModule$0(Mutable amfmConfig, int result, AmFmRegionConfig config) {
        if (result == 0) {
            amfmConfig.value = config;
        }
    }

    static /* synthetic */ void lambda$tryLoadingModule$1(Mutable dabConfig, int result, ArrayList config) {
        if (result == 0) {
            dabConfig.value = config;
        }
    }

    public IBroadcastRadio getService() {
        return this.mService;
    }

    public TunerSession openSession(android.hardware.radio.ITunerCallback userCb) throws RemoteException {
        TunerSession tunerSession;
        synchronized (this.mLock) {
            if (this.mHalTunerSession == null) {
                Mutable<ITunerSession> hwSession = new Mutable<>();
                this.mService.openSession(this.mHalTunerCallback, new IBroadcastRadio.openSessionCallback() {
                    public final void onValues(int i, ITunerSession iTunerSession) {
                        RadioModule.lambda$openSession$2(Mutable.this, i, iTunerSession);
                    }
                });
                this.mHalTunerSession = (ITunerSession) Objects.requireNonNull((ITunerSession) hwSession.value);
            }
            tunerSession = new TunerSession(this, this.mHalTunerSession, userCb);
            this.mAidlTunerSessions.add(tunerSession);
            if (this.mAntennaConnected != null) {
                userCb.onAntennaState(this.mAntennaConnected.booleanValue());
            }
            if (this.mProgramInfo != null) {
                userCb.onCurrentProgramInfoChanged(this.mProgramInfo);
            }
        }
        return tunerSession;
    }

    static /* synthetic */ void lambda$openSession$2(Mutable hwSession, int result, ITunerSession session) {
        Convert.throwOnError("openSession", result);
        hwSession.value = session;
    }

    public void closeSessions(Integer error) {
        TunerSession[] tunerSessions;
        synchronized (this.mLock) {
            tunerSessions = new TunerSession[this.mAidlTunerSessions.size()];
            this.mAidlTunerSessions.toArray(tunerSessions);
            this.mAidlTunerSessions.clear();
        }
        for (TunerSession tunerSession : tunerSessions) {
            tunerSession.close(error);
        }
    }

    /* access modifiers changed from: package-private */
    public void onTunerSessionClosed(TunerSession tunerSession) {
        synchronized (this.mLock) {
            this.mAidlTunerSessions.remove(tunerSession);
            if (this.mAidlTunerSessions.isEmpty() && this.mHalTunerSession != null) {
                Slog.v(TAG, "closing HAL tuner session");
                try {
                    this.mHalTunerSession.close();
                } catch (RemoteException ex) {
                    Slog.e(TAG, "mHalTunerSession.close() failed: ", ex);
                }
                this.mHalTunerSession = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void fanoutAidlCallback(AidlCallbackRunnable runnable) {
        synchronized (this.mLock) {
            fanoutAidlCallbackLocked(runnable);
        }
    }

    /* access modifiers changed from: private */
    public void fanoutAidlCallbackLocked(AidlCallbackRunnable runnable) {
        for (TunerSession tunerSession : this.mAidlTunerSessions) {
            try {
                runnable.run(tunerSession.mCallback);
            } catch (DeadObjectException e) {
                Slog.e(TAG, "Removing dead TunerSession");
                this.mAidlTunerSessions.remove(tunerSession);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to invoke ITunerCallback: ", ex);
            }
        }
    }

    public ICloseHandle addAnnouncementListener(int[] enabledTypes, final IAnnouncementListener listener) throws RemoteException {
        ArrayList<Byte> enabledList = new ArrayList<>();
        for (int type : enabledTypes) {
            enabledList.add(Byte.valueOf((byte) type));
        }
        MutableInt halResult = new MutableInt(1);
        final Mutable<android.hardware.broadcastradio.V2_0.ICloseHandle> hwCloseHandle = new Mutable<>();
        android.hardware.broadcastradio.V2_0.IAnnouncementListener hwListener = new IAnnouncementListener.Stub() {
            public void onListUpdated(ArrayList<Announcement> hwAnnouncements) throws RemoteException {
                listener.onListUpdated((List) hwAnnouncements.stream().map($$Lambda$RadioModule$2$06udTLOtrtIC_bWCWpXUXkuLVM.INSTANCE).collect(Collectors.toList()));
            }
        };
        synchronized (this.mService) {
            this.mService.registerAnnouncementListener(enabledList, hwListener, new IBroadcastRadio.registerAnnouncementListenerCallback(halResult, hwCloseHandle) {
                private final /* synthetic */ MutableInt f$0;
                private final /* synthetic */ Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(int i, android.hardware.broadcastradio.V2_0.ICloseHandle iCloseHandle) {
                    RadioModule.lambda$addAnnouncementListener$3(this.f$0, this.f$1, i, iCloseHandle);
                }
            });
        }
        Convert.throwOnError("addAnnouncementListener", halResult.value);
        return new ICloseHandle.Stub() {
            public void close() {
                try {
                    ((android.hardware.broadcastradio.V2_0.ICloseHandle) hwCloseHandle.value).close();
                } catch (RemoteException ex) {
                    Slog.e(RadioModule.TAG, "Failed closing announcement listener", ex);
                }
            }
        };
    }

    static /* synthetic */ void lambda$addAnnouncementListener$3(MutableInt halResult, Mutable hwCloseHandle, int result, android.hardware.broadcastradio.V2_0.ICloseHandle closeHnd) {
        halResult.value = result;
        hwCloseHandle.value = closeHnd;
    }

    /* access modifiers changed from: package-private */
    public Bitmap getImage(int id) {
        byte[] rawImage;
        if (id != 0) {
            synchronized (this.mService) {
                List<Byte> rawList = (List) Utils.maybeRethrow(new Utils.FuncThrowingRemoteException(id) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final Object exec() {
                        return RadioModule.this.lambda$getImage$4$RadioModule(this.f$1);
                    }
                });
                rawImage = new byte[rawList.size()];
                for (int i = 0; i < rawList.size(); i++) {
                    rawImage[i] = rawList.get(i).byteValue();
                }
            }
            if (rawImage.length == 0) {
                return null;
            }
            return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        }
        throw new IllegalArgumentException("Image ID is missing");
    }

    public /* synthetic */ ArrayList lambda$getImage$4$RadioModule(int id) throws RemoteException {
        return this.mService.getImage(id);
    }
}
