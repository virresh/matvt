package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.widget.Toast;
import android.util.Log;
import android.graphics.PointF;
import java.io.IOException;

import io.github.virresh.matvt.helper.AppPreferences;
import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class HybridMouseEngine extends BaseEngine {

    private static final String LOG_TAG = "HybridMouseEngine";
    private CountDownTimer swipeGestureCoolDown;
    private boolean isQueueEmpty = true;

    public HybridMouseEngine(AppPreferences appPreferences, OverlayView ov, MouseCursorView mCursorView) {
        super(appPreferences, ov, mCursorView);
    }

    @Override
    protected int scroll(KeyEvent ke) {
        if (!isQueueEmpty) return 0;

        if (mService != null && scrollCodeMap.containsKey(ke.getKeyCode())) {
            isQueueEmpty = false;

            PointF pointer = mPointerControl.getPointerLocation();
            int direction = scrollCodeMap.get(ke.getKeyCode());
            int momentum = momentumStack; // from BaseEngine
            final int DURATION = 300 - scrollSpeed * 10; // scrollSpeed from BaseEngine

            PointF lineDirection = new PointF(
                    pointer.x + (momentum + 75) * PointerControl.dirX[direction],
                    pointer.y + (momentum + 75) * PointerControl.dirY[direction]);

            shellSwipe((int) pointer.x, (int) pointer.y, (int) lineDirection.x, (int) lineDirection.y, DURATION);

            swipeGestureCoolDown = new CountDownTimer(DURATION + 200, 100) {
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    isQueueEmpty = true;
                }
            };
            swipeGestureCoolDown.start();

            momentumStack += 1;
        }
        return 0;
    }

    @Override
    protected int click(KeyEvent ke) {
        if (mService != null) {
            PointF pointer = mPointerControl.getPointerLocation();
            shellTap((int) pointer.x, (int) pointer.y);
        }
        return 0;
    }

    private void shellTap(int x, int y) {
        executeShellCommand("input tap " + x + " " + y);
    }

    private void shellSwipe(int x1, int y1, int x2, int y2, int duration) {
        executeShellCommand("input swipe " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + duration);
    }

    private void executeShellCommand(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to execute shell command: " + command, e);
        }
    }
}
