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
    static final String PREF_KEY_MOUSE_ICON = "MOUSE_ICON";
    static final String PREF_KEY_MOUSE_SIZE = "MOUSE_SIZE";
    static final String PREF_KEY_SCROLL_SPEED = "SCROLL_SPEED";
    static final String PREF_KEY_MOUSE_BORDERED = "MOUSE_BORDERED";
    static final String PREF_KEY_CB_DISABLE_BOSSKEY = "DISABLE_BOSSKEY";
    static final String PREF_KEY_CB_BEHAVIOUR_BOSSKEY = "CB_BEHAVIOUR_BOSSKEY";

    public static boolean isAccessibilityDisabled(Context ctx) {
        return !isAccServiceInstalled(ctx.getPackageName() + "/.services.MouseEventService", ctx);
    }

    public static boolean isAnotherServiceInstalled(Context ctx) {
        String fireTVSettings = "com.wolf.firetvsettings/.main.services.HomeService";
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

    public static int getBossKeyValue(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getInt(PREF_KEY_CB_OVERRIDE_VAL, 164);
    }

    @SuppressLint("ApplySharedPref")
    public static void setBossKeyValue(Context ctx, int val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_KEY_CB_OVERRIDE_VAL, val);
        editor.commit();
    }

    public static String getMouseIconPref(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getString(PREF_KEY_MOUSE_ICON, "default");
    }

    @SuppressLint("ApplySharedPref")
    public static void setMouseIconPref(Context ctx, String val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_KEY_MOUSE_ICON, val);
        editor.commit();
    }

    public static int getMouseSizePref(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getInt(PREF_KEY_MOUSE_SIZE, 1);
    }

    @SuppressLint("ApplySharedPref")
    public static void setMouseSizePref(Context ctx, int val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_KEY_MOUSE_SIZE, val);
        editor.commit();
    }

    public static int getScrollSpeed(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getInt(PREF_KEY_SCROLL_SPEED, 4);  //15 my sweet spot
    }

    @SuppressLint("ApplySharedPref")
    public static void setScrollSpeed(Context ctx, int val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_KEY_SCROLL_SPEED, val);
        editor.commit();
    }


    @SuppressLint("ApplySharedPref")
    public static void setMouseBordered(Context ctx, Boolean val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEY_MOUSE_BORDERED, val);
        editor.commit();
    }

    public static boolean getMouseBordered(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_MOUSE_BORDERED, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void setBossKeyDisabled(Context ctx, Boolean val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEY_CB_DISABLE_BOSSKEY, val);
        editor.commit();
    }

    public static boolean isBossKeyDisabled(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_CB_DISABLE_BOSSKEY, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void setBossKeyBehaviour(Context ctx, Boolean val) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEY_CB_BEHAVIOUR_BOSSKEY, val);
        editor.commit();
    }

    public static boolean isBossKeySetToToggle(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_CB_BEHAVIOUR_BOSSKEY, false);
    }

}
