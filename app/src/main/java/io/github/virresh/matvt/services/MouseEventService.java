package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.view.OverlayView;

public class MouseEventService extends AccessibilityService {

    private static String LOG_TAG = "MATVT_SERVICE";
    private static MouseEventService sMouseEventService;
    private boolean mStarted = false;
    private MouseEmulationEngine mEngine;
    private OverlayView mOverlayView;
    private boolean mCursorCapture = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(LOG_TAG, event.toString());
        if (mCursorCapture) {
            return mEngine.perform(event);
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * The service is turned on
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(LOG_TAG, "TV Service Connected!");
        init();
    }

    private void init() {
        if (mStarted) {
            Log.i(LOG_TAG, "MouseEventService is already running");
            return;
        }

        sMouseEventService = this;
        mOverlayView = new OverlayView(this);
        initEngine();
    }

    private void initEngine() {
        if (mEngine != null) {
            Log.i(LOG_TAG, "Mouse Emulation Engine already running");
        }
        mEngine = new MouseEmulationEngine(this, mOverlayView);
        Log.i(LOG_TAG, "Overlay Engine W, H " + mOverlayView.getWidth() + " " + mOverlayView.getHeight());
        mEngine.init(this);
    }

    public void setCursorCapture(boolean val) {
        mCursorCapture = val;
    }
}
