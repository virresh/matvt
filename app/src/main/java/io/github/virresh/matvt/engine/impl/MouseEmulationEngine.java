package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class MouseEmulationEngine {

    private static String LOG_TAG = "MOUSE_EMULATION";

    // service which started this engine
    private AccessibilityService mService;

    // overlay view for drawing mouse
    private OverlayView mOverlayView;
    protected OverlayView getOverlayView() {return mOverlayView;}

    private MouseCursorView mCursorView;

    private PointerControl mPointerControl;

    private KeyEvent lastEvent;
    private int momentumStack;

    private boolean isEnabled;

    private Handler timerHandler;
    private Runnable previousRunnable;

    public MouseEmulationEngine (Context c, OverlayView ov) {
        momentumStack = 0;
        this.mOverlayView = ov;
        mCursorView = new MouseCursorView(c);
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        mPointerControl.disappear();
        Log.i(LOG_TAG, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    public boolean init(@NonNull AccessibilityService s) {
        this.mService = s;
        mPointerControl.reset();
        lastEvent = null;
        timerHandler = new Handler();
        isEnabled = false;
        return true;
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

    private void attachActionable (final int action, final AccessibilityNodeInfo node) {
        if (previousRunnable != null) {
            detachPreviousTimer();
        }
        previousRunnable = new Runnable() {
            @Override
            public void run() {
                mPointerControl.reappear();
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                node.performAction(action);
                node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                timerHandler.postDelayed(this, 30);
            }
        };
        timerHandler.postDelayed(previousRunnable, 0);
    }

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
            previousRunnable = new Runnable() {
                @Override
                public void run() {
                    mPointerControl.disappear();
                }
            };
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
        // info key get's special treatment
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_INFO) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (isEnabled) {
                    // mouse already enabled, disable it and make it go away
                    this.isEnabled = false;
                    mPointerControl.disappear();
                    Toast.makeText(mService, "Mouse Gone", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    // mouse is disabled, enable it, reset it and show it
                    this.isEnabled = true;
                    mPointerControl.reset();
                    mPointerControl.reappear();
                    Toast.makeText(mService, "Mouse On", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        if (!isEnabled) {
            // don't consume anything
            return false;
        }
        boolean consumed = false;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                attachTimer(PointerControl.UP);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                attachTimer(PointerControl.DOWN);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                attachTimer(PointerControl.LEFT);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                attachTimer(PointerControl.RIGHT);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
                // backward or swipe up
//                int action = AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
//                Point pInt = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
//                AccessibilityNodeInfo hitNode = findNode(null, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, pInt);
//                if (hitNode != null) {
//                    attachActionable(action, hitNode);
//                    consumed = true;
//                }
                attachGesture(mPointerControl.getPointerLocation(), PointerControl.UP);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
                // forward or swipe down
                attachGesture(mPointerControl.getPointerLocation(), PointerControl.DOWN);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
                // swipe left
                attachGesture(mPointerControl.getPointerLocation(), PointerControl.LEFT);
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) {
                // swipe right
                attachGesture(mPointerControl.getPointerLocation(), PointerControl.RIGHT);
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
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_RED
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
                detachPreviousTimer();
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                detachPreviousTimer();
                int action = AccessibilityNodeInfo.ACTION_CLICK;
                if (keyEvent.getEventTime() - keyEvent.getDownTime() > 500) {
                    action = AccessibilityNodeInfo.ACTION_LONG_CLICK;
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
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (keyEvent.getEventTime() - keyEvent.getDownTime() > 500) {
                    this.mService.disableSelf();
                    consumed = true;
                }
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
                // forward or swipe down
                detachPreviousTimer();
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
                // swipe left
                detachPreviousTimer();
                consumed = true;
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) {
                // swipe right
                detachPreviousTimer();
                consumed = true;
            }
        }
        Log.i(LOG_TAG, "Consumed by mouse Emulator? " + consumed);
        if (consumed) {
            lastEvent = keyEvent;
        }
        return consumed;
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
