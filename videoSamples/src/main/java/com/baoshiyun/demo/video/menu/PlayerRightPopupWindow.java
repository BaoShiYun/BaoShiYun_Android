package com.baoshiyun.demo.video.menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.video.R;
import com.baoshiyun.warrior.core.utils.DensityUtil;

import java.lang.reflect.Field;

/**
 * Created by liujunting on 2019-04-28.
 * 播放器右边的弹出菜单，播放列表，清晰度选择，倍速选择等
 */
public class PlayerRightPopupWindow {

    private PopupWindow mPopupWindow;
    private Context mContext;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private TextView mMenuTitleTv;

    public PlayerRightPopupWindow(Context context) {

        mContext = context;
        mRootView = View.inflate(context, R.layout.bsyv_p_right_menu_layout, null);

        int width = DensityUtil.dp2px(context, 180);

        mPopupWindow = new PopupWindow(mRootView, width, ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setAnimationStyle(R.style.BSY_player_ui_popwin_anim_style);

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.update();

        // 设置全屏显示
        mPopupWindow.setClippingEnabled(false);
        initView();
    }

    public static void fitPopupWindowOverStatusBar(PopupWindow mPopupWindow,
                                                   boolean needFullScreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                mLayoutInScreen.setAccessible(needFullScreen);
                mLayoutInScreen.set(mPopupWindow, needFullScreen);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        mRecyclerView = mRootView.findViewById(R.id.player_right_menu_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mMenuTitleTv = mRootView.findViewById(R.id.player_right_menu_title);
    }

    protected void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    protected void show(View parentView) {
        mPopupWindow.showAtLocation(parentView, Gravity.RIGHT, 0, 0);

    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    public void setTitle(@StringRes int id) {
        mMenuTitleTv.setText(id);
    }

}
