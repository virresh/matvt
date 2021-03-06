package io.github.virresh.matvt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;

import io.github.virresh.matvt.R;
import io.github.virresh.matvt.gui.IconStyleSpinnerAdapter;
import io.github.virresh.matvt.helper.Helper;

/**
 * Draw a Mouse Cursor on screen
 */
public class MouseCursorView extends View {
    private static final int DEFAULT_ALPHA= 255;

    private final PointF mPointerLocation;
    private final Paint mPaintBox;
    private final Bitmap mPointerBitmap;
    private int pointerDrawableReference;
    private int pointerSizeReference;

    public MouseCursorView(Context context) {
        super(context);
        setWillNotDraw(false);
        mPointerLocation = new PointF();
        mPaintBox = new Paint();

        updateFromPreferences();

        @SuppressLint("UseCompatLoadingForDrawables")
        BitmapDrawable bp = (BitmapDrawable) context.getDrawable(pointerDrawableReference);
        Bitmap originalBitmap = bp.getBitmap();
        BitmapDrawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(originalBitmap, 50 * pointerSizeReference, 50 * pointerSizeReference, true));
        mPointerBitmap = d.getBitmap();
    }

    public void updateFromPreferences() {
        Context ctx = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pointerDrawableReference = IconStyleSpinnerAdapter.textToResourceIdMap.getOrDefault(Helper.getMouseIconPref(ctx), R.drawable.pointer);
        }
        else {
            pointerDrawableReference = IconStyleSpinnerAdapter.textToResourceIdMap.get(Helper.getMouseIconPref(ctx));
        }
        pointerSizeReference = Helper.getMouseSizePref(ctx) + 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaintBox.setAlpha(DEFAULT_ALPHA);
        canvas.drawBitmap(mPointerBitmap, mPointerLocation.x, mPointerLocation.y, mPaintBox);
    }

    public void updatePosition(PointF p) {
        mPointerLocation.x = p.x;
        mPointerLocation.y = p.y;
        this.postInvalidate();
    }
}