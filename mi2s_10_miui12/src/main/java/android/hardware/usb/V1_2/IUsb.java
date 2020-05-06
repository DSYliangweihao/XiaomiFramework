package android.hardware.usb.V1_2;

import android.hardware.usb.V1_0.IUsbCallback;
import android.hardware.usb.V1_0.PortRole;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.server.BatteryService;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbASFormat;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IUsb extends android.hardware.usb.V1_1.IUsb {
    public static final String kInterfaceName = "android.hardware.usb@1.2::IUsb";

    IHwBinder asBinder();

    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void enableContaminantPresenceDetection(String str, boolean z) throws RemoteException;

    void enableContaminantPresenceProtection(String str, boolean z) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IUsb asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IUsb)) {
            return (IUsb) iface;
        }
        IUsb proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IUsb castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IUsb getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IUsb getService(boolean retry) throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, retry);
    }

    static IUsb getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IUsb getService() throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR);
    }

    public static final class Proxy implements IUsb {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.usb@1.2::IUsb]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void switchRole(String portName, PortRole role) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.usb.V1_0.IUsb.kInterfaceName);
            _hidl_request.writeString(portName);
            role.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCallback(IUsbCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.usb.V1_0.IUsb.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void queryPortStatus() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.usb.V1_0.IUsb.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void enableContaminantPresenceDetection(String portName, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IUsb.kInterfaceName);
            _hidl_request.writeString(portName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void enableContaminantPresenceProtection(String portName, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IUsb.kInterfaceName);
            _hidl_request.writeString(portName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IUsb {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{IUsb.kInterfaceName, android.hardware.usb.V1_1.IUsb.kInterfaceName, android.hardware.usb.V1_0.IUsb.kInterfaceName, IBase.kInterfaceName}));
        }

        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        public final String interfaceDescriptor() {
            return IUsb.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{97, -68, UsbDescriptor.DESCRIPTORTYPE_ENDPOINT_COMPANION, 46, 124, -105, 76, 89, -78, 88, -104, -59, -123, -58, -23, 104, 94, -118, UsbASFormat.EXT_FORMAT_TYPE_I, 2, 27, 27, -19, 62, -19, -11, UsbDescriptor.DESCRIPTORTYPE_REPORT, 65, -104, -14, 120, 90}, new byte[]{-82, -68, -39, -1, 45, -96, 92, -99, 76, 67, -103, 22, -12, UsbACInterface.ACI_SAMPLE_RATE_CONVERTER, -3, UsbDescriptor.DESCRIPTORTYPE_HID, -101, -89, 98, -103, 25, 0, 124, -71, UsbASFormat.EXT_FORMAT_TYPE_I, -21, -15, 80, 6, 75, 79, UsbASFormat.EXT_FORMAT_TYPE_II}, new byte[]{78, -11, 116, -103, 39, 63, 56, -67, -67, -48, -63, 94, 86, -18, 122, 75, -59, -15, -118, 86, 68, 9, UsbDescriptor.DESCRIPTORTYPE_HID, 112, -91, 49, -33, 53, 65, -39, -32, 21}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, UsbDescriptor.DESCRIPTORTYPE_PHYSICAL, -17, 5, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -13, -51, 105, 87, 19, -109, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -72, 59, 24, -54, 76}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IUsb.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            if (_hidl_code == 1) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(android.hardware.usb.V1_0.IUsb.kInterfaceName);
                String portName = _hidl_request.readString();
                PortRole role = new PortRole();
                role.readFromParcel(_hidl_request);
                switchRole(portName, role);
            } else if (_hidl_code == 2) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(android.hardware.usb.V1_0.IUsb.kInterfaceName);
                setCallback(IUsbCallback.asInterface(_hidl_request.readStrongBinder()));
            } else if (_hidl_code == 3) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(android.hardware.usb.V1_0.IUsb.kInterfaceName);
                queryPortStatus();
            } else if (_hidl_code == 4) {
                if (_hidl_flags != false && true) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IUsb.kInterfaceName);
                enableContaminantPresenceDetection(_hidl_request.readString(), _hidl_request.readBool());
            } else if (_hidl_code != 5) {
                switch (_hidl_code) {
                    case 256067662:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        ArrayList<String> _hidl_out_descriptors = interfaceChain();
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.writeStringVector(_hidl_out_descriptors);
                        _hidl_reply.send();
                        return;
                    case 256131655:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.send();
                        return;
                    case 256136003:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        String _hidl_out_descriptor = interfaceDescriptor();
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.writeString(_hidl_out_descriptor);
                        _hidl_reply.send();
                        return;
                    case 256398152:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                        _hidl_reply.writeStatus(0);
                        HwBlob _hidl_blob = new HwBlob(16);
                        int _hidl_vec_size = _hidl_out_hashchain.size();
                        _hidl_blob.putInt32(8, _hidl_vec_size);
                        _hidl_blob.putBool(12, false);
                        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                            long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                            byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                            if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                throw new IllegalArgumentException("Array element is not of the expected length");
                            }
                            childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                        }
                        _hidl_blob.putBlob(0, childBlob);
                        _hidl_reply.writeBuffer(_hidl_blob);
                        _hidl_reply.send();
                        return;
                    case 256462420:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (!_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        setHALInstrumentation();
                        return;
                    case 256660548:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        return;
                    case 256921159:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        ping();
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.send();
                        return;
                    case 257049926:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        DebugInfo _hidl_out_info = getDebugInfo();
                        _hidl_reply.writeStatus(0);
                        _hidl_out_info.writeToParcel(_hidl_reply);
                        _hidl_reply.send();
                        return;
                    case 257120595:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (!_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface(IBase.kInterfaceName);
                        notifySyspropsChanged();
                        return;
                    case 257250372:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            } else {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (!_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IUsb.kInterfaceName);
                enableContaminantPresenceProtection(_hidl_request.readString(), _hidl_request.readBool());
            }
        }
    }
}
