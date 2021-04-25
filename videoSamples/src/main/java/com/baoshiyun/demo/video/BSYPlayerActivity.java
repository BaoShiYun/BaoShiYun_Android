package com.baoshiyun.demo.video;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baoshiyun.demo.utils.OrientationEventListener;
import com.baoshiyun.video.R;
import com.baoshiyun.warrior.video.Definition;

/**
 * 视频播页面
 */
public class BSYPlayerActivity extends AppCompatActivity {
    public static final String MEDIA_TITLE = "VIDEO_TITLE";
    public static final String MEDIA_ID = "VIDEO_ID";
    public static final String LOCAL_FILE_PATH = "LOCAL_FILE_PATH";
    public static final String IS_LOCAL = "IS_LOCAL";
    // 底部区域
    private View mBelowContainer;
    // video play view
    private BSYVideoPlayView mVideoPlayView;
    private BSYVideoView videoView;

    // 是否支持屏幕旋转
    boolean mSupportOrientation = false;
    private OrientationEventListener mOrientationEventListener;
    private String mVideoTitle;
    private String mMediaId;
    private boolean mIsLocal;
    private String mLocalFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mVideoTitle = intent.getStringExtra(MEDIA_TITLE);
        mMediaId = intent.getStringExtra(MEDIA_ID);
        mLocalFilePath = intent.getStringExtra(LOCAL_FILE_PATH);
        mIsLocal = intent.getBooleanExtra(IS_LOCAL, false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 音量控制类型 music
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.bsyv_activity_player);

        // 加载view
        mBelowContainer = findViewById(R.id.player_below_container);
        mVideoPlayView = findViewById(R.id.player_video_container);
        mVideoPlayView.setSupportOrientation(mSupportOrientation);

        // videoView 支持更多的操作
        videoView = mVideoPlayView.getVideoView();
        videoView.setDefaultDefinition(Definition.LHD);

        // 是否启用屏幕方向监听器
        if (mSupportOrientation) {
            initScreenOrientationListener();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        mVideoPlayView.setPlaySource(mMediaId, mMediaId, mIsLocal, mLocalFilePath);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mVideoPlayView.onPageResume();
        if (mOrientationEventListener != null) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoPlayView.onPagePause();
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoPlayView.onPageStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mVideoPlayView.isLocked()) {
            Toast.makeText(this, R.string.bsyv_screen_locked, Toast.LENGTH_SHORT).show();
        } else {
            if (mSupportOrientation && !isPortrait()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * 是否是竖屏
     *
     * @return
     */
    private boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isPortrait()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mBelowContainer.setVisibility(View.VISIBLE);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mBelowContainer.setVisibility(View.GONE);
        }
    }


    /**
     * 初始化屏幕方向监听器
     */
    private void initScreenOrientationListener() {
        // 屏幕方向监听器
        mOrientationEventListener =
                new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                    @Override
                    public void onScreenOrientationChanged(int orientation) {
                        if (!mVideoPlayView.isLocked()) {
                            setRequestedOrientation(orientation);
                        }
                    }
                };
    }
}