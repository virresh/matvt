package io.github.virresh.matvt.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MouseEventService extends AccessibilityService {

    private static String LOG_TAG = "MATVT_SERVICE";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(LOG_TAG, event.toString());
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(LOG_TAG, "TV Service Connected!");
//        AccessibilityServiceInfo eventConnectionInfo = new AccessibilityServiceInfo();
//        eventConnectionInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        this.setServiceInfo(eventConnectionInfo);
//        Log.i(LOG_TAG, "Setting event types to " + eventConnectionInfo.eventTypes);
    }
}
