package com.baoshiyun.demo.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.baoshiyun.demo.video.controller.MediaPlayerController;
import com.baoshiyun.demo.video.controller.PlayerStatusLayout;
import com.baoshiyun.demo.video.menu.PlayerDefinitionPopupWindow;
import com.baoshiyun.demo.video.menu.PlayerRightPopupWindow;
import com.baoshiyun.demo.video.menu.PlayerSpeedPopupWindow;
import com.baoshiyun.demo.utils.AnimUtils;
import com.baoshiyun.video.R;
import com.baoshiyun.warrior.core.NetWatchDog;
import com.baoshiyun.warrior.video.Definition;
import com.baoshiyun.warrior.video.player.IMediaPlayer;

import java.util.List;

/**
 * 抱石云视频播放view
 * 集成播放的所有功能，只需 Activity 嵌入即可
 * Created by ljt on 2020/11/19.
 */
public class BSYVideoPlayView extends FrameLayout {
    private MediaPlayerController mController;
    private View mBufferingView;
    private BSYVideoView mVideoView;
    // 是否支持旋转
    private boolean mSupportOrientation = true;
    // 倍速
    private String mCurrentSpeed = "1.0";
    // 侧边菜单
    private PlayerRightPopupWindow mCurShowPopW;
    // resume 时候续播标识
    private boolean mResumeWhenPlaying;
    private int mResumeWhenPosition;
    // 切换清晰度提示
    private TextView mToastTipsTv;
    // 状态提示view
    private PlayerStatusLayout mStatusLayout;
    // 4G网络提醒
    private boolean mNeedTipsMobileNetwork = true;
    // 网络观察
    private NetWatchDog mNetWatchdog;
    // 是否在展现
    private boolean mIsResumed;
    // 隐藏提示runnable
    private Runnable hideTipsRunnable = () -> AnimUtils.hideViewAlpha(mToastTipsTv);

    private String mMediaId;
    private String mLocalFilePath;
    private String mTitle;
    private boolean mIsLocal;

    public BSYVideoPlayView(@NonNull Context context) {
        this(context, null);
    }

    public BSYVideoPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BSYVideoPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initPlayerListener();
        // 网络观察
        initNetWatchdog();
    }

    /**
     * 设置播放源
     *
     * @param title
     * @param mediaId
     */
    public void setPlaySource(String title, String mediaId,boolean isLocal,String localFilePath) {
        this.mMediaId = mediaId;
        this.mTitle = title;
        this.mIsLocal = isLocal;
        this.mLocalFilePath = localFilePath;
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        // 互相绑定
        mController.setMediaPlayer(mVideoView, new MyMediaExtraControl());
        mVideoView.setMediaController(mController);
        playVideo();
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        dismissPopupWindow();

        // 重置一些属性
        mCurrentSpeed = "1.0";

        mStatusLayout.showPrepareLoading();

        if (mIsLocal) {
            mVideoView.setOfflineData(mMediaId, mLocalFilePath);
        } else {
            mVideoView.setOnlineData(mMediaId, null);
        }
        // 播放当前进度
        mVideoView.start();
    }

    /**
     * 视频播放 view 可以实现一些定制化的操作,比如开始,暂停,seek,默认清晰度等
     *
     * @return 视频播放 view
     */
    public BSYVideoView getVideoView() {
        return mVideoView;
    }

    /**
     * 设置是否支持旋转
     *
     * @param supportOrientation
     */
    public void setSupportOrientation(boolean supportOrientation) {
        mSupportOrientation = supportOrientation;
    }

    /**
     * 网络观察
     */
    private void initNetWatchdog() {
        mNetWatchdog = new NetWatchDog(getContext());
        mNetWatchdog.setNetChangeListener(new NetWatchDog.NetChangeListener() {
            @Override
            public void onWifiConnected() {
                // 如果已经展现并且之前显示的是4G提示，转到WIFI则开始播放
                if (mIsResumed && mStatusLayout.getCurStatus() == PlayerStatusLayout.STATUS_SHOW_4G) {
                    mStatusLayout.hide();
                    mVideoView.start();
                }
            }

            @Override
            public void onMobileConnected() {
                // 不是离线视频 & 需要非 Wifi 网络提示 & 当前没有已经存在的提示，则提示非当前为 Wifi 网络播放视频
                if (!mIsLocal
                        && mNeedTipsMobileNetwork
                        && mStatusLayout.getCurStatus() != PlayerStatusLayout.STATUS_SHOW_4G) {
                    mBufferingView.setVisibility(View.GONE);
                    mVideoView.pause();
                    mStatusLayout.show4GTips();
                }
            }

            @Override
            public void onNetDisconnected() {
            }
        });
        mNetWatchdog.startWatch();
    }

    /**
     * 初始化View
     */
    private void initView() {
        View.inflate(getContext(), R.layout.bsyv_p_video_play_view, this);

        mVideoView = findViewById(R.id.player_video_view);
        mController = findViewById(R.id.player_media_controller);
        mController.setEnabled(false);

        mBufferingView = findViewById(R.id.player_buffering_container);

        // 切换清晰度，片段，倍速，播放进度等提示
        mToastTipsTv = findViewById(R.id.player_toast_tips);

        // 状态提示layout
        mStatusLayout = findViewById(R.id.player_status_layout);
    }

    /**
     * 显示提示 View
     */
    private void showToastTips(CharSequence s) {
        mToastTipsTv.setText(s);
        mToastTipsTv.removeCallbacks(hideTipsRunnable);
        AnimUtils.showViewAlpha(mToastTipsTv);

        mToastTipsTv.postDelayed(hideTipsRunnable, 4000);
    }

    /**
     * 初始化监听器
     */
    private void initPlayerListener() {
        mStatusLayout.setonPlayerStatusEventListener(
                new PlayerStatusLayout.OnPlayerStatusEventListener() {
                    @Override
                    public void onBack() {
                        Context context = getContext();
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            activity.finish();
                        }
                    }

                    @Override
                    public void onMobileNetResume() {
                        mStatusLayout.hide();
                        mNeedTipsMobileNetwork = false;
                        mVideoView.start();
                    }

                    @Override
                    public void onReload() {
                        mStatusLayout.hide();
                        playVideo();
                    }
                });

        mVideoView.setOnErrorListener((mp, e) -> {
            mBufferingView.setVisibility(View.GONE);
            mController.hide();
            mController.setEnabled(false);
            mStatusLayout.showError(getString(R.string.bsyv_p_error_tips), false);
            return true;
        });

        mVideoView.setOnCompletionListener((IMediaPlayer mp) -> {
            mController.unlock();
            mStatusLayout.showComplete();
        });

        mVideoView.setOnInfoListener((IMediaPlayer mp, int what, int extra) -> {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (!mIsLocal) {
                        mBufferingView.setVisibility(View.VISIBLE);
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mBufferingView.setVisibility(View.GONE);
                    break;
            }
            return false;
        });

        mVideoView.setOnPreparedListener((mp) -> {
            mBufferingView.setVisibility(View.GONE);
            // 如果在loading状态隐藏掉
            if (mStatusLayout.getCurStatus() == PlayerStatusLayout.STATUS_SHOW_PREPARE) {
                mStatusLayout.hide();
            }

            // 准备完成后重新设置播放倍速
            if (!mCurrentSpeed.equals("1.0")) {
                mVideoView.setSpeed(Float.valueOf(mCurrentSpeed));
            }

            // 准备完成后需要重置播放控制器状态
            mController.prepared();
        });
    }

    public void onPageResume() {
        mIsResumed = true;
        if (mMediaId == null) {
            return;
        }
        if (mResumeWhenPlaying) {
            mVideoView.start();
            mVideoView.seekTo(mResumeWhenPosition);
            mResumeWhenPlaying = false;
            mResumeWhenPosition = 0;
        }
    }

    public void onPagePause() {
        mIsResumed = false;
        if (mMediaId == null) {
            return;
        }
        // 音频播放不控制后台暂停
        mResumeWhenPlaying = mVideoView.isPlaying();
        mResumeWhenPosition = mVideoView.getCurrentPosition();

        mVideoView.pause();
    }

    public void onPageStop() {
    }

    public void onDestroy() {
        if (mNetWatchdog != null) {
            mNetWatchdog.stopWatch();
        }

        if (mToastTipsTv != null) {
            mToastTipsTv.removeCallbacks(hideTipsRunnable);
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    /**
     * 当前视频播放时长 毫秒
     */
    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    /**
     * 当前视频总时长 毫秒
     */
    public int getDuration() {
        return mVideoView.getDuration();
    }

    /**
     * 切换清晰度
     *
     * @param definition 清晰度
     */
    private void changeDefinition(Definition definition) {
        showToastTips(Html.fromHtml(getResources().getString(R.string.bsyv_p_definition_tips, definition.getDefinitionName())));
        mBufferingView.setVisibility(View.VISIBLE);
        mVideoView.setDefinition(definition);
    }

    /**
     * 显示倍速view
     */
    private void showSpeedSelect() {
        mController.hide();
        dismissPopupWindow();
        PlayerSpeedPopupWindow pop = new PlayerSpeedPopupWindow(getContext());
        pop.show(mVideoView, mCurrentSpeed, (speed) -> {
            mCurrentSpeed = speed;

            showToastTips(Html.fromHtml(getResources().getString(R.string.bsyv_p_speed_tips, mCurrentSpeed)));
            dismissPopupWindow();
            mVideoView.setSpeed(Float.valueOf(speed));

        });

        mCurShowPopW = pop;
    }

    /**
     * 显示清晰度选择view
     */
    private void showDefinitionSelect() {

        List<Definition> definitions = mVideoView.getDefinitions();
        Definition curDefinition = mVideoView.getCurDefinition();
        if (definitions == null || definitions.isEmpty()) {
            Toast.makeText(getContext().getApplicationContext(),
                    R.string.bsyv_p_not_definitions,
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mController.hide();
        dismissPopupWindow();
        PlayerDefinitionPopupWindow pop = new PlayerDefinitionPopupWindow(getContext());
        pop.show(mVideoView, definitions, curDefinition, (definition) -> {
            dismissPopupWindow();
            changeDefinition(definition);
        });
        mCurShowPopW = pop;
    }


    private void dismissPopupWindow() {
        if (mCurShowPopW != null) {
            mCurShowPopW.dismiss();
            mCurShowPopW = null;
        }
    }

    /**
     * 是否是竖屏
     *
     * @return
     */
    private boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation != Configuration.ORIENTATION_LANDSCAPE;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        dismissPopupWindow();
        mController.hide();
    }

    /**
     * 是否已经锁定
     *
     * @return
     */
    public boolean isLocked() {
        return mController.isLocked();
    }

    private String getString(@StringRes int id, Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    class MyMediaExtraControl implements MediaPlayerController.MediaExtraControl {
        @Override
        public void fullScreen() {
            if (getContext() instanceof Activity) {
                Activity activity = (Activity) getContext();
                if (isPortrait()) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        }


        @Override
        public void showDefinitionMenu(View v) {
            showDefinitionSelect();
        }

        @Override
        public void showSpeedMenu(View v) {
            if (mVideoView.isSupportSpeed()) {
                showSpeedSelect();
            } else {
                Toast.makeText(getContext().getApplicationContext(),
                        R.string.bsyv_p_not_support_speed,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onBackClick() {
            Context context = getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.finish();
            }
        }

        @Override
        public boolean canRotateScreen() {
            return mSupportOrientation;
        }

        @Override
        public String getVideoTitle() {
            return mTitle;
        }

        @Override
        public String getDefinitionName() {
            return mVideoView.getCurDefinition().getDefinitionName();
        }

        @Override
        public boolean isLoadVideo() {
            return mIsLocal;
        }
    }
}
