package io.github.virresh.matvt.engine.impl;

import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class PointerControl {
    private static String LOG_TAG = "POINTER_CONTROL";

    // constants
    public static int LEFT = 0;
    public static int UP = 1;
    public static int RIGHT = 2;
    public static int DOWN = 3;

    // Left, Up, Right, Down
    public static int[] dirX = {-1, 0, 1,  0};
    public static int[] dirY = { 0, -1, 0, 1};

    // pointer location in screen coordinates
    private final PointF mPointerLocation = new PointF();

    // view to display the pointer
    private final OverlayView mPointerLayerView;

    // cursor view
    private final MouseCursorView mCursorView;

    // constructor
    PointerControl(@NonNull OverlayView pv, MouseCursorView mv){
        mPointerLayerView = pv;
        mCursorView = mv;
        reset();
    }

    /**
     * Reset pointer location by centering it
     */
    public void reset () {
        mCursorView.updateFromPreferences();
        mPointerLocation.x = mPointerLayerView.getWidth() / 2f;
        mPointerLocation.y = mPointerLayerView.getHeight() / 2f;
        Log.i(LOG_TAG, "View W, H: " + mPointerLayerView.getWidth() + " " + mPointerLayerView.getHeight());
        Log.i(LOG_TAG, "Location X, Y: " + mPointerLocation.x + " " + mPointerLocation.y);
        mCursorView.updatePosition(mPointerLocation);
    }

    public void disappear () {
        if (mCursorView.getVisibility() == View.VISIBLE) {
            mCursorView.setVisibility(View.INVISIBLE);
        }
    }

    public void reappear () {
        if (mCursorView.getVisibility() == View.INVISIBLE) {
            mCursorView.setVisibility(View.VISIBLE);
        }
    }

    public void move (int direction, int momentum) {
        int movementX = (int) (dirX[direction] * ((momentum)));
        int movementY = (int) (dirY[direction] * ((momentum)));

        mPointerLocation.x += movementX;
        if (mPointerLocation.x > mPointerLayerView.getWidth()) {
            mPointerLocation.x -= mPointerLayerView.getWidth();
        }
        else if (mPointerLocation.x < 0) {
            mPointerLocation.x += mPointerLayerView.getWidth();
        }

        mPointerLocation.y += movementY;
        if (mPointerLocation.y > mPointerLayerView.getHeight()) {
            mPointerLocation.y -= mPointerLayerView.getHeight();
        }
        else if (mPointerLocation.y < 0) {
            mPointerLocation.y += mPointerLayerView.getHeight();
        }

        mCursorView.updatePosition(mPointerLocation);
    }

    @NonNull
    PointF getPointerLocation() {
        return mPointerLocation;
    }
}