package com.android.server.backup.fullbackup;

import android.app.backup.IBackupManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.backup.IObbBackupService;
import com.android.internal.util.Preconditions;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupManagerServiceInjector;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.utils.FullBackupUtils;
import java.io.IOException;
import java.io.OutputStream;

public class FullBackupObbConnection implements ServiceConnection {
    private UserBackupManagerService backupManagerService;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    volatile IObbBackupService mService = null;

    public FullBackupObbConnection(UserBackupManagerService backupManagerService2) {
        this.backupManagerService = backupManagerService2;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
    }

    public void establish() {
        Slog.i(BackupManagerService.TAG, "Initiating bind of OBB service on " + this);
        UserHandle userHandle = UserHandle.SYSTEM;
        int userId = this.backupManagerService.getAppUserId();
        if (BackupManagerServiceInjector.isXSpaceUser(userId)) {
            boolean xSpaceUserRunning = BackupManagerServiceInjector.isXSpaceUserRunning();
            if (xSpaceUserRunning && BackupManagerServiceInjector.installExistingPackageAsUser(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, userId)) {
                userHandle = new UserHandle(userId);
            }
            Slog.i(BackupManagerService.TAG, "establish, xSpaceUserRunning " + xSpaceUserRunning + ", " + userHandle.toString());
        }
        this.backupManagerService.getContext().bindServiceAsUser(new Intent().setComponent(new ComponentName(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, "com.android.sharedstoragebackup.ObbBackupService")), this, 1, userHandle);
    }

    public void tearDown() {
        this.backupManagerService.getContext().unbindService(this);
    }

    public boolean backupObbs(PackageInfo pkg, OutputStream out) {
        boolean success = false;
        waitForConnection();
        try {
            ParcelFileDescriptor[] pipes = ParcelFileDescriptor.createPipe();
            int token = this.backupManagerService.generateRandomIntegerToken();
            this.backupManagerService.prepareOperationTimeout(token, this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), (BackupRestoreTask) null, 0);
            this.mService.backupObbs(pkg.packageName, pipes[1], token, this.backupManagerService.getBackupManagerBinder());
            FullBackupUtils.routeSocketDataToOutput(pipes[0], out);
            success = this.backupManagerService.waitUntilOperationComplete(token);
            try {
                out.flush();
                if (pipes[0] != null) {
                    pipes[0].close();
                }
                if (pipes[1] != null) {
                    pipes[1].close();
                }
            } catch (IOException e) {
                Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e);
            }
        } catch (Exception e2) {
            Slog.w(BackupManagerService.TAG, "Unable to back up OBBs for " + pkg, e2);
            out.flush();
            if (0 != 0) {
                if (null[0] != null) {
                    null[0].close();
                }
                if (null[1] != null) {
                    null[1].close();
                }
            }
        } catch (Throwable th) {
            try {
                out.flush();
                if (0 != 0) {
                    if (null[0] != null) {
                        null[0].close();
                    }
                    if (null[1] != null) {
                        null[1].close();
                    }
                }
            } catch (IOException e3) {
                Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e3);
            }
            throw th;
        }
        return success;
    }

    public void restoreObbFile(String pkgName, ParcelFileDescriptor data, long fileSize, int type, String path, long mode, long mtime, int token, IBackupManager callbackBinder) {
        waitForConnection();
        try {
            this.mService.restoreObbFile(pkgName, data, fileSize, type, path, mode, mtime, token, callbackBinder);
            String str = pkgName;
        } catch (Exception e) {
            Slog.w(BackupManagerService.TAG, "Unable to restore OBBs for " + pkgName, e);
        }
    }

    private void waitForConnection() {
        synchronized (this) {
            while (this.mService == null) {
                Slog.i(BackupManagerService.TAG, "...waiting for OBB service binding...");
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            Slog.i(BackupManagerService.TAG, "Connected to OBB service; continuing");
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this) {
            this.mService = IObbBackupService.Stub.asInterface(service);
            Slog.i(BackupManagerService.TAG, "OBB service connection " + this.mService + " connected on " + this);
            notifyAll();
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this) {
            this.mService = null;
            Slog.i(BackupManagerService.TAG, "OBB service connection disconnected on " + this);
            notifyAll();
        }
    }
}
