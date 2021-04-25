package com.baoshiyun.demo.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;

/**
 * Created by liujunting on 2019-04-28.
 * 屏幕旋转监听器
 */
public abstract class OrientationEventListener extends android.view.OrientationEventListener {
    int mLastOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    public OrientationEventListener(Context context) {
        super(context);
    }

    public OrientationEventListener(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;  // 手机平放时，检测不到有效的角度
        }
        // 只检测是否有四个角度的改变
        if (orientation > 315 || orientation <= 45) { // 0度-屏幕竖屏向上
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (orientation > 45 && orientation <= 135) { // 90度-屏幕横屏向右
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if (orientation > 135 && orientation <= 225) { // 180度-屏幕竖屏向下
            // 不做处理，使用上一次的值
        } else if (orientation > 225 && orientation <= 315) { // 270度-屏幕横屏向左
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return;
        }

        if (mLastOrientation != orientation) {
            mLastOrientation = orientation;
            onScreenOrientationChanged(orientation);
        }
    }

    /**
     * 屏幕方向发生变化，可以复写此方法设置屏幕方向,三种屏幕方向
     *
     * @param orientation {@link ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE},
     *                    {@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT},
     *                    {@link ActivityInfo#SCREEN_ORIENTATION_REVERSE_LANDSCAPE}
     */
    public abstract void onScreenOrientationChanged(int orientation);

}
