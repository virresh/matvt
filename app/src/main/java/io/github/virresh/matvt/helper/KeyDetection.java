package io.github.virresh.matvt.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.virresh.matvt.R;
import io.github.virresh.matvt.services.MouseEventService;

public class KeyDetection extends AppCompatActivity{
    private static final String LOG_TAG = "BOSS_KEY_ACTIVITY";
    public static boolean isDetectionActivityInForeground = false;

    // TODO: Find a better way to share state and avoid this static leak.
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;

    private static MouseEventService mouseEventService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_detection);
        activity = KeyDetection.this;
    }

    public KeyDetection(){ } //Empty Constructor

    public KeyDetection(KeyEvent event, MouseEventService service){
        mouseEventService = service;
        if (isDetectionActivityInForeground) getKey(event); //Proceed only if the Activity is in Foreground.
    }

    private void getKey(KeyEvent event) {
        TextView textView = activity.findViewById(R.id.pressed_key);
        if (event.getAction() == KeyEvent.ACTION_DOWN) textView.setText(event.getKeyCode()+"");
        else if (event.getAction() == KeyEvent.ACTION_UP) textView.setText(" ");
        if (event.getEventTime() - event.getDownTime() > 1000)
            changeBossKey(event.getKeyCode()); //Ask to change Boss key if the key is pressed for more than 1 sec.
        // some Buttons do not give their Event Times to the Services so we will not use them, because its needed of long press to work
    }

    public static void changeBossKey(int keyCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(KeyDetection.activity, R.style.Wolf_Alert_Disp);
        builder.setTitle("Confirm your changes");
        builder.setMessage("Do you really want to set Key \""+ keyCode + "\" as new Boss Key??");
        builder.setPositiveButton("YES", (dialogInterface, i) -> {
            Helper.setBossKeyDisabled(activity, false);
            Helper.setOverrideStatus(activity, true);
            Helper.setBossKeyValue(activity, keyCode);
            updateFromPreferences();
            Toast.makeText(activity, "New Boss key is : "+keyCode, Toast.LENGTH_SHORT).show();
            dialogInterface.dismiss();
            activity.finish();
        });
        builder.setNegativeButton("NO", (dialog, whichButton) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }


    @Override
    protected void onResume() {
        isDetectionActivityInForeground = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isDetectionActivityInForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Add any other static references here for proper cleanup.
        mouseEventService = null;
        activity = null;
    }

    private static void updateFromPreferences() {
        if (mouseEventService == null) {
            mouseEventService = MouseEventService.getInstance();
        }
        if (mouseEventService == null) {
            Log.i(LOG_TAG, "Accessibility Service not available. Changes will be applied next time.");
        }
        else {
            mouseEventService.updatePreferences();
        }
    }
}
