package vendor.qti.hardware.servicetracker.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ClientConnection {
    public int count;
    public String serviceName = new String();
    public int servicePid;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ClientConnection.class) {
            return false;
        }
        ClientConnection other = (ClientConnection) otherObject;
        if (HidlSupport.deepEquals(this.serviceName, other.serviceName) && this.servicePid == other.servicePid && this.count == other.count) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.serviceName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.servicePid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.count)))});
    }

    public final String toString() {
        return "{" + ".serviceName = " + this.serviceName + ", .servicePid = " + this.servicePid + ", .count = " + this.count + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<ClientConnection> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ClientConnection> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ClientConnection _hidl_vec_element = new ClientConnection();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.serviceName = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.serviceName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.servicePid = _hidl_blob.getInt32(16 + _hidl_offset);
        this.count = _hidl_blob.getInt32(20 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ClientConnection> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.serviceName);
        _hidl_blob.putInt32(16 + _hidl_offset, this.servicePid);
        _hidl_blob.putInt32(20 + _hidl_offset, this.count);
    }
}
