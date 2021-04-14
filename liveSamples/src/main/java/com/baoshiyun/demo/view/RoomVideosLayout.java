package com.baoshiyun.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.baoshiyun.warrior.live.Speaker;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ljt on 2021/3/19.
 */
public class RoomVideosLayout extends HorizontalScrollView {
    private VideoItemView mTeacherVideoView;
    private String mTeacherUid;
    private HashMap<String, VideoItemView> mOtherVideoViews = new HashMap<>();
    private LinearLayout mSeatVideoContainer;

    public RoomVideosLayout(Context context) {
        this(context, null);
    }

    public RoomVideosLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoomVideosLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mSeatVideoContainer = new LinearLayout(getContext());
        mSeatVideoContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mSeatVideoContainer);
        // 老师是固定位置
        mTeacherVideoView = new VideoItemView(getContext());
        mSeatVideoContainer.addView(mTeacherVideoView, 0, createLayoutParams());
    }

    private LinearLayout.LayoutParams createLayoutParams() {
        int measuredHeight = getMeasuredHeight();
        return new LinearLayout.LayoutParams((int) (measuredHeight / 3F * 4), measuredHeight);
    }

    /**
     * 互动成员变化
     *
     * @param speakers
     */
    public void speakerChanged(List<Speaker> speakers) {
        // 清除其他人的所有视频view
        mOtherVideoViews.clear();
        mSeatVideoContainer.removeAllViews();
        // 重新把老师的添加到第一个
        if (mTeacherVideoView != null) {
            mSeatVideoContainer.addView(mTeacherVideoView, 0, createLayoutParams());
        }

        // 只负责添加 view 不处理 用户视频状态
        for (Speaker speaker : speakers) {
            String uid = speaker.getUid();
            // 创建视频 view 并添加到容器中
            VideoItemView videoItemView = new VideoItemView(getContext());
            // 设置成员数据
            videoItemView.setSpeaker(speaker);
            mSeatVideoContainer.addView(videoItemView, createLayoutParams());

            // 记录 uid 对应的 view
            mOtherVideoViews.put(uid, videoItemView);
        }
    }

    /**
     * 设置老师渲染view
     *
     * @param uid
     * @param renderView
     */
    public void setupTeacherRenderView(String uid, View renderView) {
        mTeacherVideoView.setRenderView(renderView);
        mTeacherUid = uid;
    }

    /**
     * 移除老师渲染view
     */
    public void removeTeacherRenderView() {
        mTeacherVideoView.removeRenderView();
    }

    /**
     * 设置学生渲染view
     *
     * @param uid
     * @param renderView
     */
    public void setupStudentRenderView(String uid, View renderView) {
        // 如果互动列表中不存在则不处理
        VideoItemView videoItemView = mOtherVideoViews.get(uid);
        if (videoItemView != null) {
            videoItemView.setRenderView(renderView);
        }
    }

    /**
     * 移除学生渲染view
     */
    public void removeStudentRenderView(String uid) {
        // 如果互动列表中不存在则不处理
        VideoItemView videoItemView = mOtherVideoViews.get(uid);
        if (videoItemView != null) {
            videoItemView.removeRenderView();
        }
    }

    /**
     * 用户音频状态变化
     *
     * @param uid
     * @param isOn
     */
    public void onAudioStateChanged(String uid, boolean isOn) {
        if (uid.equals(mTeacherUid)) {
            // 老师音频状态变更
            mTeacherVideoView.setMicStatus(isOn);
        } else {
            // 其他用户音频状态变更
            VideoItemView videoItemView = mOtherVideoViews.get(uid);
            if (videoItemView != null) {
                videoItemView.setMicStatus(isOn);
            }
        }
    }

    /**
     * 用户音量变化
     *
     * @param uid
     * @param volume
     */
    public void onAudioVolumeIndication(String uid, int volume) {
        if (uid.equals(mTeacherUid)) {
            // 老师音量变更
            mTeacherVideoView.setVolumeChanged(volume);
        } else {
            // 其他用户音量变更
            VideoItemView videoItemView = mOtherVideoViews.get(uid);
            if (videoItemView != null) {
                videoItemView.setVolumeChanged(volume);
            }
        }
    }
}
