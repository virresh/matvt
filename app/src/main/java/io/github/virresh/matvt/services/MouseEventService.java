package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.github.virresh.matvt.BuildConfig;
import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.engine.impl.PointerControl;
import io.github.virresh.matvt.helper.Helper;
import io.github.virresh.matvt.helper.KeyDetection;
import io.github.virresh.matvt.view.OverlayView;

import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.bossKey;
import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.scrollSpeed;

public class MouseEventService extends AccessibilityService {

    private MouseEmulationEngine mEngine;
    private static String TAG_NAME = "MATVT_SERVICE";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);
        new KeyDetection(event);
        Log.i(TAG_NAME, "MATVT Received Key => " + event.getKeyCode() + ", Action => " + event.getAction() + ", Repetition value => " + event.getRepeatCount() + ", Scan code => " + event.getScanCode());
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
        Log.i(TAG_NAME, "Starting service initialization sequence. App version " + BuildConfig.VERSION_NAME);
        bossKey = KeyEvent.KEYCODE_VOLUME_MUTE;
        PointerControl.isBordered = Helper.getMouseBordered(this);
        scrollSpeed = Helper.getScrollSpeed(this);
        MouseEmulationEngine.isBossKeyDisabled = Helper.isBossKeyDisabled(this);
        MouseEmulationEngine.isBossKeySetToToggle = Helper.isBossKeySetToToggle(this);
        if (Helper.isOverriding(this)) bossKey = Helper.getBossKeyValue(this);
        if (Settings.canDrawOverlays(this)) init();
    }

    private void init() {
        OverlayView mOverlayView = new OverlayView(this);
        AccessibilityServiceInfo asi = this.getServiceInfo();
        if (asi != null) {
            asi.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            this.setServiceInfo(asi);
        }
        Log.i(TAG_NAME, "Configuration -- Scroll Speed " + scrollSpeed);
        Log.i(TAG_NAME, "Configuration -- Boss Key Disabled " + MouseEmulationEngine.isBossKeyDisabled);
        Log.i(TAG_NAME, "Configuration -- Boss Key Toggleable " + MouseEmulationEngine.isBossKeySetToToggle);
        Log.i(TAG_NAME, "Configuration -- Is Bordered " + PointerControl.isBordered);
        Log.i(TAG_NAME, "Configuration -- Boss Key value " + bossKey);

        mEngine = new MouseEmulationEngine(this, mOverlayView);
        mEngine.init(this);
    }
}
