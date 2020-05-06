package com.android.server.updates;

public class ConversationActionsInstallReceiver extends ConfigUpdateInstallReceiver {
    public ConversationActionsInstallReceiver() {
        super("/data/misc/textclassifier/", "actions_suggestions.model", "metadata/actions_suggestions", "version");
    }

    /* access modifiers changed from: protected */
    public boolean verifyVersion(int current, int alternative) {
        return true;
    }
}
