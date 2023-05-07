package io.github.virresh.matvt.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.virresh.matvt.BuildConfig;

import io.github.virresh.matvt.R;
import io.github.virresh.matvt.engine.impl.MouseEmulationEngine;
import io.github.virresh.matvt.engine.impl.PointerControl;
import io.github.virresh.matvt.helper.Helper;
import io.github.virresh.matvt.helper.KeyDetection;

import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.bossKey;
import static io.github.virresh.matvt.engine.impl.MouseEmulationEngine.scrollSpeed;

public class GuiActivity extends AppCompatActivity {
    CountDownTimer repopulate;
    CheckBox cb_mouse_bordered, cb_disable_bossKey, cb_behaviour_bossKey, cb_hide_toasts;
    TextView gui_acc_perm, gui_acc_serv, gui_overlay_perm, gui_overlay_serv, gui_about;

    EditText et_override;
    Button bt_saveBossKeyValue;

    Spinner sp_mouse_icon;
    SeekBar dsbar_mouse_size;
    SeekBar dsbar_scroll_speed;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 701;
    public static int ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE = 702;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper.helperContext = this;
        setContentView(R.layout.activity_main_gui);
        gui_acc_perm = findViewById(R.id.gui_acc_perm);
        gui_acc_serv = findViewById(R.id.gui_acc_serv);
        gui_overlay_perm = findViewById(R.id.gui_overlay_perm);
        gui_overlay_serv = findViewById(R.id.gui_overlay_serv);
        gui_about = findViewById(R.id.gui_about);

        bt_saveBossKeyValue = findViewById(R.id.bt_saveBossKey);
        et_override = findViewById(R.id.et_override);

        cb_mouse_bordered = findViewById(R.id.cb_border_window);
        cb_hide_toasts = findViewById(R.id.cb_hide_toasts);
        cb_disable_bossKey = findViewById(R.id.cb_disable_bossKey);
        cb_behaviour_bossKey = findViewById(R.id.cb_behaviour_bossKey);

        sp_mouse_icon = findViewById(R.id.sp_mouse_icon);
        dsbar_mouse_size = findViewById(R.id.dsbar_mouse_size);
        dsbar_scroll_speed = findViewById(R.id.dsbar_mouse_scspeed);

        // don't like to advertise in the product, but need to mention here
        // need to increase visibility of the open source version
        gui_about.setText("MATVT v" + BuildConfig.VERSION_NAME + "\n\nThis is an open source project. It's available for free and will always be. If you find issues / would like to help in improving this project, please contribute at \nhttps://github.com/virresh/matvt");

        // render icon style dropdown
        IconStyleSpinnerAdapter iconStyleSpinnerAdapter = new IconStyleSpinnerAdapter(this, R.layout.spinner_icon_text_gui, R.id.textView, IconStyleSpinnerAdapter.getResourceList());
        sp_mouse_icon.setAdapter(iconStyleSpinnerAdapter);

        checkValues(iconStyleSpinnerAdapter);

        bt_saveBossKeyValue.setOnClickListener(view -> {
            String dat = et_override.getText().toString();
            dat = dat.replaceAll("[^0-9]", "");
            int keyValue; if (dat.isEmpty()) keyValue = KeyEvent.KEYCODE_VOLUME_MUTE;
            else keyValue = Integer.parseInt(dat);
            isBossKeyChanged();
            Helper.setOverrideStatus(this, isBossKeyChanged());
            Helper.setBossKeyValue(this, keyValue);
            bossKey = keyValue;
            Toast.makeText(this, "New Boss key is : "+keyValue, Toast.LENGTH_SHORT).show();
        });



        sp_mouse_icon.setOnItemSelectedListener(new OnItemSelectedListener() {
            // the listener is set after setting initial value to avoid echo if any
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                Context ctx = getApplicationContext();
                String style = iconStyleSpinnerAdapter.getItem(pos);
                Helper.setMouseIconPref(ctx, iconStyleSpinnerAdapter.getItem(pos));
//                Toast.makeText(ctx, "Icon style set to "+style+". Changes will take effect from next restart.", Toast.LENGTH_SHORT).show();
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
                    Helper.setMouseSizePref(ctx, progress);
//                    Toast.makeText(ctx, "Mouse size set. Changes will take effect from next restart.", Toast.LENGTH_SHORT).show();
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
                    Helper.setScrollSpeed(ctx, progress);
                    scrollSpeed = progress;
//                    Toast.makeText(ctx, "Mouse size set. Changes will take effect from next restart.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        cb_mouse_bordered.setOnCheckedChangeListener((compoundButton, b) -> {
            Helper.setMouseBordered(getApplicationContext(), b);
            PointerControl.isBordered = b;
        });

        cb_hide_toasts.setOnCheckedChangeListener(((compoundButton, value) -> {
            Helper.setHideToastsOptionEnabled(getApplicationContext(), value);
            MouseEmulationEngine.isHideToastsOptionEnabled = value;
        }));

        cb_disable_bossKey.setOnCheckedChangeListener(((compoundButton, value) -> {
            Helper.setBossKeyDisabled(getApplicationContext(), value);
            MouseEmulationEngine.isBossKeyDisabled = value;
        }));

        cb_behaviour_bossKey.setOnCheckedChangeListener((((compoundButton, value) -> {
            Helper.setBossKeyBehaviour(getApplicationContext(), value);
            MouseEmulationEngine.isBossKeySetToToggle = value;
        })));

        populateText();
        findViewById(R.id.gui_setup_perm).setOnClickListener(view -> askPermissions());
    }

    private boolean isBossKeyChanged() {
        return Helper.getBossKeyValue(this) != 164;
    }

    private void checkValues(IconStyleSpinnerAdapter adapter) {
        Context ctx = getApplicationContext();
        String val = String.valueOf(Helper.getBossKeyValue(ctx));
        et_override.setText(val);
        String iconStyle = Helper.getMouseIconPref(ctx);
        sp_mouse_icon.setSelection(adapter.getPosition(iconStyle));

        int mouseSize = Helper.getMouseSizePref(ctx);
        dsbar_mouse_size.setProgress(Math.max(Math.min(mouseSize, dsbar_mouse_size.getMax()), 0));

        int scrollSpeed = Helper.getScrollSpeed(ctx);
        dsbar_scroll_speed.setProgress(Math.max(Math.min(scrollSpeed, dsbar_scroll_speed.getMax()), 0));

        boolean bordered = Helper.getMouseBordered(ctx);
        cb_mouse_bordered.setChecked(bordered);

        boolean toastVisibility = Helper.isHideToastOptionEnabled(ctx);

        cb_hide_toasts.setChecked(toastVisibility);


        boolean bossKeyStatus = Helper.isBossKeyDisabled(ctx);
        cb_disable_bossKey.setChecked(bossKeyStatus);

        boolean bossKeyBehaviour = Helper.isBossKeySetToToggle(ctx);
        cb_behaviour_bossKey.setChecked(bossKeyBehaviour);
    }

    private void askPermissions() {
        if (Helper.isOverlayDisabled(this)) {
            try {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                        ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } catch (Exception unused) {
                Toast.makeText(this, "Overlay Permission Handler not Found", Toast.LENGTH_SHORT).show();
            }
        }
        if (!Helper.isOverlayDisabled(this) && Helper.isAccessibilityDisabled(this)) {
            checkAccPerms();
        }
    }

    private void checkAccPerms() {
        if (Helper.isAccessibilityDisabled(this))
            try {
//                startActivity(new Intent(getPackageManager().getLeanbackLaunchIntentForPackage("com.wolf.apm").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))); HELPER APP
                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                        ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE);
            } catch (Exception exception) {
                Toast.makeText(this, "Acessibility Handler not Found", Toast.LENGTH_SHORT).show();
            }
    }

    public void populateText() {
        if (Helper.isOverlayDisabled(this))  gui_overlay_perm.setText(R.string.perm_overlay_denied);
        else gui_overlay_perm.setText(R.string.perm_overlay_allowed);

        if (Helper.isAccessibilityDisabled(this)) {
            gui_acc_perm.setText(R.string.perm_acc_denied);
            gui_acc_serv.setText(R.string.serv_acc_denied);
            gui_overlay_serv.setText(R.string.serv_overlay_denied); }
        else gui_acc_perm.setText(R.string.perm_acc_allowed);

        if (Helper.isAccessibilityDisabled(this) && Helper.isOverlayDisabled(this)) {
            gui_acc_perm.setText(R.string.perm_acc_denied);
            gui_acc_serv.setText(R.string.serv_acc_denied);
            gui_overlay_perm.setText(R.string.perm_overlay_denied);
            gui_overlay_serv.setText(R.string.serv_overlay_denied);
        }

        if (!Helper.isAccessibilityDisabled(this) && !Helper.isOverlayDisabled(this)) {
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
            if (Helper.isOverlayDisabled(this)) {
                Toast.makeText(this, "Overlay Permissions Denied", Toast.LENGTH_SHORT).show();
            } else checkAccPerms();
        if (requestCode == ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE)
            if (Helper.isAccessibilityDisabled(this)) {
                Toast.makeText(this, "Accessibility Services not running", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Checking services status
        checkServiceStatus();

        if (et_override != null)
            et_override.setText(Helper.getBossKeyValue(this)+"");
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

    public void callDetect(View view) {
        startActivity(new Intent(this, KeyDetection.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
