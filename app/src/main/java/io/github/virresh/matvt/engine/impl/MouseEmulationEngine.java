package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

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

    private Handler timerHandler;
    private Runnable previousRunnable;

    public MouseEmulationEngine (Context c, OverlayView ov) {
        momentumStack = 0;
        this.mOverlayView = ov;
        mCursorView = new MouseCursorView(c);
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        Log.i(LOG_TAG, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    public boolean init(@NonNull AccessibilityService s) {
        this.mService = s;
        mPointerControl.reset();
        lastEvent = null;
        timerHandler = new Handler();
        return true;
    }

    private void attachTimer (final int direction) {
        if (previousRunnable != null) {
            detachPreviousTimer();
        }
        previousRunnable = new Runnable() {
            @Override
            public void run() {
                mPointerControl.move(direction, momentumStack);
                momentumStack += 1;
                timerHandler.postDelayed(this, 30);
            }
        };
        timerHandler.postDelayed(previousRunnable, 0);
    }

    private void detachPreviousTimer () {
        if (previousRunnable != null) {
            timerHandler.removeCallbacks(previousRunnable);
            momentumStack = 0;
        }
    }

    public boolean perform (KeyEvent keyEvent) {
        boolean consumed = false;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                attachTimer(PointerControl.UP);
                consumed = true;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                attachTimer(PointerControl.DOWN);
                consumed = true;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                attachTimer(PointerControl.LEFT);
                consumed = true;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                attachTimer(PointerControl.RIGHT);
                consumed = true;
            }
        }
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                detachPreviousTimer();
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                Point pInt = new Point((int) mPointerControl.getPointerLocation().x, (int) mPointerControl.getPointerLocation().y);
                AccessibilityNodeInfo hitNode = findNode(null, AccessibilityNodeInfo.ACTION_CLICK, pInt);
                if (hitNode != null) {
                    consumed = hitNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (keyEvent.getEventTime() - keyEvent.getDownTime() > 1000) {
                    this.mService.disableSelf();
                    consumed = true;
                }
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
                mPointerControl.reset();
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
        if ((node.getActions() & action) != 0) {
            // possible to use this one, but keep searching children as well
            result = node;
        }
        int childCount = node.getChildCount();
        for (int i=0; i<childCount; i++) {
            AccessibilityNodeInfo child = findNode(node.getChild(i), action, pInt);
            if (child != null) {
                // always picks the last innermost clickable child
                result = child;
            }
        }
        return result;
    }
}
