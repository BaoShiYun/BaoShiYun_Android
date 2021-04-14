package com.baoshiyun.demo.chat.face;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by  on 2020/12/3.
 */
public class ScreenUtil {
    public static int getPxByDp(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    public static int dip2px(Context context, float dip) {
        if (context == null) {
            return (int) dip;
        }
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics()));
    }
}
