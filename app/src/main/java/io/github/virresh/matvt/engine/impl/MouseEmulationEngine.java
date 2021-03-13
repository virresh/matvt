package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class MouseEmulationEngine {

    private static String LOG_TAG = "MOUSE_EMULATION";

    CountDownTimer waitToChange;

    private boolean isInScrollMode = false;

    // service which started this engine
    private AccessibilityService mService;

    private final PointerControl mPointerControl;

    private int momentumStack;

    private boolean isEnabled;

    public static int bossKey;

    public static int scrollSpeed;

    private Handler timerHandler;

    private Runnable previousRunnable;

    // tells which keycodes correspond to which pointer movement in scroll and movement mode
    // scroll directions don't match keycode instruction because that's how swiping works
    private static final Map<Integer, Integer> scrollCodeMap;
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

    private static final Map<Integer, Integer> movementCodeMap;
    static {
        Map<Integer, Integer> integerMap = new HashMap<>();
        integerMap.put(KeyEvent.KEYCODE_DPAD_UP, PointerControl.UP);
        integerMap.put(KeyEvent.KEYCODE_DPAD_DOWN, PointerControl.DOWN);
        integerMap.put(KeyEvent.KEYCODE_DPAD_LEFT, PointerControl.LEFT);
        integerMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, PointerControl.RIGHT);
        movementCodeMap = Collections.unmodifiableMap(integerMap);
    }

    private static final Set<Integer> actionableKeyMap;
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

    private static final Set<Integer> colorSet;
    static {
        Set<Integer> integerSet = new HashSet<>();
        integerSet.add(KeyEvent.KEYCODE_PROG_GREEN);
        integerSet.add(KeyEvent.KEYCODE_PROG_YELLOW);
        integerSet.add(KeyEvent.KEYCODE_PROG_BLUE);
        integerSet.add(KeyEvent.KEYCODE_PROG_RED);
        colorSet = Collections.unmodifiableSet(integerSet);
    }

    public MouseEmulationEngine (Context c, OverlayView ov) {
        momentumStack = 0;
        // overlay view for drawing mouse
        MouseCursorView mCursorView = new MouseCursorView(c);
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        mPointerControl.disappear();
        Log.i(LOG_TAG, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    public void init(@NonNull AccessibilityService s) {
        this.mService = s;
        mPointerControl.reset();
        timerHandler = new Handler();
        isEnabled = false;
    }

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
     * Send input via Android's gestureAPI
     * Only sends swipes
     * see {@link MouseEmulationEngine#createClick(PointF, long)} for clicking at a point
     * @param originPoint
     * @param direction
     */
    private void attachGesture (final PointF originPoint, final int direction) {
        if (previousRunnable != null) {
            detachPreviousTimer();
        }
        previousRunnable = new Runnable() {
            @Override
            public void run() {
                mPointerControl.reappear();
                mService.dispatchGesture(createSwipe(originPoint, direction, 20 + momentumStack), null, null);
                momentumStack += 1;
                timerHandler.postDelayed(this, 30);
            }
        };
        timerHandler.postDelayed(previousRunnable, 0);
    }

    /**
     * Auto Disappear mouse after some duration and reset momentum
     */
    private void detachPreviousTimer () {
        if (previousRunnable != null) {
            timerHandler.removeCallbacks(previousRunnable);
            previousRunnable = mPointerControl::disappear;
            timerHandler.postDelayed(previousRunnable, 30000);
            momentumStack = 0;
        }
    }

    private static GestureDescription createClick (PointF clickPoint, long duration) {
        final int DURATION = 1 + (int) duration;
        Log.i(LOG_TAG, "Actual Duration used -- " + DURATION);
        Path clickPath = new Path();
        clickPath.moveTo(clickPoint.x, clickPoint.y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    private static GestureDescription createSwipe (PointF originPoint, int direction, int momentum) {
        final int DURATION = scrollSpeed + 8;
        Path clickPath = new Path();
        PointF lineDirection = new PointF(originPoint.x + momentum * PointerControl.dirX[direction], originPoint.y + momentum * PointerControl.dirY[direction]);
        clickPath.moveTo(originPoint.x, originPoint.y);
        clickPath.lineTo(lineDirection.x, lineDirection.y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    public boolean perform (KeyEvent keyEvent) {

        // toggle mouse mode if going via bossKey
        if (keyEvent.getKeyCode() == bossKey) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (waitToChange != null) {
                    // cancel change countdown
                    waitToChange.cancel();
                    if (isEnabled) return true;
                }
            }
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                waitToChange();
                if (isEnabled){
                    isInScrollMode = !isInScrollMode;
                    Toast.makeText(mService, "Scroll Mode " + isInScrollMode, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        // keep full functionality on full size remotes
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_INFO) {
            if (this.isEnabled) {
                // mouse already enabled, disable it and make it go away
                this.isEnabled = false;
                mPointerControl.disappear();
                Toast.makeText(mService, "Dpad Mode", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                // mouse is disabled, enable it, reset it and show it
                this.isEnabled = true;
                mPointerControl.reset();
                mPointerControl.reappear();
                Toast.makeText(mService, "Mouse/Scroll", Toast.LENGTH_SHORT).show();
            }
        }

        if (!isEnabled) {
            // mouse is disabled, don't do anything and let the system consume this event
            return false;
        }
        boolean consumed = false;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            if (scrollCodeMap.containsKey(keyEvent.getKeyCode())) {
                if (isInScrollMode || colorSet.contains(keyEvent.getKeyCode())) {
                    attachGesture(mPointerControl.getPointerLocation(), scrollCodeMap.get(keyEvent.getKeyCode()));
                }
                else if (movementCodeMap.containsKey(keyEvent.getKeyCode())){
                    attachTimer(movementCodeMap.get(keyEvent.getKeyCode()));
                }
                consumed = true;
            }
            else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                // just consume this event to prevent propagation
                consumed = true;
            }
        }
        else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            // key released, cancel any ongoing effects and clean-up
            // since bossKey is also now a part of this stuff, consume it if events enabled
            if (actionableKeyMap.contains(keyEvent.getKeyCode())
                    || keyEvent.getKeyCode() == bossKey) {
                detachPreviousTimer();
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                detachPreviousTimer();
//                if (keyEvent.getEventTime() - keyEvent.getDownTime() > 500) {
                    // unreliable long click event if button was pressed for more than 500 ms
                int action = AccessibilityNodeInfo.ACTION_CLICK;
                Point pInt = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
                List<AccessibilityWindowInfo> windowList= mService.getWindows();
                boolean wasIME = false;
                for (AccessibilityWindowInfo window : windowList) {
                    if (consumed || wasIME) {
                        break;
                    }
                    List<AccessibilityNodeInfo> nodeHierarchy = findNode(window.getRoot(), action, pInt);
                    for (int i=nodeHierarchy.size()-1; i>=0; i--){
                        if (consumed) return consumed;
                        AccessibilityNodeInfo hitNode = nodeHierarchy.get(i);
                        List<AccessibilityNodeInfo.AccessibilityAction> availableActions = hitNode.getActionList();
                        if (availableActions.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_FOCUS)){
                            hitNode.performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                        }
//                        if (hitNode.isFocused() && availableActions.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SELECT)){
//                            hitNode.performAction(AccessibilityNodeInfo.ACTION_SELECT);
//                        }
//                        if (hitNode.isFocused() && availableActions.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)){
//                            consumed = hitNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        }
                        if (window.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
                            wasIME = true;
                            consumed = hitNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }

                        if (hitNode.getPackageName().equals("com.google.android.tvlauncher") && availableActions.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                            consumed = hitNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
                if (!consumed || !wasIME) {
                    mService.dispatchGesture(createClick(mPointerControl.getPointerLocation(), keyEvent.getEventTime() - keyEvent.getDownTime()), null, null);
                }
            }
        }
        return consumed;
    }

    private void setMouseModeEnabled(boolean enable) {
        if (enable) {
            // Enable Mouse Mode
            this.isEnabled = true;
            isInScrollMode = false;
            mPointerControl.reset();
            mPointerControl.reappear();
            Toast.makeText(mService, "Mouse Mode", Toast.LENGTH_SHORT).show();
        }
        else {
            // Disable Mouse Mode
            this.isEnabled = false;
            mPointerControl.disappear();
            Toast.makeText(mService, "D-Pad Mode", Toast.LENGTH_SHORT).show();
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
                setMouseModeEnabled(!isEnabled);
            }
        };
        waitToChange.start();
    }

    //// below code is for supporting legacy devices as per my understanding of evia face cam source
    //// this is only used for long clicks here and isn't exactly something reliable
    //// leaving it in for reference just in case needed in future, because looking up face cam
    //// app's source might be a daunting task

    private List<AccessibilityNodeInfo> findNode (AccessibilityNodeInfo node, int action, Point pInt) {
        if (node == null) {
            node = mService.getRootInActiveWindow();
        }
        if (node == null) {
            Log.i(LOG_TAG, "Root Node ======>>>>>" + ((node != null) ? node.toString() : "null"));
        }
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        Log.i(LOG_TAG, "Node found ?" + ((node != null) ? node.toString() : "null"));
        node = findNodeHelper(node, action, pInt, nodeInfos);
        Log.i(LOG_TAG, "Node found ?" + ((node != null) ? node.toString() : "null"));
        Log.i(LOG_TAG, "Number of Nodes ?=>>>>> " + nodeInfos.size());
        return nodeInfos;
    }

    private AccessibilityNodeInfo findNodeHelper (AccessibilityNodeInfo node, int action, Point pInt, List<AccessibilityNodeInfo> nodeList) {
        if (node == null) {
            return null;
        }
        Rect tmp = new Rect();
        node.getBoundsInScreen(tmp);
        if (!tmp.contains(pInt.x, pInt.y)) {
            // node doesn't contain cursor
            return null;
        }
        // node contains cursor, add to node hierarchy
        nodeList.add(node);
        AccessibilityNodeInfo result = null;
        result = node;
//        if ((node.getActions() & action) != 0 && node != null) {
//            // possible to use this one, but keep searching children as well
//            nodeList.add(node);
//        }
        int childCount = node.getChildCount();
        for (int i=0; i<childCount; i++) {
            AccessibilityNodeInfo child = findNodeHelper(node.getChild(i), action, pInt, nodeList);
            if (child != null) {
                // always picks the last innermost clickable child
                result = child;
            }
        }
        return result;
    }

    /** Not used
     * Letting this stay here just in case the code needs porting back to an obsolete version
     * sometime in future
     //    private void attachActionable (final int action, final AccessibilityNodeInfo node) {
     //        if (previousRunnable != null) {
     //            detachPreviousTimer();
     //        }
     //        previousRunnable = new Runnable() {
     //            @Override
     //            public void run() {
     //                mPointerControl.reappear();
     //                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
     //                node.performAction(action);
     //                node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
     //                timerHandler.postDelayed(this, 30);
     //            }
     //        };
     //        timerHandler.postDelayed(previousRunnable, 0);
     //    }
     **/
}
