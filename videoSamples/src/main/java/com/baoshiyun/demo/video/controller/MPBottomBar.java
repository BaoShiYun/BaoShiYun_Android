package com.baoshiyun.demo.video.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baoshiyun.demo.utils.PlayTimeUtils;
import com.baoshiyun.video.R;

public class MPBottomBar extends LinearLayout implements View.OnClickListener {

    private ProgressBar mProgress;
    private TextView mTimeTv;

    private ImageView mPlayButton;
    private ImageView mNextButton;
    private ImageView mPrevButton;
    private CharSequence mPlayDescription;
    private CharSequence mPauseDescription;

    private View mFullScreenBtn;
    private View mSpeedPlayBtn;
    private TextView mDefinitionBtn;

    private MediaPlayerController.IMediaPlayerControl mPlayer;
    private MediaPlayerController.MediaExtraControl mExtraControl;
    private boolean mDragging;
    private BottomBarListener mBottomBarListener;
    private View mRootView;


    public MPBottomBar(Context context) {
        this(context, null);
    }

    public MPBottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MPBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        View.inflate(getContext(), R.layout.bsyv_p_media_controller_bottom_bar, this);

        // 防止事件透传
        findViewById(R.id.mc_bottom_bar_container).setOnClickListener(v -> {
        });
        mRootView = findViewById(R.id.mc_bottom_bar_root_view);
        mPlayDescription = "点击开始播放";
        mPauseDescription = "点击暂停播放";
        mPlayButton = findViewById(R.id.mc_bottom_bar_play);
        mNextButton = findViewById(R.id.mc_bottom_bar_next);
        mPrevButton = findViewById(R.id.mc_bottom_bar_prev);

        mProgress = findViewById(R.id.mc_bottom_bar_progress);
        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        mProgress.setMax(1000);


        mTimeTv = findViewById(R.id.mc_bottom_bar_time_current);

        mFullScreenBtn = findViewById(R.id.mc_bottom_bar_fullscreen);
        mSpeedPlayBtn = findViewById(R.id.mc_bottom_bar_speed_play);
        mDefinitionBtn = findViewById(R.id.mc_bottom_bar_definition);

        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mFullScreenBtn.setOnClickListener(this);
        mDefinitionBtn.setOnClickListener(this);
        mSpeedPlayBtn.setOnClickListener(this);
    }

    public void setMediaPlayer(MediaPlayerController.IMediaPlayerControl player,
                               MediaPlayerController.MediaExtraControl control) {
        this.mPlayer = player;
        this.mExtraControl = control;
    }

    /**
     * 处理一些不支持的控制按钮
     *
     * @param isPortrait
     */
    public void disableUnsupportedButtons(boolean isPortrait) {
        if (mPlayer == null && mExtraControl != null) {
            return;
        }
        try {
            mPlayButton.setEnabled(mPlayer.canPause());
            mProgress.setEnabled(mPlayer.canSeekBackward() && mPlayer.canSeekForward());
        } catch (IncompatibleClassChangeError ex) {
        }

        if (mExtraControl.canRotateScreen()) {
            mFullScreenBtn.setVisibility(View.VISIBLE);
        } else {
            mFullScreenBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 更新播放状态
     */
    public void updatePausePlay() {
        if (this.getVisibility() != View.VISIBLE) {
            return;
        }

        this.postDelayed(() -> {
            if (mPlayer.isPlaying()) {
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                mPlayButton.setContentDescription(mPauseDescription);
            } else {
                mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                mPlayButton.setContentDescription(mPlayDescription);
            }
        }, 100);
    }

    /**
     * 根据屏幕方向控制各个按钮是否显示
     *
     * @param orientation
     */
    public void updateControllerBtnShow(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSpeedPlayBtn.setVisibility(View.GONE);
            mDefinitionBtn.setVisibility(View.GONE);
        } else {
            mSpeedPlayBtn.setVisibility(View.VISIBLE);
            mDefinitionBtn.setVisibility(View.VISIBLE);
            if (mExtraControl != null) {
                String definitionName = mExtraControl.getDefinitionName();
                if (!TextUtils.isEmpty(definitionName)) {
                    mDefinitionBtn.setText(definitionName);
                } else {
                    mDefinitionBtn.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 更新进度
     *
     * @return
     */
    public int updateProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        int percent = mPlayer.getBufferPercentage();

        if (duration > 0) {
            // use long to avoid overflow
            long pos = 1000L * position / duration;
            mProgress.setProgress((int) pos);
        }
        mProgress.setSecondaryProgress(percent * 10);

        if (mTimeTv != null)
            mTimeTv.setText(PlayTimeUtils.formatMs(position) + " / " + PlayTimeUtils.formatMs(duration));

        return position;
    }

    public void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();

            removeCallbacks(mShowProgress);
            post(mShowProgress);
        }
        updatePausePlay();
    }

    @Override
    public void onClick(View v) {

        if (v.equals(mPlayButton)) {
            doPauseResume();
            if (mBottomBarListener != null) {
                mBottomBarListener.requestShow();
            }
        } else if (v.equals(mPrevButton)) {
        } else if (v.equals(mNextButton)) {
        } else if (v.equals(mFullScreenBtn)) {
            mExtraControl.fullScreen();
        } else if (v.equals(mSpeedPlayBtn)) {
            mExtraControl.showSpeedMenu(v);
        } else if (v.equals(mDefinitionBtn)) {
            mExtraControl.showDefinitionMenu(v);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            mPlayButton.requestFocus();
            removeCallbacks(mShowProgress);
            post(mShowProgress);
            updatePausePlay();
        } else {
            removeCallbacks(mShowProgress);
        }
    }

    public void doProgressGestureStart() {
        mDragging = true;
    }

    public void doProgressGesture(float curPosition) {
        long duration = mPlayer.getDuration();
        int progress = (int) ((1000L * curPosition / duration));
        mProgress.setProgress(progress);

        mTimeTv.setText(PlayTimeUtils.formatMs((int) curPosition) + " / "
                + PlayTimeUtils.formatMs((int) duration));
    }

    public void doProgressGestureEnd() {
        mDragging = false;
        post(mShowProgress);
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar bar) {
                    if (mBottomBarListener != null) {
                        mBottomBarListener.requestShow(3600000);
                    }

                    mDragging = true;

                    // By removing these pending progress messages we make sure
                    // that a) we won't update the progress while the user adjusts
                    // the seekbar and b) once the user is done dragging the thumb
                    // we will post one of these messages to the queue again and
                    // this ensures that there will be exactly one message queued up.
                    removeCallbacks(mShowProgress);
                }

                @Override
                public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                    if (!fromuser) {
                        // We're not interested in programmatically generated changes to
                        // the progress bar's position.
                        return;
                    }

                    long duration = mPlayer.getDuration();
                    long newposition = (duration * progress) / 1000L;
                    mTimeTv.setText(PlayTimeUtils.formatMs((int) newposition) + " / "
                            + PlayTimeUtils.formatMs((int) duration));
                }

                @Override
                public void onStopTrackingTouch(SeekBar bar) {
                    mDragging = false;

                    long duration = mPlayer.getDuration();
                    long newposition = (duration * bar.getProgress()) / 1000L;
                    mPlayer.seekToForUI((int) newposition);
                    mTimeTv.setText(PlayTimeUtils.formatMs((int) newposition) + " / "
                            + PlayTimeUtils.formatMs((int) duration));

                    updateProgress();
                    post(mShowProgress);
                    if (mBottomBarListener != null) {
                        mBottomBarListener.requestShow();
                    }
                }
            };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = updateProgress();
            if (!mDragging && getVisibility() == View.VISIBLE) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    public void setBottomBarListener(BottomBarListener l) {
        this.mBottomBarListener = l;
    }

    public void setNotchScreen(int notchHeight) {
        mRootView.setPadding(notchHeight, 0, notchHeight, 0);
    }

    public interface BottomBarListener {
        void requestShow(int timeout);

        void requestShow();
    }

}
