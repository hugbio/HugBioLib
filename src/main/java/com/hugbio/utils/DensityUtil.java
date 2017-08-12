package com.hugbio.utils;

import android.content.Context;

/**
 * dp px 工具
 * 作者： huangbiao
 * 时间： 2017-08-12
 */
public class DensityUtil {
    public DensityUtil() {
        throw new AssertionError();
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (dpValue * scale + 0.5f);
    }
}
