package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.engine.impl.PointerControl;
import io.github.virresh.matvt.helper.Helper;
import io.github.virresh.matvt.view.OverlayView;

import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.bossKey;
import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.scrollSpeed;

public class MouseEventService extends AccessibilityService {

    private MouseEmulationEngine mEngine;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);
        Logger.getLogger("WTFLOGGER").log(Level.INFO, "Key ===>>>>>>>>>> " + event.getKeyCode());
        if (Helper.isAnotherServiceInstalled(this) &&
                event.getKeyCode() == KeyEvent.KEYCODE_HOME) return true;
        if (Helper.isOverlayDisabled(this)) return false;
        return mEngine.perform(event);
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        bossKey = KeyEvent.KEYCODE_VOLUME_MUTE;
        PointerControl.isBordered = Helper.getMouseBordered(this);
        scrollSpeed = Helper.getScrollSpeed(this);
        if (Helper.isOverriding(this)) bossKey = Helper.getOverrideValue(this);
        if (Settings.canDrawOverlays(this)) init();
    }

    private void init() {
        OverlayView mOverlayView = new OverlayView(this);
        AccessibilityServiceInfo asi = this.getServiceInfo();
        if (asi != null) {
            asi.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            this.setServiceInfo(asi);
        }
        mEngine = new MouseEmulationEngine(this, mOverlayView);
        mEngine.init(this);
    }
}
