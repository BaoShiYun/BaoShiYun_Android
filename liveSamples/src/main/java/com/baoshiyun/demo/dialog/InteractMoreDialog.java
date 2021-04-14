package com.baoshiyun.demo.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.baoshiyun.live.R;
import com.baoshiyun.warrior.live.BSYRoomSdk;
import com.flyco.roundview.RoundTextView;

/**
 * 互动 直播 更多功能的弹框
 * Created by ljt on 2021/3/24.
 */
public class InteractMoreDialog extends BottomSheetDialog {
    private final BSYRoomSdk mBsyRoom;
    private RoundTextView mRaiseHandBtn;
    private TextView mCameraBtn;
    private TextView mMicBtn;

    private boolean mMuteLocalVideo;
    private boolean mMuteLocalAudio;
    private boolean mVideoDisable;
    private boolean mAudioDisable;

    /**
     * @param context
     * @param bsyRoom 直播间实例
     */
    public InteractMoreDialog(Context context, BSYRoomSdk bsyRoom) {
        super(context);
        this.mBsyRoom = bsyRoom;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_interact_more);
        mRaiseHandBtn = findViewById(R.id.interact_dialog_raise_hand_btn);

        mCameraBtn = findViewById(R.id.interact_dialog_camera_btn);
        mMicBtn = findViewById(R.id.interact_dialog_mic_btn);

        setRaiseHandDisable(mBsyRoom.isRaiseHandDisable());
    }

    /**
     * 是否禁止举手
     *
     * @param raiseHandDisable
     */
    public void setRaiseHandDisable(boolean raiseHandDisable) {
        if (raiseHandDisable) {
            mRaiseHandBtn.setOnClickListener(null);
            mRaiseHandBtn.setText("禁止互动");
            mRaiseHandBtn.getDelegate().setBackgroundColor(Color.parseColor("#424242"));
            setCameraState(true, true);
            setMicState(true, true);
        } else {

            if (mBsyRoom.isSpeaking()) {
                mRaiseHandBtn.setText("下台");
                mRaiseHandBtn.getDelegate().setBackgroundColor(Color.parseColor("#FF2935"));
                setStopSpeakClick();
            } else if (mBsyRoom.isRaiseHand()) {
                mRaiseHandBtn.setText("取消举手");
                mRaiseHandBtn.getDelegate().setBackgroundColor(Color.parseColor("#262626"));
                setStopRaiseHandClick();
            } else {
                mRaiseHandBtn.setText("举手");
                mRaiseHandBtn.getDelegate().setBackgroundColor(Color.parseColor("#262626"));
                setRaiseHandClick();
            }

            setCameraState(mBsyRoom.isLocalVideoDisable(), mBsyRoom.isMuteLocalVideo());
            setMicState(mBsyRoom.isLocalAudioDisable(), mBsyRoom.isMuteLocalAudio());
        }
    }

    /**
     * 麦克风状态变更
     *
     * @param audioDisable
     * @param muteLocalAudio
     */
    public void setMicState(boolean audioDisable, boolean muteLocalAudio) {
        if (mMicBtn == null) {
            return;
        }
        this.mAudioDisable = audioDisable;
        this.mMuteLocalAudio = muteLocalAudio;
        updateMicState(audioDisable, muteLocalAudio);

        if (audioDisable) {
            mMicBtn.setOnClickListener(null);
        } else {
            mMicBtn.setOnClickListener(v -> {
                mMuteLocalAudio = !mMuteLocalAudio;
                mBsyRoom.muteLocalAudio(mMuteLocalAudio);
                updateMicState(mAudioDisable, mMuteLocalAudio);
            });
        }
    }

    /**
     * 摄像头状态变更
     *
     * @param videoDisable
     * @param muteLocalVideo
     */
    public void setCameraState(boolean videoDisable, boolean muteLocalVideo) {
        if (mCameraBtn == null) {
            return;
        }
        this.mVideoDisable = videoDisable;
        this.mMuteLocalVideo = muteLocalVideo;
        updateCameraState(videoDisable, muteLocalVideo);

        if (videoDisable) {
            mCameraBtn.setOnClickListener(null);
        } else {
            mCameraBtn.setOnClickListener(v -> {
                mMuteLocalVideo = !mMuteLocalVideo;
                mBsyRoom.muteLocalVideo(mMuteLocalVideo);
                updateCameraState(mVideoDisable, mMuteLocalVideo);
            });
        }
    }


    private void updateCameraState(boolean videoDisable, boolean muteLocalVideo) {
        int drawableId = R.mipmap.ic_interact_camera_disable;
        if (videoDisable) {
            mCameraBtn.setText("已禁用");
        } else if (muteLocalVideo) {
            mCameraBtn.setText("已关闭");
            drawableId = R.mipmap.ic_interact_camera_closed;
        } else {
            mCameraBtn.setText("已开启");
            drawableId = R.mipmap.ic_interact_camera_opened;
        }
        Drawable drawable = ResourcesCompat.getDrawable(getContext().getResources(), drawableId, null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mCameraBtn.setCompoundDrawables(null, drawable, null, null);
    }

    private void updateMicState(boolean audioDisable, boolean muteLocalAudio) {
        int drawableId = R.mipmap.ic_interact_mic_disable;
        if (audioDisable) {
            mMicBtn.setText("已禁用");
        } else if (muteLocalAudio) {
            mMicBtn.setText("已关闭");
            drawableId = R.mipmap.ic_interact_mic_closed;
        } else {
            mMicBtn.setText("已开启");
            drawableId = R.mipmap.ic_interact_mic_opened;
        }
        Drawable drawable = ResourcesCompat.getDrawable(getContext().getResources(), drawableId, null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mMicBtn.setCompoundDrawables(null, drawable, null, null);
    }

    /**
     * 设置举手点击
     */
    private void setRaiseHandClick() {
        mRaiseHandBtn.setOnClickListener(v ->
                mBsyRoom.raiseHand(new BSYRoomSdk.OnCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getContext(), "举手成功", Toast.LENGTH_LONG).show();
                        // TODO: 如果不需要关闭弹框，需要重置举手按钮的状态
                        dismiss();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        Toast.makeText(getContext(), "举手失败", Toast.LENGTH_LONG).show();
                    }
                }));
    }

    /**
     * 设置停止举手点击
     */
    private void setStopRaiseHandClick() {
        mRaiseHandBtn.setOnClickListener(v ->
                mBsyRoom.stopRaiseHand(new BSYRoomSdk.OnCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getContext(), "取消举手成功", Toast.LENGTH_LONG).show();
                        // TODO: 如果不需要关闭弹框，需要重置举手按钮的状态
                        dismiss();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        Toast.makeText(getContext(), "取消举手失败", Toast.LENGTH_LONG).show();
                    }
                }));
    }

    /**
     * 设置 停止互动点击
     */
    private void setStopSpeakClick() {
        mRaiseHandBtn.setOnClickListener(v ->
                mBsyRoom.stopSpeak(new BSYRoomSdk.OnCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getContext(), "下台成功", Toast.LENGTH_LONG).show();
                        // TODO: 如果不需要关闭弹框，需要重置举手按钮的状态
                        dismiss();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        Toast.makeText(getContext(), "下台失败", Toast.LENGTH_LONG).show();
                    }
                }));
    }

}
