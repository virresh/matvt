package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.helper.Helper;
import io.github.virresh.matvt.view.OverlayView;

import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.bossKey;

public class MouseEventService extends AccessibilityService {

    private MouseEmulationEngine mEngine;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (Helper.isOverlayDisabled(this)) return false;
        return mEngine.perform(event);
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        bossKey = KeyEvent.KEYCODE_VOLUME_MUTE;
        bossKey = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE; //For Testing
        if (Settings.canDrawOverlays(this))
            init();
    }

    private void init() {
        OverlayView mOverlayView = new OverlayView(this);
        mEngine = new MouseEmulationEngine(this, mOverlayView);
        mEngine.init(this);
    }
}
