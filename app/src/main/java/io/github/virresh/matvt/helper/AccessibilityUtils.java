package io.github.virresh.matvt.helper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class AccessibilityUtils {
    private static final String FIRE_TV_SETTINGS_SERVICE_ID = "com.wolf.firetvsettings/.main.services.HomeService";
    private static final String BUTTON_MAPPER_SERVICE_ID = "flar2.homebutton/.a.i";

    public static boolean isAccessibilityDisabled(Context ctx) {
        return !isAccServiceInstalled(ctx.getPackageName() + "/.services.MouseEventService", ctx);
    }

    public static boolean isAnotherServiceInstalled(Context ctx) {
        return isAccServiceInstalled(FIRE_TV_SETTINGS_SERVICE_ID, ctx) || isAccServiceInstalled(BUTTON_MAPPER_SERVICE_ID, ctx);
    }

    public static boolean isAccServiceInstalled(String serviceId, Context ctx) {
        AccessibilityManager am = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = null;
        if (am != null) {
            runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        }
        if (runningServices != null) {
            for (AccessibilityServiceInfo service : runningServices) {
                if (serviceId.equals(service.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOverlayDisabled(Context ctx) {
        return !Settings.canDrawOverlays(ctx);
    }
}