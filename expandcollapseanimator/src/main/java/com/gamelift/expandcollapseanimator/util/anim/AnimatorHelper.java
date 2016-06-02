/**
 * Created by Michael Turner (aka Deepscorn) on 04/07/15
 */
package com.gamelift.expandcollapseanimator.util.anim;

import android.animation.ValueAnimator;

public class AnimatorHelper {
    public static void cancelWithoutListenersNotified(ValueAnimator animator) {
        animator.removeAllUpdateListeners();
        animator.removeAllListeners();
        animator.cancel();
    }
}
