package com.gamelift.expandcollapseanimator.util;

/**
 * Created by Deepscorn on 04/07/15. For questions and licensing contact vnms11@gmail.com
 */
public class MathEx {
    // Compares a and b, allways taking epsilon into account. Returns:
    // -1 when a < b
    // 0 when a == b
    // 1 when a > b
    public static int compare(float a, float b, float epsilon) {
        float d = a - b;
        return d > epsilon ? 1 : (d < -epsilon ? -1 : 0);
    }
}
