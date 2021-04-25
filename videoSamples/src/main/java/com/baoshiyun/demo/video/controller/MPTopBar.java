package com.baoshiyun.demo.video.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoshiyun.video.R;
import com.baoshiyun.warrior.core.NetWatchDog;

public class MPTopBar extends LinearLayout implements View.OnClickListener{
    private TextView mTitleTv;
    private View mBackBtn;
    private MediaPlayerController.MediaExtraControl mExtraControl;
    private ImageView mNetTag;
    private View mTopBarContainer;

    public MPTopBar(Context context) {
        this(context, null);
    }

    public MPTopBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MPTopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.bsyv_p_media_controller_top_bar, this);

        mTopBarContainer = findViewById(R.id.mc_top_bar_container);
        mNetTag = findViewById(R.id.mc_top_bar_net_tag);
        mTitleTv = findViewById(R.id.mc_top_bar_title);
        mBackBtn = findViewById(R.id.mc_top_bar_back);

        mBackBtn.setOnClickListener(this);
    }

    public void setMediaPlayer(MediaPlayerController.MediaExtraControl player) {
        this.mExtraControl = player;
    }

    /**
     * 处理一些不支持的控制按钮
     */
    public void disableUnsupportedButtons(boolean isPortrait) {
        if (mExtraControl == null) {
            return;
        }

        mTitleTv.setText(mExtraControl.getVideoTitle());
    }

    /**
     * 根据屏幕方向控制各个按钮是否显示
     *
     * @param orientation
     */
    public void updateControllerBtnShow(int orientation) {
        if (mExtraControl.isLoadVideo()) {
            mNetTag.setVisibility(View.VISIBLE);
            mNetTag.setImageResource(R.mipmap.bsyv_p_ic_tag_local);
        } else if (!NetWatchDog.hasNet(getContext())) {
            mNetTag.setVisibility(View.GONE);
        } else if (NetWatchDog.isMobileConnected(getContext())) {
            mNetTag.setVisibility(View.VISIBLE);
            mNetTag.setImageResource(R.mipmap.bsyv_p_ic_tag_mobile);
        } else if (NetWatchDog.isWifiConnected(getContext())) {
            mNetTag.setVisibility(View.VISIBLE);
            mNetTag.setImageResource(R.mipmap.bsyv_p_ic_tag_wifi);
        }
    }

    @Override
    public void onClick(View v) {
        if (mExtraControl == null) {
            return;
        }
        if (v.equals(mBackBtn)) {
            mExtraControl.onBackClick();
        }
    }

    public void setNotchScreen(int notchHeight) {
        mTopBarContainer.setPadding(notchHeight, 0, notchHeight, 0);
    }
}
