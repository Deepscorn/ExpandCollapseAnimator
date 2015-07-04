package com.gamelift.expandcollapseanimator;

import android.animation.Animator;
import android.animation.ValueAnimator;

import com.gamelift.expandcollapseanimator.util.MathEx;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

// Expands and collapses list items with animation. Each item must be VerticalClipView.
// start(), pause() - control animation
// Call add() and setExpanding() from onPreDrawListener, because these methods rely on layout in recyclerview.
public class ExpandCollapseAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    //views in descending order: from the ones on the bottom to the ones on the top
    private SortedMap<Integer, VerticalClipLayout> positionToViewMap = new TreeMap<>(Collections.reverseOrder());
    private int expandingViewPosition = NO_POSITION;
    private static final int NO_POSITION = -1;
    private final float speed;
    private final float epsilon;
    private long lastTime; //in millis
    private OnViewExpandCollapseListener onViewExpandCollapseListener;
    private ValueAnimator animator;
    private boolean isViewsWereChanging = false;

    public ExpandCollapseAnimator(float speed) {
        this.speed = speed;
        epsilon = Math.min(speed * 0.1f, 0.0001f);
    }

    public void setOnViewExpandCollapseListener(OnViewExpandCollapseListener listener) {
        onViewExpandCollapseListener = listener;
    }

    //call in onResume()
    public void start() {
        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.INFINITE);
        animator.addUpdateListener(this);
        animator.addListener(this);
        animator.start();
    }

    //call in onPause()
    public void pause() {
        animator.removeAllUpdateListeners();
        animator.removeAllListeners();
        if(animator.isRunning()) {
            animator.cancel();
        }
        animator = null;
    }

    // adds view to processing
    public void add(int position, VerticalClipLayout view) {
        checkPosition(position);
        if(positionToViewMap.containsKey(position)) {
            throw new IllegalArgumentException("already added: " + position);
        }
        if(view == null) {
            throw new IllegalArgumentException("view is null at position " + position);
        }

        if(position <= expandingViewPosition || expandingViewPosition == NO_POSITION) {
            view.setExpandCoef(1);
        } else {
            view.setExpandCoef(0);
        }
        if(positionToViewMap.containsKey(position + 1)) {
            view.setClipCoef(1 - view.getExpandCoef() + positionToViewMap.get(position + 1).getExpandCoef());
        }
        if(positionToViewMap.containsKey(position - 1)) {
            VerticalClipLayout viewAbove = positionToViewMap.get(position - 1);
            viewAbove.setClipCoef(1 - viewAbove.getExpandCoef() +view.getExpandCoef());
        }
        positionToViewMap.put(position, view);
    }

    public void remove(int position) {
        checkPosition(position);
        if(!positionToViewMap.containsKey(position)) {
            throw new IllegalArgumentException("nothing to remove at position " + position);
        }
        positionToViewMap.remove(position);
        //remove must be allways off-screen, so there is no need to correct any clippings
    }

    // start expand animation for view. Note, that it is neccessary for view to be add()'ed before that call
    public void setExpanding(int position) {
        checkPosition(position);
        if(!positionToViewMap.containsKey(position)) {
            throw new IllegalArgumentException("first add view for that position: " + position);
        }

        if(positionToViewMap.containsKey(expandingViewPosition) && null != onViewExpandCollapseListener) {
            onViewExpandCollapseListener.onViewStartCollapsing(expandingViewPosition, positionToViewMap.get(expandingViewPosition));
        }
        expandingViewPosition = position;

        VerticalClipLayout expandingView = positionToViewMap.get(position);
        if(onViewExpandCollapseListener != null) {
            onViewExpandCollapseListener.onViewStartExpanding(position, expandingView);
        }

        if(expandingDone()) {
            // Correct clipping if already expanded. Otherwise there can be situation, when draw occurs
            // before clipping is corrected in onAnimationUpdate and blinking occurs.
            expandingView.setClipCoef(0);
            if(onViewExpandCollapseListener != null) {
                onViewExpandCollapseListener.onViewExpanded(position, expandingView);
            }
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        long temp = animation.getCurrentPlayTime();
        long dt = temp - lastTime;
        lastTime = temp;

        if(expandingViewPosition == NO_POSITION) {
            return;
        }
        final float d = speed * dt;
        boolean isViewsChanging = false;

        for(Map.Entry<Integer, VerticalClipLayout> positionToView: positionToViewMap.entrySet()) {
            VerticalClipLayout view = positionToView.getValue();
            if(positionToView.getKey() <= expandingViewPosition) {
                // expand
                final float left = 1 - view.getExpandCoef();
                if(MathEx.compare(left, 0, epsilon) == 1) {
                    isViewsChanging = true;
                    view.setExpandCoef(left < d ? 1 : view.getExpandCoef() + d);
                }
            } else {
                // collapse
                final float left = view.getExpandCoef();
                if (MathEx.compare(left, 0, epsilon) == 1) {
                    isViewsChanging = true;
                    view.setExpandCoef(left < d ? 0 : view.getExpandCoef() - d);
                }
            }
        }
        correctClippings();

        if(onViewExpandCollapseListener != null) {
            if(isViewsChanging) {
                onViewExpandCollapseListener.onViewsChanging();
            }

            //we don't know which event will be faster: expandingView expands or view below it collapse, so fire onViewExpanded when there is nothing else to animate
            if(!isViewsChanging && isViewsWereChanging && positionToViewMap.containsKey(expandingViewPosition) && expandingDone()) {
                onViewExpandCollapseListener.onViewExpanded(expandingViewPosition, positionToViewMap.get(expandingViewPosition));
            }
            isViewsWereChanging = isViewsChanging;
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        lastTime = 0;
    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        lastTime = 0;
    }

    public interface OnViewExpandCollapseListener {
        void onViewStartExpanding(int position, VerticalClipLayout v);
        void onViewExpanded(int position, VerticalClipLayout v);
        void onViewStartCollapsing(int position, VerticalClipLayout v);
        void onViewsChanging();
    }

    private void checkPosition(int position) {
        if(position < 0) {
            throw new IllegalArgumentException("position is negative: " + position);
        }
    }

    private void correctClippings() {
        float viewBelowExpandCoef = 0;
        for(Map.Entry<Integer, VerticalClipLayout> positionToView: positionToViewMap.entrySet()) {
            VerticalClipLayout view = positionToView.getValue();
            float clipCoef = 1 - view.getExpandCoef() + viewBelowExpandCoef;
            if(clipCoef > 1) {// because calculation is in float we may get 1.0000001 and there will be exception in setClipCoef()
                clipCoef = 1;
            }
            if(clipCoef < 0) {
                clipCoef = 0;
            }
            view.setClipCoef(clipCoef);
            viewBelowExpandCoef = view.getExpandCoef(); //each card above must be clipped with the ones below
        }
    }

    private boolean expandingDone() {
        return (MathEx.compare(positionToViewMap.get(expandingViewPosition).getExpandCoef(), 1, epsilon) == 0 &&
                (!positionToViewMap.containsKey(expandingViewPosition + 1) || MathEx.compare(positionToViewMap.get(expandingViewPosition + 1).getExpandCoef(), 0, epsilon) == 0));
    }
}
