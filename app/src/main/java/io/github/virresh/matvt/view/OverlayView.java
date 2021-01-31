package io.github.virresh.matvt.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class OverlayView extends RelativeLayout {
    private static String LOG_TAG = "MATVT Overlay";

    public OverlayView(Context context) {
        super(context);

        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams();

        overlayParams.setTitle("Overlay For MATVT mouse cursor");

        overlayParams.format = PixelFormat.TRANSLUCENT;

        overlayParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY |
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        overlayParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        overlayParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        overlayParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(this, overlayParams);
    }

    void cleanup() {
        WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.removeViewImmediate(this);

        Log.i(LOG_TAG, "Overlay view has been removed");
    }

    public void addFullScreenLayer (View v) {
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(this.getWidth(), this.getHeight());
        lp.width= RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height= RelativeLayout.LayoutParams.MATCH_PARENT;

        v.setLayoutParams(lp);
        this.addView(v);
        Log.i("Overlay View", "W - H : " + this.getWidth() + " " + this.getHeight());
    }
}
