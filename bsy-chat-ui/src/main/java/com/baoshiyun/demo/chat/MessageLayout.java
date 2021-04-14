package com.baoshiyun.demo.chat;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class MessageLayout extends MessageLayoutUI {
    protected OnEmptySpaceClickListener mEmptySpaceClickListener;
    public MessageLayout(Context context) {
        super(context);
    }

    public MessageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent e) {
//        if (e.getAction() == MotionEvent.ACTION_UP) {
//            View child = findChildViewUnder(e.getX(), e.getY());
//            if (child == null) {
//                if (mEmptySpaceClickListener != null)
//                    mEmptySpaceClickListener.onClick();
//            } else if (child instanceof ViewGroup) {
//                ViewGroup group = (ViewGroup) child;
//                final int count = group.getChildCount();
//                float x = e.getRawX();
//                float y = e.getRawY();
//                View touchChild = null;
//                for (int i = count - 1; i >= 0; i--) {
//                    final View innerChild = group.getChildAt(i);
//                    int[] position = new int[2];
//                    innerChild.getLocationOnScreen(position);
//                    if (x >= position[0]
//                            && x <= position[0] + innerChild.getMeasuredWidth()
//                            && y >= position[1]
//                            && y <= position[1] + innerChild.getMeasuredHeight()) {
//                        touchChild = innerChild;
//                        break;
//                    }
//                }
//                if (touchChild == null) {
//                    if (mEmptySpaceClickListener != null) {
//                        mEmptySpaceClickListener.onClick();
//                    }
//                }
//            }
//        }
//        return super.onInterceptTouchEvent(e);
//    }

    public void setEmptySpaceClickListener(OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    public interface OnEmptySpaceClickListener {
        void onClick();
    }

}
