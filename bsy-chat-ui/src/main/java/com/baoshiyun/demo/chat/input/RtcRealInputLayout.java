package com.baoshiyun.demo.chat.input;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.baoshiyun.chat.R;
import com.baoshiyun.demo.chat.face.Emoji;
import com.baoshiyun.demo.chat.face.FaceFragment;
import com.baoshiyun.demo.chat.face.FaceManager;
import com.baoshiyun.demo.chat.face.ScreenUtil;
import com.baoshiyun.demo.chat.keyboard.KeyboardHeightObserver;
import com.baoshiyun.warrior.im.MessageInfo;
import com.baoshiyun.warrior.im.base.IMKitCallback;
import com.baoshiyun.warrior.im.utils.MessageInfoUtil;

public class RtcRealInputLayout extends FrameLayout implements TextWatcher, KeyboardHeightObserver {

    private Activity mActivity;
    private ImageView mRealFace;    //表情按钮
    private Button mSendBtn;        //发送按钮
    private EditText mRealContentEdit; //内容输入区域
    private FrameLayout mFaceLayout; //表情内容区域
    private RelativeLayout mInputContent;//输入整体内容区域

    private FaceFragment mFaceFragment;     //表情页面
    private FragmentManager mFragmentManager;

    private int mInputHeight;//键盘高度
    private int mFaceHeight; //表情高度

    private InputStates mStates = InputStates.TYPING;
    private MessageHandler mMessageHandler;
    // 空白区域
    private View mEmptySpaceView;

    public RtcRealInputLayout(@NonNull Context context) {
        this(context, null);
    }

    public RtcRealInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RtcRealInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initListener();

    }

    private void initListener() {
        mRealContentEdit.addTextChangedListener(this);
        mSendBtn.setOnClickListener(this::toSendMessage);
        mRealFace.setOnClickListener(this::toShowFace);
        mEmptySpaceView.setOnClickListener(this::onEmptyContentClick);
    }

    private void initView() {
        mActivity = (FragmentActivity) getContext();
        View.inflate(getContext(), R.layout.bsyl_view_input_layout, this);
        mRealContentEdit = findViewById(R.id.portraitRealContentEdit);
        mInputContent = findViewById(R.id.portraitRealInputLayout);
        mRealFace = findViewById(R.id.portraitRealFaceBtn);
        mSendBtn = findViewById(R.id.portraitSendBtn);
        mFaceLayout = findViewById(R.id.moreLayout);
        mFaceHeight = ScreenUtil.dip2px(getContext(), 220);

        mEmptySpaceView = findViewById(R.id.inputLayoutEmptySpacView);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        showFace();
    }

    //表情按钮点击 这里只负责键盘的吊起和收起
    private void toShowFace(View view) {
        if (mStates == InputStates.TYPING) {
            //收起键盘
            mStates = InputStates.SWITCH;
            mFaceLayout.setVisibility(VISIBLE);
            mRealFace.setImageResource(R.drawable.bsyl_ic_input_select);
            hideSoftInput();
        } else {
            mStates = InputStates.SWITCH;
            mFaceLayout.setVisibility(INVISIBLE);
            mRealFace.setImageResource(R.drawable.bsyl_ic_input_normal);
            showSoftInput();
        }
    }


    public void setMessageHandler(MessageHandler handler) {
        this.mMessageHandler = handler;
    }


    public InputStates getStates() {
        return mStates;
    }

    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRealContentEdit.getWindowToken(), 0);
        mRealContentEdit.clearFocus();
    }

    public void showSoftInput() {
        mRealContentEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mRealContentEdit, 0);
    }


    /**
     * 发送消息
     *
     * @param view
     */
    private void toSendMessage(View view) {
        String content = mRealContentEdit.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (mMessageHandler != null) {
            mMessageHandler.sendMessage(MessageInfoUtil.buildTextMessage(content), new IMKitCallback() {
                @Override
                public void onSuccess(Object data) {
                    mRealContentEdit.setText("");
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(mActivity, "发送失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void showFace() {
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getFragmentManager();

        }
        if (mFaceFragment == null) {
            mFaceFragment = FaceFragment.Instance();
        }
        mRealContentEdit.requestFocus();
        mFaceFragment.setListener(new FaceClick(mRealContentEdit, mMessageHandler));
        mFragmentManager.beginTransaction().replace(R.id.moreLayout, mFaceFragment).commitAllowingStateLoss();

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mSendBtn.setEnabled(s.toString().trim().length() > 0);
    }


    /**
     * 当键盘谈起来的时候
     *
     * @return 返回true 表示消费 即是由于自己切换模式时候引起的调用
     */
    public boolean theInputToShow(int height) {
        //判读当键盘弹起来的时候 是不是切换状态
        if (mStates == InputStates.SWITCH || mStates == InputStates.FACE) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mInputContent.getLayoutParams();
            params.setMargins(0, 0, 0, height);
            mInputContent.setLayoutParams(params);
            mStates = InputStates.TYPING;
            mRealFace.setImageResource(R.drawable.bsyl_ic_input_normal);
            return true;
        }

        return false;
    }


    /**
     * 当键盘缩起来的时候
     *
     * @return 返回true 表示消费 即是由于自己切换模式时候引起的调用
     */
    public boolean theInputToHide() {
        //当键盘收起来的时候判读是不是typing状态
        if (mStates == InputStates.SWITCH) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mInputContent.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            mInputContent.setLayoutParams(params);
            mStates = InputStates.FACE;
            mRealFace.setImageResource(R.drawable.bsyl_ic_input_select);
            return true;
        }

        return false;
    }

    //默认展示
    public void showDefaultInput() {
        mStates = InputStates.SWITCH;
        setVisibility(VISIBLE);
        showSoftInput();
    }

    public void recotryInputGone() {
        if (mFaceLayout != null) {
            mFaceLayout.setVisibility(INVISIBLE);
        }
        setVisibility(GONE);
    }

    /**
     * 键盘变化的监听
     *
     * @param height      The height of the keyboard in pixels
     * @param orientation The orientation either: Configuration.ORIENTATION_PORTRAIT or
     */
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 10) {
            //显示键盘
            mInputHeight = Math.abs(height);
            theInputToShow(mInputHeight - mFaceHeight);
        } else {
            theInputToHide();
            if (getStates() == InputStates.TYPING) {

                recotryInputGone();
            }
        }
    }

    /**
     * 空白区域点击
     */
    private void onEmptyContentClick(View view) {
        hideSoftInput();
        recotryInputGone();
    }

    /**
     * 两种互斥状态
     * 输入状态 - 上部分出现表情按钮 下部分出现键盘
     * 表情状态 - 上部分出现键盘按钮 下部分出现表情
     * 切换状态 - 表示切换中
     */
    public enum InputStates {
        TYPING, FACE, SWITCH
    }

    public static class FaceClick implements FaceFragment.OnEmojiClickListener {
        private EditText inputEdit;
        private MessageHandler messageHandler = null;

        public FaceClick(EditText inputEdit) {
            this.inputEdit = inputEdit;
        }

        public FaceClick(EditText inputEdit, MessageHandler messageHandler) {
            this.inputEdit = inputEdit;
            this.messageHandler = messageHandler;
        }

        @Override
        public void onEmojiDelete() {
            int index = inputEdit.getSelectionStart();
            Editable editable = inputEdit.getText();
            boolean isFace = false;
            if (index <= 0) {
                return;
            }
            if (editable.charAt(index - 1) == ']') {
                for (int i = index - 2; i >= 0; i--) {
                    if (editable.charAt(i) == '[') {
                        String faceChar = editable.subSequence(i, index).toString();
                        if (FaceManager.isFaceChar(faceChar)) {
                            editable.delete(i, index);
                            isFace = true;
                        }
                        break;
                    }
                }
            }
            if (!isFace) {
                editable.delete(index - 1, index);
            }
        }

        @Override
        public void onEmojiClick(Emoji emoji) {
            int index = inputEdit.getSelectionStart();
            Editable editable = inputEdit.getText();
            editable.insert(index, emoji.getFilter());
            FaceManager.handlerEmojiText(inputEdit,
                    editable.toString(),
                    true);
        }

        @Override
        public void onCustomFaceClick(int groupIndex, Emoji emoji) {
        }

    }


    public interface MessageHandler {
        void sendMessage(MessageInfo msg, IMKitCallback<MessageInfo> callback);
    }


    public interface IEmptyContentClick {
        void onEmptyContentClick();
    }


}
