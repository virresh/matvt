package io.github.virresh.matvt.engine.impl;

import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.virresh.matvt.view.MouseCursorView;
import io.github.virresh.matvt.view.OverlayView;

public class MouseEmulationEngine {

    private static String LOG_TAG = "MOUSE_EMULATION";

    // service which started this engine
    private Service mService;

    // overlay view for drawing mouse
    private OverlayView mOverlayView;
    protected OverlayView getOverlayView() {return mOverlayView;}

    private MouseCursorView mCursorView;

    private PointerControl mPointerControl;

    public MouseEmulationEngine (Context c, OverlayView ov) {
        this.mOverlayView = ov;
        mCursorView = new MouseCursorView(c);
        ov.addFullScreenLayer(mCursorView);
        mPointerControl = new PointerControl(ov, mCursorView);
        Log.i(LOG_TAG, "X, Y: " + mPointerControl.getPointerLocation().x + ", " + mPointerControl.getPointerLocation().y);
    }

    public boolean init(@NonNull Service s) {
        this.mService = s;
        mPointerControl.reset();
        return true;
    }

    public boolean perform (KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.KEYCODE_DPAD_UP) {

        }
        return true;
    }
}
