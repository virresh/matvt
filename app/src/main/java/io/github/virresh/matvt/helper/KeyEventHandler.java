package io.github.virresh.matvt.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import io.github.virresh.matvt.R;
import io.github.virresh.matvt.services.MouseEventService;

public class KeyEventHandler {
    private final AppPreferences appPreferences;
    private final Context context;
    private final MouseEventService mouseEventService;

    public KeyEventHandler(MouseEventService service) {
        this.context = service;
        this.mouseEventService = service;
        this.appPreferences = new AppPreferences(context);
    }

    public void handleKeyEvent(KeyEvent event) {
        if (event.getEventTime() - event.getDownTime() > 1000) {
            showChangeBossKeyDialog(event.getKeyCode());
        }
    }

    private void showChangeBossKeyDialog(int keyCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Wolf_Alert_Disp);
        builder.setTitle(R.string.confirm_changes_title);
        builder.setMessage(context.getString(R.string.confirm_set_boss_key_message, keyCode));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                appPreferences.setBossKeyDisabled(false);
                appPreferences.setOverrideStatus(true);
                appPreferences.setBossKeyValue(keyCode);
                mouseEventService.updatePreferences();
                Toast.makeText(context, context.getString(R.string.new_boss_key_toast, keyCode), Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alert.show();
    }
}
