//package com.baoshiyun.trailblazer.video;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.hardware.SensorManager;
//import android.media.AudioManager;
//import android.os.Bundle;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.baoshiyun.trailblazer.R;
//import com.baoshiyun.warrior.video.player.exception.BSYPlayException;
//import com.baoshiyun.videoui.BSYVideoPlayView;
//import com.baoshiyun.videoui.bean.BSYPlaylist;
//import com.baoshiyun.videoui.utils.OrientationEventListener;
//
//
//public class BSYPlayerActivity extends AppCompatActivity {
//    private static final String PLAYLIST = "playlist";
//    // 底部区域
//    private View mBelowContainer;
//    // video play view
//    private BSYVideoPlayView mVideoPlayView;
//    // 是否支持旋转
//    private boolean mSupportOrientation = true;
//    private OrientationEventListener mOrientationEventListener;
//    // 播放列表
//    private BSYPlaylist mPlaylist;
//
//    /**
//     * 启动播放器
//     *
//     * @param context  上下文
//     * @param playlist 播放列表
//     */
//    public static void startPlayer(Context context, BSYPlaylist playlist) {
//        Intent intent = new Intent(context, BSYPlayerActivity.class);
//        intent.putExtra(PLAYLIST, playlist);
//        context.startActivity(intent);
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        // 禁止截屏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
//        // 保持屏幕常亮
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        // 音量控制类型 music
//        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//
//        mPlaylist = (BSYPlaylist) getIntent().getSerializableExtra(PLAYLIST);
//        setContentView(R.layout.activity_player);
//
//        mBelowContainer = findViewById(R.id.player_below_container);
//        mVideoPlayView = findViewById(R.id.player_video_container);
//        mVideoPlayView.setOnEventListener(new BSYVideoPlayView.OnEventListener() {
//            @Override
//            public void onBack() {
//                onBackPressed();
//            }
//
//            @Override
//            public void onExit() {
//                finish();
//            }
//
//            @Override
//            public void onError(BSYPlayException e) {
//            }
//        });
//        mVideoPlayView.setSupportOrientation(mSupportOrientation);
//
//        mVideoPlayView.setPlaylist(mPlaylist);
//
//        if (mSupportOrientation) {
//            // 屏幕方向监听器
//            mOrientationEventListener =
//                    new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
//                        @Override
//                        public void onScreenOrientationChanged(int orientation) {
//                            if (!mVideoPlayView.isLocked()) {
//                                setRequestedOrientation(orientation);
//                            }
//                        }
//                    };
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//        }
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mVideoPlayView.onPageResume();
//        if (mOrientationEventListener != null) {
//            mOrientationEventListener.enable();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mVideoPlayView.onPagePause();
//        if (mOrientationEventListener != null) {
//            mOrientationEventListener.disable();
//        }
//        // 上报
//        reportProgress();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mVideoPlayView.onPageStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mVideoPlayView.onDestroy();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mVideoPlayView.isLocked()) {
//            Toast.makeText(this, R.string.player_screen_locked, Toast.LENGTH_SHORT).show();
//        } else {
//            if (mSupportOrientation && !isPortrait()) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            } else {
//                super.onBackPressed();
//            }
//        }
//    }
//
//    /**
//     * 是否是竖屏
//     *
//     * @return
//     */
//    private boolean isPortrait() {
//        int orientation = getResources().getConfiguration().orientation;
//        return orientation == Configuration.ORIENTATION_PORTRAIT;
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (isPortrait()) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            mBelowContainer.setVisibility(View.VISIBLE);
//        } else {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            mBelowContainer.setVisibility(View.GONE);
//        }
//    }
//
//    private void reportProgress() {
////        int playIndex = mVideoPlayView.getPlayIndex();
////        boolean isLastMedia = (playIndex + 1 == mPlaylist.getMediaList().size());
////        UIMediaInfo mCurInfo = mPlaylist.getMediaList().get(playIndex);
////        int currentPosition = mVideoPlayView.getCurrentPosition();
////        int duration = mVideoPlayView.getDuration();
////
////        float curVideoProgress = currentPosition / (float) duration * 100;
////        // 学过的或者跳过的记录为100%
////        float totalProgress = playIndex * 100 + curVideoProgress;
////        float progress = (totalProgress / (mPlaylist.getMediaList().size() * 100)) * 100;
////
////        if (progress > 90) {
////            progress = 100;
////        }
////
////        // 上报学习时长
////        mStudyReportManager.sendReport(progress);
////
////
////        int lastStudyPosition = currentPosition / 1000;
////        String lastStudyVideoId = mCurInfo.getMediaId();
////        // 全部播放完需要重置
////        if (currentPosition >= duration - 1000 && isLastMedia) {
////            lastStudyPosition = 0;
////            lastStudyVideoId = mPlaylist.getMediaList().get(0).getMediaId();
////        }
////        // 上报学习历史
////        mStudyReportManager.reportPlayHistory(lastStudyVideoId, lastStudyPosition);
//    }
//}