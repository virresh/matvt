package io.github.virresh.matvt.helper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class Helper {
    public static boolean isAccessibilityDisabled(Context ctx) {
        String id = ctx.getPackageName() + "/.services.MouseEventService";
        AccessibilityManager am = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = null;

        if (am != null) {
            runningServices = am
                    .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        }

        if (runningServices != null) {
            for (AccessibilityServiceInfo service : runningServices) {
                if (id.equals(service.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isOverlayDisabled(Context ctx) {
        return !Settings.canDrawOverlays(ctx);
    }
}
