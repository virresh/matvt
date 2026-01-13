package io.github.virresh.matvt.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import io.github.virresh.matvt.R;

public class KeyDetection extends AppCompatActivity {

    public static final String ACTION_KEY_EVENT = "io.github.virresh.matvt.ACTION_KEY_EVENT";
    public static final String EXTRA_KEY_EVENT = "extra_key_event";
    public static final String EXTRA_KEY_ACTION = "extra_key_action";

    private TextView textView;

    private final BroadcastReceiver keyEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_KEY_EVENT.equals(intent.getAction())) {
                int action = intent.getIntExtra(EXTRA_KEY_ACTION, -1);
                KeyEvent event = intent.getParcelableExtra(EXTRA_KEY_EVENT);
                if (action == KeyEvent.ACTION_DOWN) {
                    textView.setText(String.valueOf(event.getKeyCode()));
                } else if (action == KeyEvent.ACTION_UP) {
                    textView.setText(" ");
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_detection);
        textView = findViewById(R.id.pressed_key);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_KEY_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(keyEventReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyEventReceiver);
    }
}