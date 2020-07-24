/**
 * Author: 李季东
 * Create Time: 2020/7/24
 * Update Time: 2020/7/24
 */
package com.edu.whu.xiaomaivideo;

import android.view.animation.Interpolator;

public class LookupTableInterpolator implements Interpolator {
    private final float[] mValues;
    private final float mStepSize;

    protected LookupTableInterpolator(float[] values) {
        this.mValues = values;
        this.mStepSize = 1.0F / (float)(this.mValues.length - 1);
    }

    public float getInterpolation(float input) {
        if (input >= 1.0F) {
            return 1.0F;
        } else if (input <= 0.0F) {
            return 0.0F;
        } else {
            int position = Math.min((int)(input * (float)(this.mValues.length - 1)), this.mValues.length - 2);
            float quantized = (float)position * this.mStepSize;
            float diff = input - quantized;
            float weight = diff / this.mStepSize;
            return this.mValues[position] + weight * (this.mValues[position + 1] - this.mValues[position]);
        }
    }
}
