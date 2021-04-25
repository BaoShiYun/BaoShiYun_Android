package com.baoshiyun.demo.video.controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.baoshiyun.video.R;

public class PlayerStatusLayout extends RelativeLayout implements View.OnClickListener {

    public static final int STATUS_NONE = 0;
    public static final int STATUS_SHOW_4G = 1;
    public static final int STATUS_SHOW_ERROR = 2;
    public static final int STATUS_SHOW_PREPARE = 3;
    public static final int STATUS_SHOW_COMPLETE = 4;

    private int mCurStatus = STATUS_NONE;
    private TextView mTipsText;
    private TextView mActionText;
    private View mActionBtn;
    private View mBackBtn;
    private OnPlayerStatusEventListener mEventListener;

    private View mTipsContainer;
    private View mPrepareContainer;
    private TextView mPlayNextBtn;

    public PlayerStatusLayout(Context context) {
        this(context, null);
    }

    public PlayerStatusLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerStatusLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        View.inflate(getContext(), R.layout.bsyv_p_status_tips_layout, this);
        mTipsContainer = findViewById(R.id.player_status_tips_container);
        mPrepareContainer = findViewById(R.id.player_status_prepare_container);

        mTipsText = findViewById(R.id.player_status_tips_text);
        mActionText = findViewById(R.id.player_status_action_text);
        mActionBtn = findViewById(R.id.player_status_action_btn);
        mPlayNextBtn = findViewById(R.id.player_status_play_next);

        mBackBtn = findViewById(R.id.player_status_tips_back);


        mActionBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mPlayNextBtn.setOnClickListener(this);

        // 防止时间透传
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mActionBtn)) {
            if (mEventListener != null) {
                if (mCurStatus == STATUS_SHOW_4G) {
                    mEventListener.onMobileNetResume();
                } else if (mCurStatus == STATUS_SHOW_ERROR) {
                    mEventListener.onReload();
                }
            }
        } else if (v.equals(mBackBtn)) {
            if (mEventListener != null) {
                mEventListener.onBack();
            }
        }
    }

    /**
     * 显示使用4G播放视频
     */
    public void show4GTips() {
        mTipsContainer.setVisibility(View.VISIBLE);
        mPrepareContainer.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
        mCurStatus = STATUS_SHOW_4G;

        mPlayNextBtn.setVisibility(View.GONE);

        mTipsText.setText(R.string.bsyv_p_use_4g_tips);
        mActionText.setText(R.string.bsyv_p_resume);
        mActionText.setCompoundDrawables(getDrawable(R.mipmap.bsyv_p_ic_pause), null, null, null);
    }

    /**
     * 显示播放失败
     */
    public void showError(String text, boolean hasNext) {
        mTipsContainer.setVisibility(View.VISIBLE);
        mPrepareContainer.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
        mCurStatus = STATUS_SHOW_ERROR;

        mTipsText.setText(text);
        mActionText.setText(R.string.bsyv_p_reload);
        mActionText.setCompoundDrawables(getDrawable(R.mipmap.bsyv_p_ic_reload), null, null, null);

        if (hasNext) {
            mPlayNextBtn.setVisibility(View.VISIBLE);
            mPlayNextBtn.setText(R.string.bsyv_p_play_next);
        } else {
            mPlayNextBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 显示播放完成
     */
    public void showComplete() {
        mTipsContainer.setVisibility(View.VISIBLE);
        mPrepareContainer.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
        mCurStatus = STATUS_SHOW_COMPLETE;

        mTipsText.setText(R.string.bsyv_p_complete);
        mActionText.setText(R.string.bsyv_p_replay);
        mActionText.setCompoundDrawables(getDrawable(R.mipmap.bsyv_p_ic_reload), null, null, null);

        mPlayNextBtn.setVisibility(View.VISIBLE);
        mPlayNextBtn.setText(R.string.bsyv_p_exit);
    }

    /**
     * 显示准备视频loading
     */
    public void showPrepareLoading() {
        mCurStatus = STATUS_SHOW_PREPARE;
        this.setVisibility(View.VISIBLE);
        mTipsContainer.setVisibility(View.GONE);
        mPrepareContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mCurStatus = STATUS_NONE;
        this.setVisibility(View.GONE);
    }

    public int getCurStatus() {
        return mCurStatus;
    }


    private Drawable getDrawable(@DrawableRes int id) {
        Drawable drawable = getResources().getDrawable(id);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

    public void setonPlayerStatusEventListener(OnPlayerStatusEventListener l) {
        this.mEventListener = l;
    }

    public void setNotchScreen(int notchHeight) {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mBackBtn.getLayoutParams();
        layoutParams.leftMargin = layoutParams.leftMargin + notchHeight;
        mBackBtn.setLayoutParams(layoutParams);
    }

    public interface OnPlayerStatusEventListener {
        void onBack();

        void onReload();

        void onMobileNetResume();
    }
}
