package io.github.virresh.matvt.view;

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

    private PointF mPointerLocation;
    private Paint mPaintBox;
    private Bitmap mPointerBitmap;
    private int mAlphaPointer= DEFAULT_ALPHA;


    public MouseCursorView(Context context) {
        super(context);
        setWillNotDraw(false);
        mPointerLocation = new PointF();
        mPaintBox = new Paint();

        BitmapDrawable bp = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.pointer);
        Bitmap originalBitmap = bp.getBitmap();
        mPointerBitmap = originalBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaintBox.setAlpha(mAlphaPointer);
        canvas.drawBitmap(mPointerBitmap, mPointerLocation.x, mPointerLocation.y, mPaintBox);
    }

    public void updatePosition(PointF p) {
        mPointerLocation.x = p.x;
        mPointerLocation.y = p.y;
        this.postInvalidate();
    }
}