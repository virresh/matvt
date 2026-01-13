package io.github.virresh.matvt.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.virresh.matvt.BuildConfig;
import io.github.virresh.matvt.R;
import io.github.virresh.matvt.helper.AccessibilityUtils;
import io.github.virresh.matvt.helper.AppPreferences;

import io.github.virresh.matvt.helper.KeyDetection;
import io.github.virresh.matvt.services.MouseEventService;

public class GuiActivity extends AppCompatActivity {
    private static final String TAG_NAME = "MATVT_SERVICE";

    CountDownTimer repopulate;
    CheckBox cb_mouse_bordered, cb_disable_bossKey, cb_behaviour_bossKey;
    TextView gui_acc_perm, gui_acc_serv, gui_overlay_perm, gui_overlay_serv, gui_about;

    EditText et_override;
    Button bt_saveBossKeyValue;

    Spinner sp_mouse_icon;
    SeekBar dsbar_mouse_size;
    SeekBar dsbar_scroll_speed;

    MouseEventService mService;
    private AppPreferences appPreferences;

    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 701;
    public static final int ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE = 702;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = new AppPreferences(this);
        mService = MouseEventService.getInstance();
        setContentView(R.layout.activity_main_gui);
        gui_acc_perm = findViewById(R.id.gui_acc_perm);
        gui_acc_serv = findViewById(R.id.gui_acc_serv);
        gui_overlay_perm = findViewById(R.id.gui_overlay_perm);
        gui_overlay_serv = findViewById(R.id.gui_overlay_serv);
        gui_about = findViewById(R.id.gui_about);

        bt_saveBossKeyValue = findViewById(R.id.bt_saveBossKey);
        et_override = findViewById(R.id.et_override);

        cb_mouse_bordered = findViewById(R.id.cb_border_window);
        cb_disable_bossKey = findViewById(R.id.cb_disable_bossKey);
        cb_behaviour_bossKey = findViewById(R.id.cb_behaviour_bossKey);

        sp_mouse_icon = findViewById(R.id.sp_mouse_icon);
        dsbar_mouse_size = findViewById(R.id.dsbar_mouse_size);
        dsbar_scroll_speed = findViewById(R.id.dsbar_mouse_scspeed);

        // don't like to advertise in the product, but need to mention here
        // need to increase visibility of the open source version
        gui_about.setText("MATVT v" + BuildConfig.VERSION_NAME + "\nThis is an open source project. It's available for free and will always be. If you find issues / would like to help in improving this project, please contribute at \nhttps://github.com/virresh/matvt");

        // render icon style dropdown
        IconStyleSpinnerAdapter iconStyleSpinnerAdapter = new IconStyleSpinnerAdapter(this, R.layout.spinner_icon_text_gui, R.id.textView, IconStyleSpinnerAdapter.getResourceList());
        sp_mouse_icon.setAdapter(iconStyleSpinnerAdapter);

        checkValues(iconStyleSpinnerAdapter);

        bt_saveBossKeyValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dat = et_override.getText().toString();
                dat = dat.replaceAll("[^0-9]", "");
                int keyValue; if (dat.isEmpty()) keyValue = KeyEvent.KEYCODE_VOLUME_MUTE;
                else keyValue = Integer.parseInt(dat);
                isBossKeyChanged();
                appPreferences.setOverrideStatus(isBossKeyChanged());
                appPreferences.setBossKeyValue(keyValue);
                Toast.makeText(GuiActivity.this, "New Boss key is : "+keyValue, Toast.LENGTH_SHORT).show();
                updateFromPreferences();
            }
        });



        sp_mouse_icon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // the listener is set after setting initial value to avoid echo if any
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                Context ctx = getApplicationContext();
                String style = iconStyleSpinnerAdapter.getItem(pos);
                appPreferences.setMouseIconPref(iconStyleSpinnerAdapter.getItem(pos));
                updateFromPreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        dsbar_mouse_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // do not do anything if the progress change was done programmatically
                    Context ctx = getApplicationContext();
                    appPreferences.setMouseSizePref(progress);
                    updateFromPreferences();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        dsbar_scroll_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // do not do anything if the progress change was done programmatically
                    Context ctx = getApplicationContext();
                    appPreferences.setScrollSpeed(progress);
                    updateFromPreferences();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        cb_mouse_bordered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appPreferences.setMouseBordered(b);
                updateFromPreferences();
            }
        });

        cb_disable_bossKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                appPreferences.setBossKeyDisabled(value);
                updateFromPreferences();
            }
        });

        cb_behaviour_bossKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                appPreferences.setBossKeyBehaviour(value);
                updateFromPreferences();
            }
        });

        populateText();
        findViewById(R.id.gui_setup_perm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissions();
            }
        });
    }

    private boolean isBossKeyChanged() {
        return appPreferences.getBossKeyValue() != 164;
    }

    private void checkValues(IconStyleSpinnerAdapter adapter) {
        Context ctx = getApplicationContext();
        String val = String.valueOf(appPreferences.getBossKeyValue());
        et_override.setText(val);
        String iconStyle = appPreferences.getMouseIconPref();
        sp_mouse_icon.setSelection(adapter.getPosition(iconStyle));

        int mouseSize = appPreferences.getMouseSizePref();
        dsbar_mouse_size.setProgress(Math.max(Math.min(mouseSize, dsbar_mouse_size.getMax()), 0));

        int scrollSpeed = appPreferences.getScrollSpeed();
        dsbar_scroll_speed.setProgress(Math.max(Math.min(scrollSpeed, dsbar_scroll_speed.getMax()), 0));

        boolean bordered = appPreferences.getMouseBordered();
        cb_mouse_bordered.setChecked(bordered);

        boolean bossKeyStatus = appPreferences.isBossKeyDisabled();
        cb_disable_bossKey.setChecked(bossKeyStatus);

        boolean bossKeyBehaviour = appPreferences.isBossKeySetToToggle();
        cb_behaviour_bossKey.setChecked(bossKeyBehaviour);
    }

    private void askPermissions() {
        if (AccessibilityUtils.isOverlayDisabled(this)) {
            try {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 
                        ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } catch (Exception unused) {
                Toast.makeText(this, "Overlay Permission Handler not Found", Toast.LENGTH_SHORT).show();
            }
        }
        if (!AccessibilityUtils.isOverlayDisabled(this) && AccessibilityUtils.isAccessibilityDisabled(this)) {
            checkAccPerms();
        }
    }

    private void checkAccPerms() {
        if (AccessibilityUtils.isAccessibilityDisabled(this))
            try {
//                startActivity(new Intent(getPackageManager().getLeanbackLaunchIntentForPackage("com.wolf.apm")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))); HELPER APP
                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 
                        ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE);
            } catch (Exception exception) {
                Toast.makeText(this, "Acessibility Handler not Found", Toast.LENGTH_SHORT).show();
            }
    }

    public void populateText() {
        if (AccessibilityUtils.isOverlayDisabled(this))  gui_overlay_perm.setText(R.string.perm_overlay_denied);
        else gui_overlay_perm.setText(R.string.perm_overlay_allowed);

        if (AccessibilityUtils.isAccessibilityDisabled(this)) {
            gui_acc_perm.setText(R.string.perm_acc_denied);
            gui_acc_serv.setText(R.string.serv_acc_denied);
            gui_overlay_serv.setText(R.string.serv_overlay_denied); } 
        else gui_acc_perm.setText(R.string.perm_acc_allowed);

        if (AccessibilityUtils.isAccessibilityDisabled(this) && AccessibilityUtils.isOverlayDisabled(this)) {
            gui_acc_perm.setText(R.string.perm_acc_denied);
            gui_acc_serv.setText(R.string.serv_acc_denied);
            gui_overlay_perm.setText(R.string.perm_overlay_denied);
            gui_overlay_serv.setText(R.string.serv_overlay_denied);
        }

        if (!AccessibilityUtils.isAccessibilityDisabled(this) && !AccessibilityUtils.isOverlayDisabled(this)) {
            gui_acc_perm.setText(R.string.perm_acc_allowed);
            gui_acc_serv.setText(R.string.serv_acc_allowed);
            gui_overlay_perm.setText(R.string.perm_overlay_allowed);
            gui_overlay_serv.setText(R.string.serv_overlay_allowed);
            findViewById(R.id.gui_setup_perm).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            if (AccessibilityUtils.isOverlayDisabled(this)) {
                Toast.makeText(this, "Overlay Permissions Denied", Toast.LENGTH_SHORT).show();
            } else checkAccPerms();
        if (requestCode == ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE)
            if (AccessibilityUtils.isAccessibilityDisabled(this)) {
                Toast.makeText(this, "Accessibility Services not running", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Checking services status
        checkServiceStatus();

        if (et_override != null)
            et_override.setText(appPreferences.getBossKeyValue()+"");
    }

    private void checkServiceStatus() {
        //checking for changed every 2 sec
        repopulate = new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long l) { } 
            @Override
            public void onFinish() {
                populateText();
                repopulate.start(); //restarting the timer
            }
        };
        repopulate.start();
    }

    private void updateFromPreferences() {
        // Initiate update sequence if feasible.
        if (mService == null) {
            mService = MouseEventService.getInstance();
        }
        if (mService == null) {
            Log.i(TAG_NAME, "Accessibility service is not connected. Changes will be applied next time it's started.");
        }
        else{
            mService.updatePreferences();
        }
    }

    public void callDetect(View view) {
        startActivity(new Intent(this, KeyDetection.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}