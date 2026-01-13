package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.virresh.matvt.helper.AppPreferences;
import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public abstract class BaseEngine implements MouseEmulationEngine {

    public enum OperatingMode {
        DPAD,
        MOUSE,
        SCROLL
    }

    private boolean DPAD_SELECT_PRESSED = false;
    private static String LOG_TAG = "MOUSE_EMULATION";
    protected Handler timerHandler;

    protected Runnable previousRunnable;
    CountDownTimer waitToChange;

    CountDownTimer disappearTimer;

    public OperatingMode operatingMode = OperatingMode.DPAD;

    // service which started this engine
    protected AccessibilityService mService;
    protected final AppPreferences appPreferences;

    protected final PointerControl mPointerControl;

    protected int momentumStack;

    public int bossKey;

    protected int scrollSpeed;

    public boolean isBossKeyDisabled;

    public boolean isBossKeySetToToggle;

    private Point DPAD_Center_Init_Point = new Point();

    // tells which keycodes correspond to which pointer movement in scroll and movement mode
    // scroll directions don't match keycode instruction because that's how swiping works
    protected static final Map<Integer, Integer> scrollCodeMap;
    static {
        Map<Integer, Integer> integerMap = new HashMap<>();
        integerMap.put(KeyEvent.KEYCODE_DPAD_UP, PointerControl.DOWN);
        integerMap.put(KeyEvent.KEYCODE_DPAD_DOWN, PointerControl.UP);
        integerMap.put(KeyEvent.KEYCODE_DPAD_LEFT, PointerControl.RIGHT);
        integerMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, PointerControl.LEFT);
        integerMap.put(KeyEvent.KEYCODE_PROG_GREEN, PointerControl.DOWN);
        integerMap.put(KeyEvent.KEYCODE_PROG_RED, PointerControl.UP);
        integerMap.put(KeyEvent.KEYCODE_PROG_BLUE, PointerControl.RIGHT);
        integerMap.put(KeyEvent.KEYCODE_PROG_YELLOW, PointerControl.LEFT);
        scrollCodeMap = Collections.unmodifiableMap(integerMap);
    }

    protected static final Map<Integer, Integer> movementCodeMap;
    static {
        Map<Integer, Integer> integerMap = new HashMap<>();
        integerMap.put(KeyEvent.KEYCODE_DPAD_UP, PointerControl.UP);
        integerMap.put(KeyEvent.KEYCODE_DPAD_DOWN, PointerControl.DOWN);
        integerMap.put(KeyEvent.KEYCODE_DPAD_LEFT, PointerControl.LEFT);
        integerMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, PointerControl.RIGHT);
        movementCodeMap = Collections.unmodifiableMap(integerMap);
    }

    protected static final Set<Integer> actionableKeyMap;
    static {
        Set<Integer> integerSet = new HashSet<>();
        integerSet.add(KeyEvent.KEYCODE_DPAD_UP);
        integerSet.add(KeyEvent.KEYCODE_DPAD_DOWN);
        integerSet.add(KeyEvent.KEYCODE_DPAD_LEFT);
        integerSet.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        integerSet.add(KeyEvent.KEYCODE_PROG_GREEN);
        integerSet.add(KeyEvent.KEYCODE_PROG_YELLOW);
        integerSet.add(KeyEvent.KEYCODE_PROG_BLUE);
        integerSet.add(KeyEvent.KEYCODE_PROG_RED);
        actionableKeyMap = Collections.unmodifiableSet(integerSet);
    }

    protected static final Set<Integer> colorSet;
    static {
        Set<Integer> integerSet = new HashSet<>();
        integerSet.add(KeyEvent.KEYCODE_PROG_GREEN);
        integerSet.add(KeyEvent.KEYCODE_PROG_YELLOW);
        integerSet.add(KeyEvent.KEYCODE_PROG_BLUE);
        integerSet.add(KeyEvent.KEYCODE_PROG_RED);
        colorSet = Collections.unmodifiableSet(integerSet);
    }

    public BaseEngine (AppPreferences appPreferences, OverlayView ov, MouseCursorView mCursorView) {
        this.appPreferences = appPreferences;
        momentumStack = 0;
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        mPointerControl.disappear();
        Log.i(LOG_TAG, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    abstract protected int scroll(KeyEvent ke);
    abstract protected int click(KeyEvent ke);

    private void attachTimer (final int direction) {
        if (previousRunnable != null) {
            detachPreviousTimer();
        }
        previousRunnable = new Runnable() {
            @Override
            public void run() {
                mPointerControl.reappear();
                mPointerControl.move(direction, momentumStack);
                momentumStack += 1;
                timerHandler.postDelayed(this, 30);
            }
        };
        timerHandler.postDelayed(previousRunnable, 0);
    }


    /**
     * Auto Disappear mouse after some duration and reset momentum
     */
    protected void detachPreviousTimer () {
        if (disappearTimer != null) {
            disappearTimer.cancel();
        }
        if (previousRunnable != null) {
            timerHandler.removeCallbacks(previousRunnable);
            momentumStack = 0;
            disappearTimer = new CountDownTimer(10000, 10000) {
                @Override
                public void onTick(long l) { }

                @Override
                public void onFinish() {
                    mPointerControl.disappear();
                }
            };
            disappearTimer.start();
        }
    }

    @Override public void init(@NonNull AccessibilityService s) {
        // Called only once, during service initialisation.
        this.mService = s;
        mPointerControl.reset();
        timerHandler = new Handler();
        operatingMode = OperatingMode.DPAD;
    }

    @Override
    public void updateFromPreferences() {
        // Can be called multiple times, please avoid all side-effects / static hacks.
        scrollSpeed = appPreferences.getScrollSpeed();
        isBossKeyDisabled = appPreferences.isBossKeyDisabled();
        isBossKeySetToToggle = appPreferences.isBossKeySetToToggle();
        bossKey = appPreferences.getBossKeyValue();
        boolean isBordered = appPreferences.getMouseBordered();

        mPointerControl.setIsBordered(isBordered);
        mPointerControl.updateCursorViewPreferences();

        Log.i(LOG_TAG, "Configuration -- Scroll Speed " + scrollSpeed);
        Log.i(LOG_TAG, "Configuration -- Boss Key Disabled " + isBossKeyDisabled);
        Log.i(LOG_TAG, "Configuration -- Boss Key Toggleable " + isBossKeySetToToggle);
        Log.i(LOG_TAG, "Configuration -- Is Bordered " + isBordered);
        Log.i(LOG_TAG, "Configuration -- Boss Key value " + bossKey);
    }

    @Override
    public void destroy() {
        this.mService = null;
    }

    protected boolean handleBossKey(KeyEvent keyEvent) {
        if (isBossKeyDisabled) {
            return false;
        }

        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (isBossKeySetToToggle) {
                // 3-way toggle: DPAD -> MOUSE -> SCROLL -> DPAD
                switch (operatingMode) {
                    case DPAD:
                        setOperatingMode(OperatingMode.MOUSE);
                        break;
                    case MOUSE:
                        setOperatingMode(OperatingMode.SCROLL);
                        break;
                    case SCROLL:
                        setOperatingMode(OperatingMode.DPAD);
                        break;
                }
            } else {
                // Long-press to toggle between DPAD and MOUSE/SCROLL
                waitToChange();
                if (operatingMode != OperatingMode.DPAD) {
                    setOperatingMode(operatingMode == OperatingMode.MOUSE ? OperatingMode.SCROLL : OperatingMode.MOUSE);
                }
            }
            return true; // Consume the event
        } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (!isBossKeySetToToggle && waitToChange != null) {
                waitToChange.cancel();
                return operatingMode != OperatingMode.DPAD;
            }
        }

        return !isBossKeySetToToggle;
    }

    @Override public boolean perform (KeyEvent keyEvent) {
        // toggle mouse mode if going via bossKey
        if (keyEvent.getKeyCode() == bossKey) {
            return handleBossKey(keyEvent);
        }

        // keep full functionality on full size remotes
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_INFO) {
            setOperatingMode(operatingMode == OperatingMode.DPAD ? OperatingMode.MOUSE : OperatingMode.DPAD);
            return true;
        }

        if (operatingMode == OperatingMode.DPAD) {
            // mouse is disabled, don't do anything and let the system consume this event
            return false;
        }
        boolean consumed = false;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            if (scrollCodeMap.containsKey(keyEvent.getKeyCode())) {
                if (operatingMode == OperatingMode.SCROLL || colorSet.contains(keyEvent.getKeyCode()))
                    scroll(keyEvent);
                else if (operatingMode == OperatingMode.MOUSE && mPointerControl.isStuckAtSide() != 0 && keyEvent.getKeyCode() == mPointerControl.isStuckAtSide())
                    click(keyEvent);
                else if (movementCodeMap.containsKey(keyEvent.getKeyCode()))
                    attachTimer(movementCodeMap.get(keyEvent.getKeyCode()));
                consumed = true;
            } else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                // just consume this event to prevent propagation
                DPAD_Center_Init_Point = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
                DPAD_SELECT_PRESSED = true;
                consumed = true;
            }
        } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            // key released, cancel any ongoing effects and clean-up
            if (actionableKeyMap.contains(keyEvent.getKeyCode()) || keyEvent.getKeyCode() == bossKey) {
                detachPreviousTimer();
                consumed = true;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                DPAD_SELECT_PRESSED = false;
                detachPreviousTimer();
                Point pInt = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
                if (DPAD_Center_Init_Point.equals(pInt)) {
                    click(keyEvent);
                } else{
                    //Implement Drag Function here
                    Log.i(LOG_TAG, "Trying to drag. This is not available.");
                }
            }
        }
        return consumed;
    }

    public void setOperatingMode(OperatingMode newMode) {
        if (operatingMode == newMode) return;

        operatingMode = newMode;
        switch (newMode) {
            case DPAD:
                mPointerControl.disappear();
                showToast("D-Pad Mode");
                break;
            case MOUSE:
                mPointerControl.reset();
                mPointerControl.reappear();
                showToast("Mouse Mode");
                break;
            case SCROLL:
                showToast("Scroll Mode");
                break;
        }
    }

    private void showToast(String message) {
        if (mService != null && !appPreferences.isHideToastOptionEnabled()) {
            Toast.makeText(mService, message, Toast.LENGTH_SHORT).show();
        } else {
            Log.i(LOG_TAG, message);
        }
    }

    /**
     * Simple count down timer for checking keypress duration
     */
    private void waitToChange() {
        waitToChange = new CountDownTimer(800, 800) {
            @Override
            public void onTick(long l) { }
            @Override
            public void onFinish() {
                setOperatingMode(operatingMode == OperatingMode.DPAD ? OperatingMode.MOUSE : OperatingMode.DPAD);
            }
        };
        waitToChange.start();
    }
}