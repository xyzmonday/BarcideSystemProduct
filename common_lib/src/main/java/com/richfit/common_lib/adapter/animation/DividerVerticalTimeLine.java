package com.richfit.common_lib.adapter.animation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by monday on 2017/4/20.
 */

public class DividerVerticalTimeLine extends RecyclerView.ItemDecoration {

    private int dividerHeight;
    private int dividerWidth;
    private Paint mPaint;
    private Drawable middleOfDrawable;

    public DividerVerticalTimeLine(int color, int dividerHeight, int dividerWidth) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        this.dividerHeight = dividerHeight;
        this.dividerWidth = dividerWidth;
    }

    public DividerVerticalTimeLine(int color, int dividerHeight, int dividerWidth, Drawable middleOfDrawable) {
        this(color, dividerHeight, dividerWidth);
        this.middleOfDrawable = middleOfDrawable;
    }

    /**
     * 在每一个Item之间绘制一个Rect
     *
     * @param canvas
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize - 1; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            //计算下一个Decoration的顶
            int top = child.getBottom() + lp.bottomMargin;
            int bottom = top + dividerHeight;
            int left = 0;
            int right = 0;
            if (middleOfDrawable != null) {
                left = middleOfDrawable.getIntrinsicWidth() / 2 + child.getLeft() + child.getPaddingLeft()
                        + lp.leftMargin - dividerWidth / 2;
                right = middleOfDrawable.getIntrinsicWidth() / 2 + child.getLeft() + child.getPaddingLeft()
                        + lp.leftMargin + dividerWidth / 2;
            } else {
                left = child.getLeft() + child.getPaddingLeft() + lp.leftMargin - dividerWidth;
                right = child.getLeft() + child.getPaddingLeft() + lp.leftMargin + dividerWidth;
            }
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, dividerWidth, dividerHeight);
    }
}
