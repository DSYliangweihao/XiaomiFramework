package com.miui.gamebooster.ui;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.miui.gamebooster.m.Q;
import com.miui.gamebooster.m.na;

class Pa implements ViewTreeObserver.OnGlobalLayoutListener {

    /* renamed from: a  reason: collision with root package name */
    final /* synthetic */ SettingsActivity f4956a;

    Pa(SettingsActivity settingsActivity) {
        this.f4956a = settingsActivity;
    }

    /* JADX WARNING: type inference failed for: r0v5, types: [com.miui.gamebooster.ui.SettingsActivity, android.app.Activity] */
    public void onGlobalLayout() {
        View decorView = this.f4956a.getWindow().getDecorView();
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        Rect a2 = Q.a(decorView);
        Log.i("SettingsActivity", "onGlobalLayout: " + a2);
        if (a2 != null) {
            int a3 = na.a(this.f4956a.getApplicationContext());
            if ((a3 == 90 || a3 == 270) && a2.top == 0) {
                Q.a(this.f4956a, 2);
            }
        }
    }
}
