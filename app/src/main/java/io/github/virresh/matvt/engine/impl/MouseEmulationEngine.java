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
import android.widget.Toast;

import androidx.annotation.NonNull;

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

    private Handler timerHandler;

    private Runnable previousRunnable;

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

    /** Not used
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

    private void detachPreviousTimer () {
        if (previousRunnable != null) {
            timerHandler.removeCallbacks(previousRunnable);
            previousRunnable = mPointerControl::disappear;
            timerHandler.postDelayed(previousRunnable, 30000);
            momentumStack = 0;
        }
    }

    private static GestureDescription createClick (PointF clickPoint) {
        final int DURATION = 1;
        Path clickPath = new Path();
        clickPath.moveTo(clickPoint.x, clickPoint.y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    private static GestureDescription createSwipe (PointF originPoint, int direction, int momentum) {
        final int DURATION = 10;
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

        if (keyEvent.getKeyCode() == bossKey) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (waitToChange != null) {
                    waitToChange.cancel();
                    if (isEnabled) return true;
                }
            }
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                waitToChange();
                if (isEnabled){
                    isInScrollMode = !isInScrollMode;
                    return true;
                }
            }
        }
        if (!isEnabled) {
            return false;
        }
        boolean consumed = false;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (isInScrollMode) attachGesture(mPointerControl.getPointerLocation(), PointerControl.DOWN);
                else attachTimer(PointerControl.UP);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (isInScrollMode) attachGesture(mPointerControl.getPointerLocation(), PointerControl.UP);
                else attachTimer(PointerControl.DOWN);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInScrollMode) attachGesture(mPointerControl.getPointerLocation(), PointerControl.RIGHT);
                else attachTimer(PointerControl.LEFT);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (isInScrollMode) attachGesture(mPointerControl.getPointerLocation(), PointerControl.LEFT);
                else attachTimer(PointerControl.RIGHT);
                consumed = true;
            }
            else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                // just consume this event to prevent propagation
                consumed = true;
            }
        }
        else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyEvent.getKeyCode() == bossKey) {
                detachPreviousTimer();
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                detachPreviousTimer();
                if (keyEvent.getEventTime() - keyEvent.getDownTime() > 500) {
                    int action = AccessibilityNodeInfo.ACTION_LONG_CLICK;
                    Point pInt = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
                    AccessibilityNodeInfo hitNode = findNode(null, action, pInt);

                    if (hitNode != null) {
                        hitNode.performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                        consumed = hitNode.performAction(action);
                    }
                }
                else {
                    mService.dispatchGesture(createClick(mPointerControl.getPointerLocation()), null, null);
                    return false;
                }

            }
        }
        return consumed;
    }

    private void setMouseModeEnabled(boolean enable) { if (enable) {
        // Enable Mouse Mode
        this.isEnabled = true;
        isInScrollMode = false;
        mPointerControl.reset();
        mPointerControl.reappear();
        Toast.makeText(mService, "Mouse Mode", Toast.LENGTH_SHORT).show();
    } else {
        //Disable Mouse Mode
        this.isEnabled = false;
        mPointerControl.disappear();
        Toast.makeText(mService, "D-Pad Mode", Toast.LENGTH_SHORT).show();
    } }

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

    private AccessibilityNodeInfo findNode (AccessibilityNodeInfo node, int action, Point pInt) {
        if (node == null) {
            node = mService.getRootInActiveWindow();
        }
        Log.i(LOG_TAG, "Node found ?" + ((node != null) ? node.toString() : "null"));
        node = findNodeHelper(node, action, pInt);
        Log.i(LOG_TAG, "Node found ?" + ((node != null) ? node.toString() : "null"));
        return node;
    }

    private AccessibilityNodeInfo findNodeHelper (AccessibilityNodeInfo node, int action, Point pInt) {
        if (node == null) {
            return null;
        }
        Rect tmp = new Rect();
        node.getBoundsInScreen(tmp);
        if (!tmp.contains(pInt.x, pInt.y)) {
            // node doesn't contain cursor
            return null;
        }
        AccessibilityNodeInfo result = null;
        result = node;
//        if ((node.getActions() & action) != 0) {
//            // possible to use this one, but keep searching children as well
//            result = node;
//        }
        int childCount = node.getChildCount();
        for (int i=0; i<childCount; i++) {
            AccessibilityNodeInfo child = findNodeHelper(node.getChild(i), action, pInt);
            if (child != null) {
                // always picks the last innermost clickable child
                result = child;
            }
        }
        return result;
    }
}
