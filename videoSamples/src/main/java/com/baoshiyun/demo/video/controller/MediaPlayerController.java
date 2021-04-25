package com.baoshiyun.demo.video.controller;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoshiyun.demo.utils.AnimUtils;
import com.baoshiyun.demo.utils.PlayTimeUtils;
import com.baoshiyun.video.R;

public class MediaPlayerController extends FrameLayout implements View.OnClickListener {

    private MyControllerGestureListener mGestureListener;
    private Window mWindow;

    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mMinVolume;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    private IMediaPlayerControl mPlayer;
    private MediaExtraControl mExtraControl;
    private Context mContext;

    private boolean mShowing;
    private static final int sDefaultTimeout = 5000;

    // 控制器容器
    private MPBottomBar mBottomBar;
    private MPTopBar mTopBar;

    private View mGestureSeekBarContainer;
    private ProgressBar mGestureSeekBar;
    private View mLockBtn;
    private View mMiddleBar;
    // 仅显示顶部bar
    private boolean mOnlyShowTopBar = false;
    private ImageView mGestureTypeIv;
    private TextView mGestureProgressTimeTv;


    public MediaPlayerController(Context context) {
        this(context, null);
    }

    public MediaPlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();

        mGestureListener = new MyControllerGestureListener(context);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mScaleGestureDetector = new ScaleGestureDetector(context, mGestureListener);

        mAudioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mMinVolume = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
        } else {
            mMinVolume = 0;
        }

        if (context instanceof Activity) {
            mWindow = ((Activity) context).getWindow();
        }
    }

    private void initView() {
        LayoutInflater inflate =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflate.inflate(R.layout.bsyv_p_media_controller, this);
        initControllerView();
    }

    private void initControllerView() {
        mGestureSeekBarContainer = findViewById(R.id.mc_gesture_progress_bar_container);
        mGestureSeekBar = findViewById(R.id.mc_gesture_progress_bar);
        mGestureTypeIv = findViewById(R.id.mc_gesture_type_icon);
        mGestureProgressTimeTv = findViewById(R.id.mc_gesture_progress_time);

        mBottomBar = findViewById(R.id.mc_bottom_bar);
        mBottomBar.setBottomBarListener(new MPBottomBar.BottomBarListener() {
            @Override
            public void requestShow(int timeout) {
                show(timeout);
            }

            @Override
            public void requestShow() {
                show();
            }
        });

        mTopBar = findViewById(R.id.mc_top_bar);


        mMiddleBar = findViewById(R.id.mc_middle_bar);

        mLockBtn = findViewById(R.id.mc_lock);
        mLockBtn.setSelected(false);
        mLockBtn.setOnClickListener(this);

    }

    public void setMediaPlayer(IMediaPlayerControl player, MediaExtraControl extraControl) {
        mPlayer = player;
        mExtraControl = extraControl;
        mBottomBar.setMediaPlayer(player, extraControl);
        mTopBar.setMediaPlayer(extraControl);

        updatePausePlay();
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        boolean isPortrait = isPortrait();
        mBottomBar.disableUnsupportedButtons(isPortrait);
        mTopBar.disableUnsupportedButtons(isPortrait);
    }

    /**
     * 是否是竖屏
     *
     * @return
     */
    private boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing) {
            mShowing = true;

            if (!isLocked()) {
                if (!mOnlyShowTopBar) {
                    AnimUtils.showViewAlpha(mBottomBar);
                }
                AnimUtils.showViewAlpha(mTopBar);
            }
            if (!isPortrait()) {
                AnimUtils.showViewAlpha(mMiddleBar);
            }
        }

        if (!isLocked()) {
            setProgress();
            int orientation = getResources().getConfiguration().orientation;
            disableUnsupportedButtons();
            updateControllerBtnShow(orientation);
        }

        updatePausePlay();

        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public boolean isLocked() {
        return mLockBtn.isSelected();
    }

    public void lock() {
        mLockBtn.setSelected(true);
        hideTopAndBottomBar();
        // 锁闭后再显示锁闭按钮延迟消失
        show();
        mGestureListener.setLocked(true);
    }

    public void unlock() {
        mLockBtn.setSelected(false);
        mShowing = false;
        // 强制显示top bar 和 bottom bar
        show();
        mGestureListener.setLocked(false);
    }

    public void hide() {
        if (mShowing) {
            removeCallbacks(mFadeOut);
            hideTopAndBottomBar();
            AnimUtils.hideViewAlpha(mMiddleBar);
            mShowing = false;
        }
        updatePausePlay();
    }

    private void hideTopAndBottomBar() {
        AnimUtils.hideViewAlpha(mBottomBar);
        AnimUtils.hideViewAlpha(mTopBar);
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    private int setProgress() {
        return mBottomBar.updateProgress();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可用或者控制器锁定都不进行操作
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }
        if (event.getPointerCount() >= 2) {
            mScaleGestureDetector.onTouchEvent(event);
        } else {
            mGestureDetector.onTouchEvent(event);
        }

        // 通知手指抬起事件
        if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            mGestureListener.onUp(event);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show();
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show();
        return super.dispatchKeyEvent(event);
    }

    private void updatePausePlay() {
        mBottomBar.updatePausePlay();
    }

    public void doPauseResume() {
        mBottomBar.doPauseResume();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return IMediaPlayerControl.class.getName();
    }

    /**
     * 显示手势进度条
     */
    private void showGestureProgressBar() {
        AnimUtils.showViewAlpha(mGestureSeekBarContainer);
    }

    private void hideSeekBar() {
        AnimUtils.hideViewAlpha(mGestureSeekBarContainer);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateControllerBtnShow(newConfig.orientation);
    }

    /**
     * 更新各个控制按钮根据屏幕的方向去适配
     *
     * @param orientation
     */
    private void updateControllerBtnShow(int orientation) {
        mBottomBar.updateControllerBtnShow(orientation);
        mTopBar.updateControllerBtnShow(orientation);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mMiddleBar.setVisibility(View.GONE);

            // 通过其他途径恢复到了竖屏状态
//            mLockBtn.setSelected(false);
//            mGestureListener.setLocked(false);
        } else {
            if (mShowing) {
                AnimUtils.showViewAlpha(mMiddleBar);
            }
        }
    }

    public void prepared() {
        if (mExtraControl != null) {
            show(1000 * 60 * 60 * 24);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mLockBtn)) {
            if (mLockBtn.isSelected()) {
                unlock();
            } else {
                lock();
            }
        }
    }

    public void setOnlyShowTopBar(boolean onlyShowTopBar) {
        this.mOnlyShowTopBar = onlyShowTopBar;
    }

    /**
     * 设置为刘海屏
     *
     * @param notchHeight 刘海高度
     */
    public void setNotchScreen(int notchHeight) {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mMiddleBar.getLayoutParams();
        layoutParams.rightMargin = notchHeight + layoutParams.rightMargin;
        mMiddleBar.setLayoutParams(layoutParams);

        mTopBar.setNotchScreen(notchHeight);
        mBottomBar.setNotchScreen(notchHeight);
    }

    class MyControllerGestureListener extends MCGestureBaseListener {

        private int lastPosition = -1;
        // 进度条手势慢速滑动因子
        private float mProgressScale = 1F;

        public MyControllerGestureListener(Context context) {
            super(context);
        }

        @Override
        float getCurBrightness() {
            if (mWindow != null) {
                float brightness = mWindow.getAttributes().screenBrightness;
                // 默认值使用系统当前亮度
                if (brightness == -1) {
                    int systemBrightness = Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, 255);
                    brightness = systemBrightness / 255F;
                }
                return brightness;
            } else {
                return 0;
            }
        }

        @Override
        int getCurVolume() {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        int getMaxVolume() {
            return mMaxVolume;
        }

        @Override
        int getControllerWidth() {
            return getWidth();
        }

        @Override
        int getControllerHeight() {
            return getHeight();
        }

        @Override
        void doHideShowGesture() {
            if (mShowing) {
                hide();
            } else {
                show();
            }
        }

        @Override
        void doPauseResumeGesture() {
            doPauseResume();
        }

        @Override
        void doScaleGesture(boolean zoomIn) {
            mPlayer.onScale(zoomIn);
        }

        @Override
        void doProgressGestureStart() {
            mProgressScale = 1F / (mPlayer.getDuration() / 1000F / 100F / 5F);
            if (mProgressScale > 1F) {
                mProgressScale = 1F;
            }

            lastPosition = -1;
            mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_ff);
            mGestureSeekBar.setVisibility(View.GONE);
            mGestureProgressTimeTv.setVisibility(View.VISIBLE);
            showGestureProgressBar();

            mBottomBar.doProgressGestureStart();
        }

        @Override
        public float getProgressScale() {
            return mProgressScale;
        }

        @Override
        void doProgressGesture(float distanceProgress, boolean isFF) {
            int duration = mPlayer.getDuration();
            // 一次的滑动范围在总时长的5分之1
            float gesturePosition =
                    mPlayer.getCurrentPosition() + (duration / 5F / 100 * distanceProgress);

            if (gesturePosition < 0) {
                gesturePosition = 0;
            } else if (gesturePosition > duration) {
                gesturePosition = duration;
            }

            if (lastPosition == gesturePosition) {
                return;
            }
            if (lastPosition == -1) {
                lastPosition = (int) gesturePosition;
            }

            if (gesturePosition - lastPosition > 1000) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_ff);
            } else if (gesturePosition - lastPosition < -1000) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_rew);
            }

            String text = getResources().getString(R.string.bsyv_p_gesture_progress_time,
                    PlayTimeUtils.formatMs((int) gesturePosition),
                    PlayTimeUtils.formatMs(duration));
            mGestureProgressTimeTv.setText(Html.fromHtml(text));

            mBottomBar.doProgressGesture(gesturePosition);

            lastPosition = (int) gesturePosition;
        }

        @Override
        void doProgressGestureEnd(float distanceProgress) {
            int duration = mPlayer.getDuration();
            // 一次的滑动范围在总时长的5分之1
            float gesturePosition =
                    mPlayer.getCurrentPosition() + (duration / 5F / 100 * distanceProgress);
            mPlayer.seekToForUI((int) gesturePosition);
            hideSeekBar();

            mBottomBar.doProgressGestureEnd();
        }

        @Override
        void doBrightnessGestureStart() {
            float curBrightness = getCurBrightness();
            // 显示进度条
            mGestureSeekBar.setVisibility(View.VISIBLE);
            mGestureProgressTimeTv.setVisibility(View.GONE);
            if (curBrightness >= 0.5f) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_brightnes_high);
            } else {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_brightnes_low);
            }

            showGestureProgressBar();

            mGestureSeekBar.setMax(100);
            mGestureSeekBar.setProgress((int) (curBrightness * 100));
        }

        @Override
        void doBrightnessGesture(float newBrightness) {
            if (mWindow != null) {
                WindowManager.LayoutParams attributes = mWindow.getAttributes();
                attributes.screenBrightness = newBrightness;
                mWindow.setAttributes(attributes);
            }
            if (newBrightness >= 0.5f) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_brightnes_high);
            } else {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_brightnes_low);
            }

            mGestureSeekBar.setProgress((int) (newBrightness * 100));
        }

        @Override
        void doBrightnessGestureEnd(float newBrightness) {
            hideSeekBar();
        }

        @Override
        void doVolumeGestureStart() {
            // 显示进度条
            mGestureSeekBar.setVisibility(View.VISIBLE);
            mGestureProgressTimeTv.setVisibility(View.GONE);
            showGestureProgressBar();
            int curVolume = getCurVolume();
            if (curVolume > 0) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_volume);
            } else {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_silence);
            }
            mGestureSeekBar.setMax(getMaxVolume());
            mGestureSeekBar.setProgress(getCurVolume());
        }

        @Override
        void doVolumeGesture(int newVolume, int maxVolume) {
            if (newVolume > 0) {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_volume);
            } else {
                mGestureTypeIv.setImageResource(R.mipmap.bsyv_p_ic_silence);
            }

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume,
                    AudioManager.FLAG_PLAY_SOUND);
            mGestureSeekBar.setProgress(newVolume);
        }

        @Override
        void doVolumeGestureEnd(int newVolume, int maxVolume) {
            hideSeekBar();
        }
    }

    public interface IMediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        void seekToForUI(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        int getAudioSessionId();

        /**
         * 放大缩小
         *
         * @param zoomIn true 放大 false 缩小
         */
        void onScale(boolean zoomIn);
    }

    /**
     * 额外的扩展控制
     */
    public interface MediaExtraControl {

        void fullScreen();

        void showDefinitionMenu(View v);

        void showSpeedMenu(View v);

        void onBackClick();

        boolean canRotateScreen();

        String getVideoTitle();

        String getDefinitionName();

        boolean isLoadVideo();
    }
}
