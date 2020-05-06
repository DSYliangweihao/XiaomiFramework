package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.WorkSource;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.location.AbstractLocationProvider;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MockProvider extends AbstractLocationProvider {
    private boolean mEnabled = true;
    private Bundle mExtras = null;
    private Location mLocation = null;
    private int mStatus = 2;
    private long mStatusUpdateTime = 0;

    public MockProvider(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager, ProviderProperties properties) {
        super(context, locationProviderManager);
        setProperties(properties);
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
        super.setEnabled(enabled);
    }

    public void setLocation(Location l) {
        this.mLocation = new Location(l);
        if (!this.mLocation.isFromMockProvider()) {
            this.mLocation.setIsFromMockProvider(true);
        }
        if (this.mEnabled) {
            reportLocation(this.mLocation);
        }
    }

    public void setStatus(int status, Bundle extras, long updateTime) {
        this.mStatus = status;
        this.mStatusUpdateTime = updateTime;
        this.mExtras = extras;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(" last location=" + this.mLocation);
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
    }

    public int getStatus(Bundle extras) {
        if (this.mExtras != null) {
            extras.clear();
            extras.putAll(this.mExtras);
        }
        return this.mStatus;
    }

    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    public void sendExtraCommand(String command, Bundle extras) {
    }
}
