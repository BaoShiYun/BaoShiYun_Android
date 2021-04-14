
package com.baoshiyun.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baoshiyun.live.R;
import com.baoshiyun.warrior.live.Speaker;

/**
 * 直播间 副机位的课件 view
 * Created by lht on 2021/3/1.
 */
public class VideoItemView extends FrameLayout {

    private View mMicCloseTag;
    private TextView mNicknameTv;
    private View mVolumeTag;

    Runnable mGoneVolumeViewRunnable = new Runnable() {
        @Override
        public void run() {
            mVolumeTag.setVisibility(View.GONE);
        }
    };
    // 互动成员数据
    private Speaker mSpeaker;
    private View mLastRenderView;

    public VideoItemView(@NonNull Context context) {
        this(context, null);
    }

    public VideoItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.layout_video_item, this);
        mMicCloseTag = findViewById(R.id.video_mic_close);
        mNicknameTv = findViewById(R.id.video_nickname);
        mVolumeTag = findViewById(R.id.video_volume_tag);
    }

    /**
     * 设置数据
     *
     * @param speaker 直播间互动者
     */
    public void setSpeaker(Speaker speaker) {
        if (speaker != null) {
            mSpeaker = speaker;

            String nickname = speaker.getNickname();
            // 展示自己的样式的框
            if (speaker.isSelf()) {
                nickname = "自己：" + nickname;
            } else {
                nickname = "学员：" + nickname;
            }
            mNicknameTv.setText(nickname);
        }
    }

    /**
     * 设置视频渲染view
     *
     * @param renderView
     */
    public void setRenderView(View renderView) {
        // 因为可能会多次设置，所以先移除掉再添加到父容器
        if (mLastRenderView != null) {
            removeView(mLastRenderView);
            mLastRenderView = null;
        }
        mLastRenderView = renderView;
        addView(renderView, 0, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 移除渲染view
     */
    public void removeRenderView() {
        if (mLastRenderView != null) {
            removeView(mLastRenderView);
            mLastRenderView = null;
        }
    }

    /**
     * 设置 用户mic 状态
     *
     * @param isOn 是否开启
     */
    public void setMicStatus(boolean isOn) {
        if (isOn) {
            mMicCloseTag.setVisibility(View.GONE);
        } else {
            mMicCloseTag.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置用户是否有音频传输
     *
     * @param volume
     */
    public void setVolumeChanged(int volume) {
        mVolumeTag.removeCallbacks(mGoneVolumeViewRunnable);
        if (volume > 10) {
            mVolumeTag.setVisibility(View.VISIBLE);
            mVolumeTag.postDelayed(mGoneVolumeViewRunnable, 3000);
        } else {
            mVolumeTag.setVisibility(View.GONE);
        }
    }
}
