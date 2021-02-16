package io.github.virresh.matvt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import io.github.virresh.matvt.R;

/**
 * Draw a Mouse Cursor on screen
 */
public class MouseCursorView extends View {
    private static final int DEFAULT_ALPHA= 255;

    private final PointF mPointerLocation;
    private final Paint mPaintBox;
    private final Bitmap mPointerBitmap;

    public MouseCursorView(Context context) {
        super(context);
        setWillNotDraw(false);
        mPointerLocation = new PointF();
        mPaintBox = new Paint();

        @SuppressLint("UseCompatLoadingForDrawables")
        BitmapDrawable bp = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.pointer);
        Bitmap originalBitmap = bp.getBitmap();
        BitmapDrawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(originalBitmap, 50, 50, true));
        mPointerBitmap = d.getBitmap();
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