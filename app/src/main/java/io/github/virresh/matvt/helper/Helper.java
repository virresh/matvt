package io.github.virresh.matvt.helper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbCrypto;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class Helper {

    @SuppressLint("StaticFieldLeak")
    public static Context helperContext;

    static final String PREFS_ID = "MATVT";
    static final String PREF_KEY_CB_OVERRIDE_STAT = "CB_OVERRIDE_STAT";
    static final String PREF_KEY_CB_OVERRIDE_VAL = "CB_OVERRIDE_VAL";
    static final String PREF_KEY_MOUSE_ICON = "MOUSE_ICON";
    static final String PREF_KEY_MOUSE_SIZE = "MOUSE_SIZE";
    static final String PREF_KEY_SCROLL_SPEED = "SCROLL_SPEED";
    static final String PREF_KEY_MOUSE_BORDERED = "MOUSE_BORDERED";
    static final String PREF_KEY_CB_DISABLE_BOSSKEY = "DISABLE_BOSSKEY";
    static final String PREF_KEY_CB_BEHAVIOUR_BOSSKEY = "CB_BEHAVIOUR_BOSSKEY";
    static final String PREF_KEY_CRYPTO_ADB = "ADB_CRYPTO";

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static AdbCrypto getAdbCryptoKey(Context ctx) throws NoSuchAlgorithmException {
        AdbCrypto crypto = null;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
        if (sp.contains(PREF_KEY_CRYPTO_ADB)) {
            Gson gson = new Gson();
            crypto = gson.fromJson(sp.getString(PREF_KEY_CRYPTO_ADB, null), AdbCrypto.class);
        }
        else {
            crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return Base64.getEncoder().encodeToString(data);
                }
            });
            Gson gson = new Gson();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(PREF_KEY_CRYPTO_ADB, gson.toJson(crypto));
        }
        return crypto;
    }

}
