package com.tpb.hn.item.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.tpb.hn.Util;

import java.util.Locale;

/**
 * Created by theo on 23/11/16.
 */

public abstract class HolderSwipeCallback extends ItemTouchHelper.SimpleCallback {
    private final Paint mPaint;
    private String lt;
    private String rt;
    private int textSize;
    private int margin;

    public HolderSwipeCallback(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
        mPaint = new Paint();
        textSize = Util.pxFromDp(14);
        mPaint.setTextSize(textSize);
        margin = Util.pxFromDp(16);
    }

    public HolderSwipeCallback(int dragDirs, int swipeDirs, String left, String right) {
        super(dragDirs, swipeDirs);
        mPaint = new Paint();
        this.lt = left;
        this.rt = right;
        textSize = Util.pxFromSp(14);
        mPaint.setTextSize(textSize);
        margin = Util.pxFromDp(16);
    }

    public void setLeft(String left) {
        this.lt = left;
    }

    public void setRight(String right) {
        this.rt = right;
    }

    public void setText(String left, String right) {
        this.lt = left;
        this.rt = right;
    }

    public void setTextSize(int sp) {
        textSize = Util.pxFromSp(sp);
        mPaint.setTextSize(textSize);
    }

    public void setMargin(int dp) {
        margin = Util.pxFromDp(dp);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return 0.25f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        drawText(c, viewHolder, dX);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public abstract String getSwipeText(boolean right, int adapterPosition);

    private void drawText(Canvas canvas, RecyclerView.ViewHolder vh, float dX) {
        final boolean right = dX > 0;
        final Rect r = new Rect();
        final String text = getSwipeText(right, vh.getAdapterPosition());
        mPaint.getTextBounds(text, 0, text.length(), r);
        final float tw = r.right - r.left;
        final float th = r.top - r.bottom;
        final float width = vh.itemView.getWidth();
        final float py = (vh.itemView.getHeight() - th) / 2;
        mPaint.setAlpha(Math.min(255, (int) (255 / 0.25f * Math.abs(dX) / width)));
        canvas.drawText(text.toUpperCase(Locale.getDefault()),
                right ? vh.itemView.getLeft() + margin : vh.itemView.getRight() - margin - tw,
                vh.itemView.getBottom() - py,
                mPaint);
    }
}
