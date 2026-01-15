
package io.github.virresh.matvt.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;

public class AppPreferences {
    private static final String PREFS_ID = "MATVT";
    private static final String PREF_ALERTS_HIDE_TOASTS = "HIDE_ALERTS";
    private static final String PREF_KEY_CB_OVERRIDE_STAT = "CB_OVERRIDE_STAT";
    private static final String PREF_KEY_CB_OVERRIDE_VAL = "CB_OVERRIDE_VAL";
    private static final String PREF_KEY_MOUSE_ICON = "MOUSE_ICON";
    private static final String PREF_KEY_MOUSE_SIZE = "MOUSE_SIZE";
    private static final String PREF_KEY_SCROLL_SPEED = "SCROLL_SPEED";
    private static final String PREF_KEY_MOUSE_BORDERED = "MOUSE_BORDERED";
    private static final String PREF_KEY_CB_DISABLE_BOSSKEY = "DISABLE_BOSSKEY";
    private static final String PREF_KEY_CB_BEHAVIOUR_BOSSKEY = "CB_BEHAVIOUR_BOSSKEY";
    private static final String PREF_KEY_ENGINE_TYPE = "ENGINE_TYPE";
    private static final String PREF_KEY_CONFIRM_KEY = "CONFIRM_KEY";

    private final SharedPreferences sharedPreferences;

    public AppPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
    }

    public boolean isOverriding() {
        return sharedPreferences.getBoolean(PREF_KEY_CB_OVERRIDE_STAT, false);
    }

    public void setOverrideStatus(boolean val) {
        sharedPreferences.edit().putBoolean(PREF_KEY_CB_OVERRIDE_STAT, val).apply();
    }

    public int getBossKeyValue() {
        return sharedPreferences.getInt(PREF_KEY_CB_OVERRIDE_VAL, KeyEvent.KEYCODE_VOLUME_MUTE);
    }

    public int getEffectiveBossKeyValue() {
        if (isOverriding()) {
            return getBossKeyValue();
        }
        return KeyEvent.KEYCODE_VOLUME_MUTE;
    }

    public void setBossKeyValue(int val) {
        sharedPreferences.edit().putInt(PREF_KEY_CB_OVERRIDE_VAL, val).apply();
    }

    public String getMouseIconPref() {
        return sharedPreferences.getString(PREF_KEY_MOUSE_ICON, "default");
    }

    public void setMouseIconPref(String val) {
        sharedPreferences.edit().putString(PREF_KEY_MOUSE_ICON, val).apply();
    }

    public int getMouseSizePref() {
        return sharedPreferences.getInt(PREF_KEY_MOUSE_SIZE, 1);
    }

    public void setMouseSizePref(int val) {
        sharedPreferences.edit().putInt(PREF_KEY_MOUSE_SIZE, val).apply();
    }

    public int getScrollSpeed() {
        return sharedPreferences.getInt(PREF_KEY_SCROLL_SPEED, 4);
    }

    public void setScrollSpeed(int val) {
        sharedPreferences.edit().putInt(PREF_KEY_SCROLL_SPEED, val).apply();
    }

    public void setMouseBordered(boolean val) {
        sharedPreferences.edit().putBoolean(PREF_KEY_MOUSE_BORDERED, val).apply();
    }

    public boolean getMouseBordered() {
        return sharedPreferences.getBoolean(PREF_KEY_MOUSE_BORDERED, false);
    }

    public void setBossKeyDisabled(boolean val) {
        sharedPreferences.edit().putBoolean(PREF_KEY_CB_DISABLE_BOSSKEY, val).apply();
    }

    public boolean isBossKeyDisabled() {
        return sharedPreferences.getBoolean(PREF_KEY_CB_DISABLE_BOSSKEY, false);
    }

    public void setBossKeyBehaviour(boolean val) {
        sharedPreferences.edit().putBoolean(PREF_KEY_CB_BEHAVIOUR_BOSSKEY, val).apply();
    }

    public boolean isBossKeySetToToggle() {
        return sharedPreferences.getBoolean(PREF_KEY_CB_BEHAVIOUR_BOSSKEY, false);
    }
    
    public void setHideToastsOptionEnabled(boolean val){
        sharedPreferences.edit().putBoolean(PREF_ALERTS_HIDE_TOASTS,val).apply();
    }

    public boolean isHideToastOptionEnabled(){
        return sharedPreferences.getBoolean(PREF_ALERTS_HIDE_TOASTS, false);
    }

    public String getEngineType() {
        return sharedPreferences.getString(PREF_KEY_ENGINE_TYPE, "gesture");
    }

    public void setEngineType(String val) {
        sharedPreferences.edit().putString(PREF_KEY_ENGINE_TYPE, val).apply();
    }

    public int getConfirmKeyValue() {
        return sharedPreferences.getInt(PREF_KEY_CONFIRM_KEY, KeyEvent.KEYCODE_DPAD_CENTER);
    }

    public void setConfirmKeyValue(int val) {
        sharedPreferences.edit().putInt(PREF_KEY_CONFIRM_KEY, val).apply();
    }
}
