package com.baoshiyun.demo.video.controller;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.annotation.FloatRange;

public abstract class MCGestureBaseListener extends GestureDetector.SimpleOnGestureListener
        implements ScaleGestureDetector.OnScaleGestureListener {

    private final Context mContext;
    private VelocityTracker mVelocityTracker;
    // 最小滑动距离
    private int mTouchSlop;

    // 是否缩放过
    private boolean mScaleChanged = false;

    public static final int NONE = -1;
    public static final int VOLUME = 1; // 音量模式
    public static final int BRIGHTNESS = 2; // 亮度模式
    public static final int FF_REW = 3; // 快进快退模式
    // 滚动模式
    private int mScrollMode = NONE;
    private int mCurVolume;
    private int mMaxVolume;
    private float mCurBrightness;

    // 防止触摸时事件太多，仅当此值不同时才回调
    private float mLastProgress = 0;
    private int mLastVolume = 0;
    private int mLastBrightness = 0;
    private boolean mIsLocked;

    public MCGestureBaseListener(Context context) {
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mScaleChanged = false;
        mScrollMode = NONE;

        mCurVolume = getCurVolume();
        mMaxVolume = getMaxVolume();

        mCurBrightness = getCurBrightness();

        mLastProgress = 0;
        mLastVolume = 0;
        mLastBrightness = 0;

        return super.onDown(e);
    }

    public void onUp(MotionEvent event) {
        switch (mScrollMode) {
            case NONE:
                break;
            case VOLUME:
                doVolumeGestureEnd(mLastVolume, mMaxVolume);
                break;
            case BRIGHTNESS:
                doBrightnessGestureEnd(mLastBrightness / 255F);
                break;
            case FF_REW:
                doProgressGestureEnd(mLastProgress);
                break;
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mIsLocked) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        // 缩放模式执行了，不在执行其他手势
//        if (mScaleChanged) {
//            return super.onScroll(e1, e2, distanceX, distanceY);
//        }
//        switch (mScrollMode) {
//            case NONE:
//                if (Math.abs(distanceX) > mTouchSlop) {
//                    int width = DensityUtil.getWidth(mContext);
//                    float rawX = e1.getRawX();
//                    if (rawX < width / 10 || rawX > width / 10 * 9) {
//                        break;
//                    }
//                    mScrollMode = FF_REW;
//                    mLastProgress = 0;
//                    mVelocityTracker.clear();
//                    mVelocityTracker.addMovement(e2);
//                    doProgressGestureStart();
//                } else if (Math.abs(distanceY) > mTouchSlop) {
//                    int height = DensityUtil.getHeight(mContext);
//                    float rawY = e1.getRawY();
//                    if (rawY < height / 10 || rawY > height / 10 * 9) {
//                        break;
//                    }
//                    if (e1.getX() < getControllerWidth() / 2) {
//                        mScrollMode = BRIGHTNESS;
//                        doBrightnessGestureStart();
//                    } else {
//                        mScrollMode = VOLUME;
//                        doVolumeGestureStart();
//                    }
//                }
//                break;
//            case VOLUME:
//                onVolumeGesture(e1, e2, distanceX, distanceY);
//                break;
//            case BRIGHTNESS:
//                onBrightnessGesture(e1, e2, distanceX, distanceY);
//                break;
//            case FF_REW:
//                mVelocityTracker.addMovement(e2);
//                mVelocityTracker.computeCurrentVelocity(1000);
//                onProgressGesture(e1, e2, distanceX, distanceY);
//                break;
//        }

        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mIsLocked) {
            return super.onDoubleTap(e);
        }
        doPauseResumeGesture();
        return super.onDoubleTap(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        doHideShowGesture();
        return super.onSingleTapConfirmed(e);
    }

    // 双指放大缩小视频画面
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mIsLocked) {
            return false;
        }
        float previousSpan = detector.getPreviousSpan();
        float currentSpan = detector.getCurrentSpan();
        float offset = currentSpan - previousSpan;

        if (offset > mTouchSlop) {
            if (!mScaleChanged) {
                mScaleChanged = true;
                doScaleGesture(true);
            }
        } else if (offset < -mTouchSlop) {
            if (!mScaleChanged) {
                mScaleChanged = true;
                doScaleGesture(false);
            }
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // Intentionally empty
    }


    private void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int value = getControllerHeight() / mMaxVolume;
        int newVolume = (int) ((e1.getY() - e2.getY()) / value + mCurVolume);
        if (newVolume < 0) {
            newVolume = 0;
        } else if (newVolume > mMaxVolume) {
            newVolume = mMaxVolume;
        }
        if (newVolume != mLastVolume) {
            mLastVolume = newVolume;
            doVolumeGesture(newVolume, mMaxVolume);
        }
    }

    private void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX,
                                     float distanceY) {
        float newBrightness = (e1.getY() - e2.getY()) / getControllerHeight();
        newBrightness += mCurBrightness;

        if (newBrightness < 0) {
            newBrightness = 0;
        } else if (newBrightness > 1) {
            newBrightness = 1;
        }

        int brightness = (int) (newBrightness * 255);
        if (brightness != mLastBrightness) {
            mLastBrightness = brightness;
            doBrightnessGesture(newBrightness);
        }
    }

    private void onProgressGesture(MotionEvent e1, MotionEvent e2, float distanceX,
                                   float distanceY) {
        float xVelocity = mVelocityTracker.getXVelocity();

        float progress = mLastProgress;
        // 手势速度慢的情况
        if (Math.abs(xVelocity) <= mTouchSlop * 10F) {
            progress += getProgressScale() * xVelocity / (mTouchSlop * 10F) * 0.5F;
        } else {
            progress -= distanceX / getControllerWidth() * 100;
        }

        doProgressGesture(progress, mLastProgress <= progress);

        mLastProgress = progress;
    }

    public void setLocked(boolean isLocked) {
        this.mIsLocked = isLocked;
    }

    /**
     * 手势进度的百分比因子
     *
     * @return
     */
    public float getProgressScale() {
        return 1.0F;
    }

    /**
     * 获取当前亮度
     *
     * @return
     */
    abstract float getCurBrightness();

    /**
     * 获取当前音量
     *
     * @return 当前音量
     */
    abstract int getCurVolume();

    /**
     * 最大音量
     *
     * @return 最大音量
     */
    abstract int getMaxVolume();

    /**
     * 获取controller宽度
     *
     * @return controller view 宽
     */
    abstract int getControllerWidth();

    /**
     * 获取controller高度
     *
     * @return controller view 高
     */
    abstract int getControllerHeight();

    /**
     * 显示隐藏手势
     */
    abstract void doHideShowGesture();

    /**
     * 开始暂停手势
     */
    abstract void doPauseResumeGesture();

    /**
     * 缩放手势
     *
     * @param zoomIn true 放大手势，false 缩小手势
     */
    abstract void doScaleGesture(boolean zoomIn);

    /**
     * 进度手势 开始
     */
    abstract void doProgressGestureStart();

    /**
     * 进度手势 根据移动的正负决定快进还是快退
     *
     * @param distanceProgress 移动的百分比，相对于当前控制器宽度计算的移动百分比 0-100
     * @param isFF             是否快进
     */
    abstract void doProgressGesture(float distanceProgress, boolean isFF);

    /**
     * 进度手势 根据移动的正负决定快进还是快退 结束
     *
     * @param distanceProgress 移动的百分比，相对于当前控制器宽度计算的移动百分比 0-100
     */
    abstract void doProgressGestureEnd(float distanceProgress);

    /**
     * 亮度手势开始
     */
    abstract void doBrightnessGestureStart();

    /**
     * 亮度手势
     *
     * @param newBrightness 亮度等级 0.0-1.0之间的浮点数
     */
    abstract void doBrightnessGesture(@FloatRange(from = 0.0f, to = 1.0f) float newBrightness);

    /**
     * 亮度手势结束
     *
     * @param newBrightness 亮度等级 0.0-1.0之间的浮点数
     */
    abstract void doBrightnessGestureEnd(@FloatRange(from = 0.0f, to = 1.0f) float newBrightness);

    /**
     * 音量手势开始
     */
    abstract void doVolumeGestureStart();

    /**
     * 音量手势
     *
     * @param newVolume 新的音量
     * @param maxVolume 最大音量
     */
    abstract void doVolumeGesture(int newVolume, int maxVolume);

    /**
     * 音量手势结束
     *
     * @param newVolume 新的音量
     * @param maxVolume 最大音量
     */
    abstract void doVolumeGestureEnd(int newVolume, int maxVolume);

}
