package com.baoshiyun.demo.chat.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.baoshiyun.chat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来监听软键盘的弹起和收缩
 */
public class KeyboardHeightProvider extends PopupWindow {
    /**
     * The tag for logging purposes
     */
    private final static String TAG = "sample_KeyboardHeightProvider";
    private int statusBarHeight = -1;

    /**
     * The keyboard height observerList
     */
    private List<KeyboardHeightObserver> observerList;

    /**
     * The cached landscape height of the keyboard
     */
    private int keyboardLandscapeHeight;

    /**
     * The cached portrait height of the keyboard
     */
    private int keyboardPortraitHeight;

    /**
     * The view that is used to calculate the keyboard height
     */
    private View popupView;

    /**
     * The parent view
     */
    private View parentView;

    /**
     * The root activity that uses this KeyboardHeightProvider
     */
    private Activity activity;


    private Window mWindow;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutaListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (popupView != null) {
                handleOnGlobalLayout();
            }
        }
    };

    /**
     * Construct a new KeyboardHeightProvider
     *
     * @param activity The parent activity
     */
    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.activity = activity;
        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.bsyl_popup_window, null, false);
        setContentView(popupView);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        observerList = new ArrayList<>();
        parentView = activity.findViewById(android.R.id.content);
        setWidth(5);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutaListener);
    }


    public KeyboardHeightProvider(Window window) {
        super(window.getContext());
        mWindow = window;
        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.bsyl_popup_window, null, false);
        setContentView(popupView);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        observerList = new ArrayList<>();
        parentView = window.getDecorView().findViewById(android.R.id.content);
        setWidth(5);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutaListener);
    }


    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    public void start() {
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    public void close() {
        this.observerList = null;
        if (mOnGlobalLayoutaListener != null) {
            popupView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutaListener);
            mOnGlobalLayoutaListener = null;
        }
        dismiss();
    }

    /**
     * Set the keyboard height observerList to this provider. The
     * observerList will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observerList to be added to this provider.
     */
    public void addKeyboardHeightObserver(KeyboardHeightObserver observer) {
        if (observer == null) return;
        observerList.add(observer);
    }

    public void removeKeyboardHeightObserver(KeyboardHeightObserver observer) {
        observerList.remove(observer);
    }

    public void clearObserver() {
        observerList.clear();
    }

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private int getScreenOrientation() {
        if (activity == null) {
            return mWindow.getContext().getResources().getConfiguration().orientation;
        }
        return activity.getResources().getConfiguration().orientation;
    }

    public int getStatusBarHeight(Context context) {
        if (statusBarHeight != -1) {
            return statusBarHeight;
        }
        if (statusBarHeight <= 0) {
            int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resId);
            }
        }
        return statusBarHeight;
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private void handleOnGlobalLayout() {

        int height = parentView.getHeight();
        Rect rect = new Rect();
        popupView.getLocalVisibleRect(rect);

        int[] position = new int[]{0, 0};
        popupView.getLocationOnScreen(position);


        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        int orientation = getScreenOrientation();
        int keyboardHeight = height - rect.bottom - position[1];

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = height - rect.bottom;
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation);
        } else {
            this.keyboardLandscapeHeight = height - rect.bottom - position[1];
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation);
        }
    }

    private void notifyKeyboardHeightChanged(int height, int orientation) {
        if (observerList != null) {
            for (KeyboardHeightObserver observer : observerList)
                try {
                    observer.onKeyboardHeightChanged(height, orientation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
