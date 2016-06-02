/**
 * Created by Michael Turner (aka Deepscorn) on 04/07/15
 */
package com.gamelift.expandcollapseanimator;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

// currently I know about 2 ways to implement that view:
// 1. use negative bottom margin, let cards overlap and translate up. Only need to clip (Intersect)
// area below and not dispatch touch events
// 2. set height to collapsed and height of child to expanded. Clipping rect must be REPLACED so
// that we see child part, translated above. Here may be danger if we wount remove from clipping
// something that must be clipped, then really-really bad things will be drawn to the user
// (as seen when testing on android 4.2.2). It seems like here we can ignore things, which is
// clipped by parent, e.g. ActionBar
public class VerticalClipLayout extends FrameLayout {
    private float expandCoef = 0; // collapsed by default
    private float clipCoef = 1; // so, full clipping of animated part

    public VerticalClipLayout(Context context) {
        super(context);
        initialize();
    }

    public VerticalClipLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public VerticalClipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(21)
    public VerticalClipLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // do not dispatch touch events to view or it's children when clicking the clipped area
        return ev.getY() < getClippedHeight() && super.dispatchTouchEvent(ev);
    }

    // In xml layout_height of VerticalClipLayout is the collapsed height. Inside VerticalClipLayout
    // must be exactly one child view/layout. It's height will be the expanded height.
    // So, setting layout_height="match_parent" for child view is useless.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() == 1) {
            View child = getChildAt(0);

            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.getMode(heightMeasureSpec)));
            MarginLayoutParams margins = (MarginLayoutParams)getLayoutParams();
            margins.bottomMargin = MeasureSpec.getSize(heightMeasureSpec) - child.getMeasuredHeight();
        } else {
            throw new IllegalArgumentException("VerticalClipLayout should have exactly 1 child");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // clipping bottom part to force see video beneath
        canvas.clipRect(0, 0, getWidth(), getClippedHeight()); //Region.Op.REPLACE is no use because some applied clippings (for example from action bar) are not applied sometimes with REPLACE
        super.onDraw(canvas);
    }

    // set's current expand in 0..1 range
    // expandCoef is the coef to expand - y changes when expanding
    public void setExpandCoef(float expandCoef) {
        if(0 > expandCoef || 1 < expandCoef) {
            throw new IllegalArgumentException("expandCoef is out of range [0, 1], expandCoef: " + expandCoef);
        }
        this.expandCoef = expandCoef;

        setTranslationY(expandCoef * ((MarginLayoutParams) getLayoutParams()).bottomMargin);
    }

    // clipHeightCoef is in range 0..1, where 1 means clip all the animated area and show only the allways shown part at the top. 0 means show everything, you'll see fully expaded view
    public void setClipCoef(float clipHeightCoef) {
        if(0 > clipHeightCoef || 1 < clipHeightCoef) {
            throw new IllegalArgumentException("clipHeightCoef is out of range [0, 1], clipHeightCoef: " + clipHeightCoef);
        }
        clipCoef = clipHeightCoef;
        invalidate();
    }

    public float getExpandCoef() {
        return expandCoef;
    }

    // returns y, which view would have, if setExpandCoef(1) was called
    public float getYWhenExpanded() {
        return super.getTop() + ((MarginLayoutParams) getLayoutParams()).bottomMargin;
    }

    public int getExpandedHeight() {
        return getHeight();
    }

    public float getClippedHeight() {
        return getHeight() + clipCoef * ((MarginLayoutParams) getLayoutParams()).bottomMargin;
    }
}
