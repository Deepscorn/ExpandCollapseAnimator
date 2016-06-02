/**
 * Created by Michael Turner (aka Deepscorn) on 04/07/15
 */
package com.gamelift.expandcollapseanimator.util.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

// for old API; starting from API 14 we can use android.animation.TimeAnimator instead
public class TimeAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final int timeStart = 0;
    private static final int timeEnd = 1000;
    private int lastTime;
    private ValueAnimator animator;
    private TimeListener timeListener;

    public void start(TimeListener timeListener) {
        this.timeListener = timeListener;

        animator = ValueAnimator.ofInt(timeStart, timeEnd);
        animator.setDuration(timeEnd);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(this);
        animator.addListener(this);
        animator.start();
    }

    public void stop() {
        timeListener = null;
        AnimatorHelper.cancelWithoutListenersNotified(animator);
        animator = null;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int dt = processAndGetDeltaTime(animation);
        if (dt > 0 && timeListener != null) {
            timeListener.onTimeUpdate(this, dt);
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        lastTime = timeStart;
    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        lastTime = timeStart;
    }

    private int processAndGetDeltaTime(ValueAnimator animation) {
        int current = (Integer) animation.getAnimatedValue();
        int delta = current - lastTime;
        lastTime = current;
        return delta;
    }

    public interface TimeListener {
        void onTimeUpdate(TimeAnimator animation, int deltaTime);
    }
}
