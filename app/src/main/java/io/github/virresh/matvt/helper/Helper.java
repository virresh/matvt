package io.github.virresh.matvt.helper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.RequiresApi;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbCrypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;

public class Helper {
    // TODO: Implement listener based sharedContext and replace instant commits with background commits.

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
    static final String IO_PUBLICKEY_FILENAME = "public_key.bin";
    static final String IO_PRIVATEKEY_FILENAME = "private_key.bin";


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
        return sp.getInt(PREF_KEY_CB_OVERRIDE_VAL, KeyEvent.KEYCODE_VOLUME_MUTE);
    }

    public static int getEffectiveBossKeyValue(Context ctx) {
        if (isOverriding(ctx)) return getBossKeyValue(ctx);
        return KeyEvent.KEYCODE_VOLUME_MUTE;
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
        AdbBase64 adbBase64Encoder;
        File packageFilesDir;
        File publicKeyFile;
        File privateKeyFile;

        adbBase64Encoder = new AdbBase64() {
            @Override
            public String encodeToString(byte[] data) {
                return Base64.getEncoder().encodeToString(data);
            }
        };

        packageFilesDir = ctx.getFilesDir();
        publicKeyFile = new File(packageFilesDir,IO_PUBLICKEY_FILENAME);
        privateKeyFile = new File(packageFilesDir,IO_PRIVATEKEY_FILENAME);

        try {

            if (publicKeyFile.exists() && privateKeyFile.exists()) {

                crypto = AdbCrypto.loadAdbKeyPair(adbBase64Encoder, privateKeyFile, publicKeyFile);

            } else {

                crypto = AdbCrypto.generateAdbKeyPair(adbBase64Encoder);
                crypto.saveAdbKeyPair(privateKeyFile, publicKeyFile);

            }

        }catch (IOException Exception){

            Log.e("HELPER", "Could not read/write key files");

        }catch(InvalidKeySpecException Exception){

            Log.e("HELPER","Could not load keys.");

        }


        return crypto;
    }

}
