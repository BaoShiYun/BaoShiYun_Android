/*
     The MIT License (MIT)
     Copyright (c) 2017 Jenly Yu
     https://github.com/jenly1314

     Permission is hereby granted, free of charge, to any person obtaining
     a copy of this software and associated documentation files
     (the "Software"), to deal in the Software without restriction, including
     without limitation the rights to use, copy, modify, merge, publish,
     distribute, sublicense, and/or sell copies of the Software, and to permit
     persons to whom the Software is furnished to do so, subject to the
     following conditions:

     The above copyright notice and this permission notice shall be included
     in all copies or substantial portions of the Software.

     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
     FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
     DEALINGS IN THE SOFTWARE.
 */
package com.baoshiyun.demo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baoshiyun.live.R;
import com.flyco.roundview.RoundTextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ????????????view
 *
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * @since 2017/4/10
 */

public class GoodsButton extends RelativeLayout {

    private View mGoodsBtn;
    private RoundTextView mGoodsNum;

    /**
     * ??????
     */
    private int[] heartRes = new int[]{
            R.mipmap.bsyl_ic_good1,
            R.mipmap.bsyl_ic_good2,
            R.mipmap.bsyl_ic_good3,
            R.mipmap.bsyl_ic_good4,
            R.mipmap.bsyl_ic_good5,
            R.mipmap.bsyl_ic_good6,
            R.mipmap.bsyl_ic_good7,
            R.mipmap.bsyl_ic_good8,
            R.mipmap.bsyl_ic_good9,
            R.mipmap.bsyl_ic_good10,
    };

    /**
     * ?????????
     */
    private Interpolator[] interpolators = new Interpolator[]{
            new LinearInterpolator(), new AccelerateInterpolator(),
            new DecelerateInterpolator(), new AccelerateDecelerateInterpolator(),
            new BounceInterpolator(), new OvershootInterpolator()
    };

    private int mWidth, mHeight;

    private Random mRandom;

    /**
     * ????????????????????????
     */
    private int mEnterDuration = 300;
    /**
     * ??????????????????
     */
    private int mDuration = 3000;
    /**
     * ?????????????????????
     */
    private float mScale = 1.0f;

    private LayoutParams mParams;

    /**
     * ??????????????????????????????????????????????????????
     */
    private boolean mIsSameSize = true;

    private PointF mStartPointF;
    private int mOffsetX; // x ?????????

    // ????????????????????????????????????
    private ValueAnimator mBatchShowAnimator;
    private int mLastBatchShowCount = 0;
    private List<BatchShowBean> mBatchShowDatas = new ArrayList<>();
    // ???????????????
    private int mTotalHearts;

    public GoodsButton(Context context) {
        this(context, null);
    }

    public GoodsButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoodsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRandom = new Random();

        mStartPointF = new PointF();

        mParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BSYFlutteringLayout);
        mEnterDuration = a.getInt(R.styleable.BSYFlutteringLayout_fl_enter_duration, mEnterDuration);
        mDuration = a.getInt(R.styleable.BSYFlutteringLayout_fl_duration, mDuration);
        mScale = a.getFloat(R.styleable.BSYFlutteringLayout_fl_scale, mScale);
        mIsSameSize = a.getBoolean(R.styleable.BSYFlutteringLayout_fl_same_size, mIsSameSize);

        mOffsetX = a.getDimensionPixelOffset(R.styleable.BSYFlutteringLayout_fl_offset_x, 0);
        a.recycle();

        mGoodsBtn = View.inflate(getContext(), R.layout.layout_goods_view, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        layoutParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        layoutParams.setMarginEnd(mOffsetX);

        addView(mGoodsBtn, layoutParams);
        mGoodsNum = findViewById(R.id.goods_num);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //??????????????????
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

    }

    /**
     * ??????????????????
     *
     * @param l
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        mGoodsBtn.setOnClickListener(l);
    }

    /**
     * ??????????????????????????? ???????????????????????????????????????
     *
     * @param totalHearts ??????
     */
    public void setTotalHearts(int totalHearts) {
        mTotalHearts = totalHearts;
        mGoodsNum.setText(getShowNumber(totalHearts));
    }

    /**
     * ??????????????????
     *
     * @param count        ??????
     * @param showDuration ????????? ????????????????????????
     */
    public void addHearts(int count, int showDuration) {
        if (count <= 0 || showDuration <= 0) {
            return;
        }
        if (count > showDuration * 6) {
            count = showDuration * 6;
        }
        synchronized (mBatchShowDatas) {
            // ????????????????????????,????????????????????????,????????????????????????
            if (mBatchShowAnimator != null && mBatchShowAnimator.isRunning()) {
                mBatchShowDatas.add(new BatchShowBean(count, showDuration));
                return;
            }
            mLastBatchShowCount = 0;
            mBatchShowAnimator = createBatchShowAnim(count, showDuration * 1000);
            mBatchShowAnimator.start();
        }
    }

    /**
     * ?????????????????????Anim
     *
     * @param count
     * @param showDuration
     * @return
     */
    private ValueAnimator createBatchShowAnim(int count, int showDuration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, count);
        animator.setInterpolator(randomInterpolator());
        animator.setDuration(showDuration);
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            int needShowCount = progress - mLastBatchShowCount;
            mLastBatchShowCount = progress;

            // ???????????????????????????
            if (needShowCount > 0) {
                for (int i = 0; i < needShowCount; i++) {
                    addHeart();
                }
            }
        });

        animator.addListener(new EndAnimatorListener(null) {
            @Override
            public void onAnimationEnd(Animator animation) {
                synchronized (mBatchShowDatas) {
                    if (mBatchShowDatas.size() > 0) {
                        BatchShowBean bean = mBatchShowDatas.remove(0);
                        addHearts(bean.count, bean.duration);
                    }
                }
            }
        });
        return animator;
    }

    /**
     * ????????????
     */
    public void addHeart() {
        mTotalHearts++;
        mGoodsNum.setText(getShowNumber(mTotalHearts));

        ImageView iv = getHeartView(randomHeartResource());
        addView(iv);
        updateStartPointF(iv);
        iv.setTranslationX(mStartPointF.x);

        Animator animator = getAnimator(iv);
        animator.addListener(new EndAnimatorListener(iv));
        animator.start();
    }

    /**
     * ??????????????????
     *
     * @param resId
     * @return
     */
    private ImageView getHeartView(int resId) {
        ImageView iv = new ImageView(getContext());
        iv.setLayoutParams(mParams);
        iv.setImageResource(resId);
        return iv;
    }

    /**
     * ??????????????????
     *
     * @param params
     */
    public void setLayoutParams(LayoutParams params) {
        this.mParams = params;
    }


    /**
     * ??????????????????
     *
     * @param target
     * @return
     */
    private AnimatorSet getEnterAnimator(View target) {

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.1f, mScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.1f, mScale);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.1f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.setDuration(mEnterDuration);

        return animatorSet;
    }


    /**
     * ?????????????????????
     *
     * @param target
     * @return
     */
    private ValueAnimator getBezierCurveAnimator(final View target) {

        //?????????????????????????????????
        final PointF pointf1 = randomPointF(3.0f);
        final PointF pointf2 = randomPointF(1.5f);

        // ??????  ?????? ??? ??????
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator<PointF>() {
            @Override
            public PointF evaluate(float fraction, PointF startValue, PointF endValue) {

                //???????????????????????? ?????? ???????????????????????????????????????????????????
                float timeLeft = 1.0f - fraction;
                PointF pointF = new PointF();
                pointF.x = (float) (startValue.x * Math.pow(timeLeft, 3)
                        + 3 * pointf1.x * fraction * Math.pow(timeLeft, 2)
                        + 3 * pointf2.x * Math.pow(fraction, 2) * timeLeft
                        + endValue.x * Math.pow(fraction, 3));

                pointF.y = (float) (startValue.y * Math.pow(timeLeft, 3)
                        + 3 * pointf1.y * fraction * Math.pow(timeLeft, 2)
                        + 3 * pointf2.y * Math.pow(fraction, 2) * timeLeft
                        + endValue.y * Math.pow(fraction, 3));


                return pointF;
            }
            //???????????????
        }, mStartPointF, new PointF(mRandom.nextInt(mWidth), 0));

        valueAnimator.setInterpolator(randomInterpolator());

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                //??????target?????????
                target.setX(pointF.x);
                target.setY(pointF.y);
                //????????? ???????????????????????????
                target.setAlpha(1.0f - animation.getAnimatedFraction() * animation.getAnimatedFraction());
            }
        });
        valueAnimator.setDuration(mDuration);

        return valueAnimator;
    }


    private Animator getAnimator(View target) {

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(getEnterAnimator(target), getBezierCurveAnimator(target));
        return animatorSet;

    }

    /**
     * ??????
     *
     * @param target
     */
    private void makeMeasureSpec(View target) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        target.measure(spec, spec);
    }

    /**
     * ??????
     *
     * @param target
     * @return
     */
    private void updateStartPointF(View target) {

        if (mStartPointF.x == 0 || mStartPointF.y == 0 || !mIsSameSize) {
            makeMeasureSpec(target);
            int width = target.getMeasuredWidth();
            int height = target.getMeasuredHeight();
            mStartPointF.x = mWidth - width - mOffsetX;
            int btnCenterX = (int) (mGoodsBtn.getX() + mGoodsBtn.getMeasuredWidth() / 2);
            mStartPointF.x = btnCenterX - target.getMeasuredWidth() / 2;
            mStartPointF.y = mHeight + getPaddingTop() - getPaddingBottom() - height;
        }
    }


    /**
     * ?????????????????????????????????
     *
     * @param scale
     * @return
     */
    private PointF randomPointF(float scale) {
        PointF pointF = new PointF();
        pointF.x = mRandom.nextInt(mWidth);
        pointF.y = mRandom.nextInt(mHeight) / scale;

        return pointF;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    private Interpolator randomInterpolator() {
        return interpolators[mRandom.nextInt(interpolators.length)];
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private int randomHeartResource() {
        return heartRes[mRandom.nextInt(heartRes.length)];
    }

    public int[] getHeartRes() {
        return heartRes;
    }

    public void setHeartRes(int[] heartRes) {
        this.heartRes = heartRes;
    }

    public Interpolator[] getInterpolators() {
        return interpolators;
    }

    public void setInterpolators(Interpolator[] interpolators) {
        this.interpolators = interpolators;
    }

    public int getEnterDuration() {
        return mEnterDuration;
    }

    public void setEnterDuration(int enterDuration) {
        this.mEnterDuration = enterDuration;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public LayoutParams getHeartLayoutParams() {
        return mParams;
    }

    public void setHeartLayoutParams(LayoutParams params) {
        this.mParams = params;
    }

    public boolean isSameSize() {
        return mIsSameSize;
    }

    public void setSameSize(boolean isSameSize) {
        this.mIsSameSize = isSameSize;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBatchShowDatas != null) mBatchShowDatas.clear();
        if (mBatchShowAnimator != null) mBatchShowAnimator.cancel();
    }

    private class EndAnimatorListener extends AnimatorListenerAdapter {

        private View target;

        public EndAnimatorListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //???????????? ??????target
            removeView(target);
        }
    }

    /**
     * ???????????????????????????
     */
    class BatchShowBean {
        int count;
        int duration;

        BatchShowBean(int count, int duration) {
            this.count = count;
            this.duration = duration;
        }
    }

    private String getShowNumber(int number) {
        if (number <= 0) {
            return "0";
        }
        if (number >= 10000) {
            double v = number * 1.0 / 10000f;
            DecimalFormat df = new DecimalFormat("#.0W");// ????????????????????????????????????
            df.setRoundingMode(RoundingMode.FLOOR);
            return df.format(v);
        } else {
            return number + "";
        }

    }
}
