package io.github.virresh.matvt.engine.impl;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import io.github.virresh.matvt.helper.AppPreferences;
import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class GestureDispatchMouseEngine extends BaseEngine {


    private static String LOG_TAG = "GESTURE_MOUSE_EMULATION";

    public GestureDispatchMouseEngine(AccessibilityService service, OverlayView ov, AppPreferences appPreferences, MouseCursorView mouseCursorView) {
        super(appPreferences, ov, mouseCursorView);
        this.mService = service;
    }

    @Override
    protected int scroll(KeyEvent ke) {
        int direction = scrollCodeMap.get(ke.getKeyCode());
        PointF pointer = mPointerControl.getPointerLocation();
        mService.dispatchGesture(createSwipe(pointer, direction, 150, scrollSpeed), gestureResultCallback, null);
        return 1;
    }

    @Override
    protected int click(KeyEvent ke) {
        PointF pointer = mPointerControl.getPointerLocation();
        if (ke.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
            mService.dispatchGesture(createClick(pointer, 1), gestureResultCallback, null);
        } else if (scrollCodeMap.containsKey(ke.getKeyCode())) {
            int direction = scrollCodeMap.get(ke.getKeyCode());
            mService.dispatchGesture(createSwipe(pointer, direction, 100, scrollSpeed), gestureResultCallback, null);
        }
        return 1;
    }

    private AccessibilityService.GestureResultCallback gestureResultCallback = new AccessibilityService.GestureResultCallback() {
        @Override
        public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
            Log.i(LOG_TAG, "Dispatch Gesture Completed Succesfully! -- " + gestureDescription.getStrokeCount());
        }

        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
            Log.i(LOG_TAG, "Dispatch Gesture was cancelled! -- " + gestureDescription.getStrokeCount());
        }
    };

    private static GestureDescription createClick (PointF clickPoint, long duration) {
        final int DURATION = 1 + (int) duration;
        Path clickPath = new Path();
        clickPath.moveTo(clickPoint.x, clickPoint.y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    private static GestureDescription createSwipe (PointF originPoint, int direction, int momentum, int scrollSpeed) {
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

    /**
     * Send input via Android's gestureAPI
     * Only sends swipes
     * see {@link #createClick(PointF, long)} for clicking at a point
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
                mService.dispatchGesture(createSwipe(originPoint, direction, 20 + momentumStack, scrollSpeed), gestureResultCallback, null);
                momentumStack += 1;
                timerHandler.postDelayed(this, 30);
            }
        };
        timerHandler.postDelayed(previousRunnable, 0);
    }

    private List<AccessibilityNodeInfo> findNode (AccessibilityNodeInfo node, int action, Point pInt) {
        if (node == null) {
            node = mService.getRootInActiveWindow();
        }
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        if (node != null) {
            findNodeHelper(node, action, pInt, nodeInfos);
        }
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
        AccessibilityNodeInfo result = node;
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
}