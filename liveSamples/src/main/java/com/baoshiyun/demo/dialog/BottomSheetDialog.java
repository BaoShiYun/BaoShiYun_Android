package com.baoshiyun.demo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.baoshiyun.live.R;


/**
 * 通用 底部弹出，底部收起 dialog
 */
public class BottomSheetDialog extends Dialog {

    public BottomSheetDialog(@NonNull Context context) {
        this(context, R.style.BottomSheetDialogStyle);
    }

    public BottomSheetDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initDialog();
    }

    /**
     * 初始化Dialog
     */
    public void initDialog() {
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        win.setGravity(Gravity.RELATIVE_LAYOUT_DIRECTION | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);

        setCancelable(false);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(() -> {
            Window window = getWindow();
            if (window != null) {
                // 启动后设置为只有退出的动画，防止 Activity 再次resume 时，dialog 会再次执行进入的动画
                window.setWindowAnimations(R.style.AnimWindowExit);
            }
        }, 1000);
    }
}
