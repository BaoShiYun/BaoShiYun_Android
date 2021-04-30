package com.baoshiyun.demo.video;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.baoshiyun.demo.AuthorizationManager;
import com.baoshiyun.demo.video.controller.MediaPlayerController;
import com.baoshiyun.warrior.core.NetWatchDog;
import com.baoshiyun.warrior.video.Definition;
import com.baoshiyun.warrior.video.player.BSYPlayerFactory;
import com.baoshiyun.warrior.video.player.IMediaPlayer;
import com.baoshiyun.warrior.video.player.exception.BSYPlayException;
import com.baoshiyun.warrior.video.player.exception.ErrorCode;
import com.baoshiyun.warrior.video.utils.VideoLog;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BSYVideoView extends SurfaceView implements MediaPlayerController.IMediaPlayerControl {

    public static final String TAG = BSYVideoView.class.getSimpleName();
    public static final int VIDEO_SCALE_MODEL_FULL = 1; // 视频缩放模式 全屏模式，会拉伸
    public static final int VIDEO_SCALE_MODEL_CORRECT = 2; // 视频缩放模式 修正模式
    private int mVideoScaleModel = VIDEO_SCALE_MODEL_CORRECT;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // settable by the client
    private String mMediaId;
    private String mLocalFilePath;
    private Map<String, String> mHeaders;
    private Boolean mIsLocal;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaPlayerController mMediaController;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.onPlayErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    // 音频焦点处理
    private AudioManager mAudioManager;
    private int mAudioFocusType = AudioManager.AUDIOFOCUS_GAIN; // legacy focus gain
    // 26版本以上焦点处理
    private AudioAttributes mAudioAttributes;
    private AudioFocusRequest mAudioFocusRequest;

    private int mRetryCount = 0;
    // 清晰度列表
    private List<Definition> mDefinitions;
    // 当前清晰度
    private Definition mCurDefinition = Definition.LSD;
    // 视频的时长，播放之后更新，播放失败或者完成后，外部再次使用时，用此数据
    private int mVideoDuration;

    public BSYVideoView(Context context) {
        this(context, null);
    }

    public BSYVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BSYVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (isLaterOApi()) {
            mAudioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();
            mAudioFocusRequest = createAudioFocus(mAudioFocusType);
        }

        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        if (mVideoScaleModel == VIDEO_SCALE_MODEL_CORRECT
                && mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return BSYVideoView.class.getName();
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    public void setOnlineData(String mediaId, Map<String, String> headers) {
        this.mMediaId = mediaId;
        mIsLocal = false;
        mHeaders = headers;

        mRetryCount = 0;
        mSeekWhenPrepared = 0;
        mVideoDuration = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setOfflineData(String mediaId, String filePath) {
        this.mMediaId = mediaId;
        this.mLocalFilePath = filePath;
        mIsLocal = true;

        mRetryCount = 0;
        mSeekWhenPrepared = 0;
        mVideoDuration = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        removeCallbacks(retryPlayRunnable);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
        abandonAudioFocus();
    }

    private void openVideo() {
        if (mSurfaceHolder == null || mMediaId == null) {
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        // 申请音频焦点
        requestAudioFocus();

        Exception exception = null;

        try {
            final Context context = getContext();
            if (mMediaPlayer == null) {
                mMediaPlayer = BSYPlayerFactory.createMediaPlayer(
                        AuthorizationManager.accessToken,
                        AuthorizationManager.tenantId,
                        AuthorizationManager.userId);
            }

            // 更新 accessToken,防止已经失效
            mMediaPlayer.updateAccessToken(AuthorizationManager.accessToken);

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnPlayErrorListener(mInnerOnPlayErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;

            mMediaPlayer.setDefaultDefinition(mCurDefinition);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            if (isLaterOApi()) {
                mMediaPlayer.setAudioAttributes(mAudioAttributes);
            } else {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            mMediaPlayer.setScreenOnWhilePlaying(true);

            if (mIsLocal) {
                // 离线视频，传入的清晰度主要用于UI展示时统一的获取规则设定，无实际用途
                mMediaPlayer.setOfflineData(context, mMediaId, mLocalFilePath, Definition.LHD);
            } else {
                mMediaPlayer.setOnlineData(context, mMediaId, mHeaders);
            }

            // 准备播放
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            exception = ex;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
        } catch (IllegalArgumentException ex) {
            exception = ex;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
        } catch (IllegalStateException ex) {
            exception = ex;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
        } finally {
            // 上报错误
            if (exception != null) {
                exception.printStackTrace();
                mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }
        }
    }

    public void setMediaController(MediaPlayerController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    public void setDefinition(Definition definition) {
        if (isInPlaybackState()) {
            int curPos = getCurrentPosition();
            boolean isPlaying = isPlaying();

            reset();

            mMediaPlayer.setDisplay(mSurfaceHolder);
            try {
                mMediaPlayer.changeDefinition(getContext(), definition);
                // 更新清晰度
                mCurDefinition = definition;
            } catch (IOException e) {
                VideoLog.w(TAG, "设置清晰度失败 definition=" + definition + " e=" + e.toString());
                e.printStackTrace();
                // 重新打开视频
                openVideo();
            }
            if (curPos > 0) {
                seekTo(curPos);
            }
            if (isPlaying) {
                start();
            }
        }
    }

    /**
     * 设置默认的清晰度
     */
    public void setDefaultDefinition(Definition definition) {
        mCurDefinition = definition;
    }

    /**
     * 获取清晰度列表
     */
    public List<Definition> getDefinitions() {
        return mDefinitions;
    }

    /**
     * 获取当前的清晰度
     */
    public Definition getCurDefinition() {
        return mCurDefinition;
    }


    public boolean isSupportSpeed() {
        return mMediaPlayer.isSupportSpeed();
    }

    public void setSpeed(float speed) {
        if (isSupportSpeed() && isInPlaybackState()) {
            mMediaPlayer.setSpeed(speed);
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            List<Definition> definitions = mp.getDefinitions();
            Definition curDefinition = mp.getCurDefinition();
            // mediaPlayer 清晰度不为空使用 mediaPlayer的数据
            if (definitions != null) {
                mDefinitions = definitions;
                mCurDefinition = curDefinition;
            }

            mRetryCount = 0;

            mCurrentState = STATE_PREPARED;
            // TODO: 默认 全部可用，其实可以读取视频信息判断是否可以前进和后退
            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after
            // seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start();
                    if (mMediaController != null) {
                        mMediaController.show();
                    }
                } else if (mTargetState == STATE_PAUSED) {
                    pause();
                } else if (!isPlaying() &&
                        (seekToPosition != 0 || getCurrentPosition() > 0)) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make
                        // 'em stick.
                        mMediaController.show(0);
                    }
                }
//                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                } else if (mTargetState == STATE_PAUSED) {
                    pause();
                }
            }
        }
    };


    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    abandonAudioFocus();
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, what, extra);
                    }
                    return true;
                }
            };
    private IMediaPlayer.onPlayErrorListener mInnerOnPlayErrorListener =
            new IMediaPlayer.onPlayErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, BSYPlayException e) {
                    VideoLog.w(TAG, "onError e:" + e.toString());
                    if (mOnErrorListener != null) {
                        return mOnErrorListener.onError(mp, e);
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    VideoLog.w(TAG, "Error: " + what + "," + extra);
                    // 没网不上报了，因为没网播放失败属于正常
                    Context context = getContext();
                    if (context != null && NetWatchDog.hasNet(context)) {
                        String errorMsg = "播放失败 {retryCount=" + mRetryCount + ", media=" + mMediaId + ", what=" + what + ", extra=" + extra + "}";
                        VideoLog.w(TAG, errorMsg);
                    }
                    // 保存失败后重试需要回复的播放进度
                    mSeekWhenPrepared = getCurrentPosition();
                    // 通知再重新加载需要loading
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
                    }

                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mRetryCount <= 3) {
                        mRetryCount++;
                        postDelayed(retryPlayRunnable, 2000);
                        return true;
                    }
                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        BSYPlayException bsyPlayException
                                = new BSYPlayException(ErrorCode.PLAY_ERROR, "what:" + what + ", extra:" + extra);
                        if (mOnErrorListener.onError(mp, bsyPlayException)) {
                            return true;
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (getWindowToken() != null) {
                        String messageId;
                        if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = "对不起,无法获.到视频流.";
                        } else {
                            messageId = "播放失败,遇到未知错误.";
                        }
                        new AlertDialog.Builder(getContext())
                                .setMessage(messageId)
                                .setPositiveButton("播放失败", (dialog, whichButton) -> {
                                    if (mOnCompletionListener != null) {
                                        mOnCompletionListener.onCompletion(mMediaPlayer);
                                    }

                                })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };

    Runnable retryPlayRunnable = () -> {
        int currentPosition = getCurrentPosition();
        openVideo();
        seekTo(currentPosition);
        start();
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.onPlayErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
//            if (mMediaPlayer != null &&isValidState &&  hasValidSize) {
            if (mMediaPlayer != null && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                if (mTargetState == STATE_PLAYING) {
                    start();
                } else if (mTargetState == STATE_PAUSED) {
                    pause();
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            // 如果不是可播放状态，初始化播放时
            if (!isInPlaybackState()) {
                openVideo();
            } else {
                // 设置holder直接播放
                mMediaPlayer.setDisplay(mSurfaceHolder);
                if (mTargetState == STATE_PLAYING) {
                    start();
                } else if (mTargetState == STATE_PAUSED) {
                    pause();
                }
                if (mMediaController != null) mMediaController.show();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            if (isInPlaybackState()) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentState = STATE_PAUSED;
                }
            } else {
                // 如果不是可播放状态则暂停播放，防止后台后还在播放
                pause();
            }

        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
//            abandonAudioFocus();
        }
    }

    private void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            requestAudioFocus();
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            mVideoDuration = mMediaPlayer.getDuration();
        }
        return mVideoDuration;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        // 如果没有在可播放状态，使用目标的 seek 位置
        return mSeekWhenPrepared;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            if (msec >= getDuration()) {
                msec = getDuration() - 1000;
            }
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public void seekToForUI(int pos) {
        if (isInPlaybackState()) {
            if (pos >= getDuration()) {
                pos = getDuration() - 1000;
            }
            mMediaPlayer.seekToForUI(pos);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = pos;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    /**
     * 是否是在可以播放状态
     *
     * @return
     */
    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    /**
     * Sets which type of audio focus will be requested during the playback, or configures playback
     * to not request audio focus. Valid values for focus requests are
     * {@link AudioManager#AUDIOFOCUS_GAIN}, {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT},
     * {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK}, and
     * {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE}. Or use
     * {@link AudioManager#AUDIOFOCUS_NONE} to express that audio focus should not be
     * requested when playback starts. You can for instance use this when playing a silent animation
     * through this class, and you don't want to affect other audio applications playing in the
     * background.
     *
     * @param focusGain the type of audio focus gain that will be requested, or
     *                  {@link AudioManager#AUDIOFOCUS_NONE} to disable the use audio focus
     *                  during playback.
     */
    public void setAudioFocusRequest(int focusGain) {
        checkAudioFocusType(focusGain);
        mAudioFocusType = focusGain;
        if (focusGain != AudioManager.AUDIOFOCUS_NONE && isLaterOApi()) {
            mAudioFocusRequest = createAudioFocus(focusGain);
        }

    }

    /**
     * Sets the {@link AudioAttributes} to be used during the playback of the video.
     *
     * @param attributes non-null <code>AudioAttributes</code>.
     */
    @RequiresApi(26)
    public void setAudioAttributes(@NonNull AudioAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        mAudioAttributes = attributes;
    }

    @RequiresApi(26)
    private AudioFocusRequest createAudioFocus(int focusGain) {
        return new AudioFocusRequest.Builder(focusGain)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(false)
                .setAudioAttributes(mAudioAttributes)
                .setOnAudioFocusChangeListener(mAudioFocusListener)
                .build();
    }

    /**
     * 检查FocusType是否合法
     *
     * @param focusType
     * @return
     */
    private boolean checkAudioFocusType(int focusType) {
        if (focusType != AudioManager.AUDIOFOCUS_NONE
                && focusType != AudioManager.AUDIOFOCUS_GAIN
                && focusType != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                && focusType != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                && focusType != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {
            throw new IllegalArgumentException("Illegal audio focus type " + focusType);
        }
        return true;
    }

    /**
     * 申请音频焦点
     */
    private void requestAudioFocus() {
        if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            if (isLaterOApi()) {
                mAudioManager.requestAudioFocus(mAudioFocusRequest);
            } else {
                mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                        mAudioFocusType);
            }
        }
    }

    /**
     * 释放焦点
     */
    private void abandonAudioFocus() {
        if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        }
    }

    /**
     * 是否是O以后的版本
     *
     * @return
     */
    private boolean isLaterOApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }


    boolean mWhenAudioFocusGainStart = false;
    /**
     * 焦点被强占的状态处理，因为没有后台播放暂时未处理
     */
    AudioManager.OnAudioFocusChangeListener mAudioFocusListener = (focusChange) -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: // 长时间失去焦点，恢复的可能性非常小
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // 短暂失去焦点，恢复可能性非常大
                if (isPlaying()) {
                    mWhenAudioFocusGainStart = true;
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // 失去焦点，但可以小音量播放 Q以上系统自动控制，不需处理
                break;
            case AudioManager.AUDIOFOCUS_GAIN: // 获得音频焦点
                if (mWhenAudioFocusGainStart) {
                    mWhenAudioFocusGainStart = false;
                    start();
                }

                break;
        }
    };


    @Override
    public void onScale(boolean zoomIn) {
        // 放大
        if (zoomIn) {
            if (mVideoScaleModel != VIDEO_SCALE_MODEL_FULL) {
                mVideoScaleModel = VIDEO_SCALE_MODEL_FULL;
                requestLayout();
            }
        } else { // 缩小
            if (mVideoScaleModel != VIDEO_SCALE_MODEL_CORRECT) {
                mVideoScaleModel = VIDEO_SCALE_MODEL_CORRECT;
                requestLayout();

            }
        }
    }
}
