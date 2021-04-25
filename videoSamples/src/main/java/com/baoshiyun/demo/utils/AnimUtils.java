package com.baoshiyun.demo.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.baoshiyun.video.R;


/**
 * Created by liujunting on 2019/4/10.
 * 动画帮助类工具
 */
public class AnimUtils {
    private static final long DEFAULT_DURATION = 400L;

    /**
     * 隐藏view 执行alpha动画
     *
     * @param v
     */
    public static void hideViewAlpha(final View v, long delay) {
        hideViewAlpha(v, 1.0f, 0f, delay, DEFAULT_DURATION);
    }

    /**
     * 隐藏view 执行alpha动画
     *
     * @param v
     */
    public static void hideViewAlpha(final View v) {
        hideViewAlpha(v, 1.0f, 0f, 0, DEFAULT_DURATION);
    }

    /**
     * 隐藏view 执行alpha动画
     *
     * @param v
     */
    public static void hideViewAlpha(final View v, float fromAlpha, float toAlpha) {
        hideViewAlpha(v, fromAlpha, toAlpha, 0, DEFAULT_DURATION);
    }

    /**
     * 隐藏view 执行alpha动画
     *
     * @param v
     */
    public static void hideViewAlpha(final View v, float fromAlpha, float toAlpha, long delay,
                                     long duration) {
        Object tag = v.getTag(R.id.value_anim_tag);
        if (tag != null && tag instanceof ValueAnimator) {
            ValueAnimator oldAnim = (ValueAnimator) tag;
            oldAnim.cancel();
        }

        PropertyValuesHolder p = PropertyValuesHolder.ofFloat(View.ALPHA, fromAlpha, toAlpha);
        ValueAnimator valueAnimator =
                ObjectAnimator.ofPropertyValuesHolder(v, p).setDuration(duration);
        valueAnimator.setStartDelay(delay);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.GONE);
            }
        });
        v.setTag(R.id.value_anim_tag, valueAnimator);
        valueAnimator.start();
    }

    /**
     * 显示view 执行alpha动画
     *
     * @param v
     */
    public static void showViewAlpha(final View v) {
        showViewAlpha(v, 0f, 1.0f, DEFAULT_DURATION);
    }

    /**
     * 显示view 执行alpha动画
     *
     * @param v
     */
    public static void showViewAlpha(final View v, float fromAlpha, float toAlpha, long duration) {

        Object tag = v.getTag(R.id.value_anim_tag);
        if (tag != null && tag instanceof ValueAnimator) {
            ValueAnimator oldAnim = (ValueAnimator) tag;
            oldAnim.cancel();
        }

        v.setVisibility(View.VISIBLE);
        PropertyValuesHolder p = PropertyValuesHolder.ofFloat(View.ALPHA, fromAlpha, toAlpha);
        ValueAnimator valueAnimator =
                ObjectAnimator.ofPropertyValuesHolder(v, p).setDuration(duration);
        v.setTag(R.id.value_anim_tag, valueAnimator);
        valueAnimator.start();
    }

    public static void hideView2Right(View view, long delay) {
        view.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                android.R.anim.slide_out_right);
        animation.setStartOffset(delay);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(animation);
    }

    public static void showViewInRight(View view, long delay, Animation.AnimationListener l) {
        view.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.bsyv_slide_in_right);
        animation.setStartOffset(delay);
        animation.setAnimationListener(l);
        view.startAnimation(animation);
    }
}
