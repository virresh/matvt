package io.github.virresh.matvt.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.github.virresh.matvt.R;
import io.github.virresh.matvt.helper.Helper;

public class GuiActivity extends Activity {

    CountDownTimer repopulate;
    TextView gui_acc_perm;
    TextView gui_acc_serv;
    TextView gui_overlay_perm;
    TextView gui_overlay_serv;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 701;
    public static int ACTION_ACCESSIBILITY_PERMISSION_REQUEST_CODE = 702;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui);
        gui_acc_perm = findViewById(R.id.gui_acc_perm);
        gui_acc_serv = findViewById(R.id.gui_acc_serv);
        gui_overlay_perm = findViewById(R.id.gui_overlay_perm);
        gui_overlay_serv = findViewById(R.id.gui_overlay_serv);
        findViewById(R.id.gui_setup).setOnClickListener(view -> askPermissions());
    }

    private void askPermissions() {
        if (Helper.isOverlayDisabled(this)) {
            try {
                startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION"), ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
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
}