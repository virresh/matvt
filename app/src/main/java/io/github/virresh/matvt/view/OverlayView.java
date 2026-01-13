package io.github.virresh.matvt.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class OverlayView extends RelativeLayout {
    private static String LOG_TAG = "MATVT Overlay";

    public OverlayView(Context context) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams();
        overlayParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        overlayParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        overlayParams.format = PixelFormat.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            overlayParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            overlayParams.alpha = (float) 0.8;
        } else {
            overlayParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            overlayParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        }

        wm.addView(this, overlayParams);
    }

    public void addFullScreenLayer (View v) {
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(this.getWidth(), this.getHeight());
        lp.width= RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height= RelativeLayout.LayoutParams.MATCH_PARENT;

        v.setLayoutParams(lp);
        v.setVisibility(INVISIBLE);
        this.addView(v);
        Log.i("Overlay View", "W - H : " + this.getWidth() + " " + this.getHeight());
    }
}