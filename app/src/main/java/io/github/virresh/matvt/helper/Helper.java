package io.github.virresh.matvt.helper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class Helper {

    static final String PREFS_ID = "MATVT";
    static final String PREF_KEY_CB_OVERRIDE_STAT = "CB_OVERRIDE_STAT";
    static final String PREF_KEY_CB_OVERRIDE_VAL = "CB_OVERRIDE_VAL";

    public static boolean isAccessibilityDisabled(Context ctx) {
        return !isAccServiceInstalled(ctx.getPackageName() + "/.services.MouseEventService", ctx);
    }

    public static boolean isAnotherServiceInstalled(Context ctx) {
        String fireTVSettings = "com.wolf.firetvsettings/.HomeService";
        String buttonMapper = "flar2.homebutton/.a.i";
        return isAccServiceInstalled(fireTVSettings, ctx) || isAccServiceInstalled(buttonMapper, ctx);
    }

    public static boolean isAccServiceInstalled(String serviceId, Context ctx) {
        AccessibilityManager am = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = null;
        if (am != null)  runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        if (runningServices != null) {
            for (AccessibilityServiceInfo service : runningServices)
                if (serviceId.equals(service.getId())) return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(Context ctx) {
        return !Settings.canDrawOverlays(ctx);
    }

    public static boolean isOverriding(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID,
                Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_CB_OVERRIDE_STAT, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void setOverrideStatus(Context ctx, boolean val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEY_CB_OVERRIDE_STAT, val);
        editor.commit();
    }

    public static int getOverrideValue(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getInt(PREF_KEY_CB_OVERRIDE_VAL, 164);
    }

    @SuppressLint("ApplySharedPref")
    public static void setOverrideValue(Context ctx, int val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_KEY_CB_OVERRIDE_VAL, val);
        editor.commit();
    }


}
