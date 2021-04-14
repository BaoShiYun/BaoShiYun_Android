package com.baoshiyun.demo.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.chat.R;
import com.baoshiyun.warrior.im.MessageInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天显示view
 */
public class ChatLayout extends RelativeLayout implements IMessageAdapter {
    private MessageLayout mMessageLayout;
    private ImageView mUnReadMsg;
    private MessageListAdapter mAdapter;
    private boolean isScrollBottom = true; // 是否已经滚动到了底部
    private onLoadMessageListener mOnLoadMsgListener;
    private OnEmptySpaceClickListener mEmptySpaceClickListener;

    // 缓存消息和处理
    private ArrayList<MessageInfo> mCacheMessages = new ArrayList();
    private MyHandler mMsgHandler;

    public ChatLayout(Context context) {
        super(context);
        initViews();
    }

    public ChatLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ChatLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mMsgHandler = new MyHandler(this);

        inflate(getContext(), R.layout.bsyl_view_chat_layout, this);
        mMessageLayout = findViewById(R.id.chat_message_layout);
        mUnReadMsg = findViewById(R.id.chat_unread_msg_tips);

        init();
    }

    protected void init() {
        mUnReadMsg.setOnClickListener(v -> onScrollToEnd());
        mAdapter = new MessageListAdapter(mMessageLayout);
        mMessageLayout.setAdapter(mAdapter);

        addListener();
    }

    private void addListener() {
        mMessageLayout.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mMessageLayout.getLayoutManager();
                int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 显示第一个条目加载更多
                    if (firstPosition == 0 && ((lastPosition - firstPosition + 1) < mAdapter.getItemCount())) {
                        mAdapter.showLoading();
                        // 获取第一个消息，因为第一个条目是loading
                        loadMessages(mAdapter.getFirstMessage());
                    }
                }
                // 不需要关心滚动状态，因为状态变换不会太频繁，
                // 在教室消息过多刷屏的情况下，如果有滚动状态限制，不太容易滚动到底部
                if (lastPosition >= layoutManager.getItemCount() - 1) {
                    isScrollBottom = true;
                    mUnReadMsg.setVisibility(View.GONE);
                } else {
                    isScrollBottom = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        mMessageLayout.setEmptySpaceClickListener(() -> {
            if (mEmptySpaceClickListener != null) {
                mEmptySpaceClickListener.onClick();
            }
        });
        /**
         * 设置消息列表空白处点击处理
         */
        mMessageLayout.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) {
                        if (mEmptySpaceClickListener != null) {
                            mEmptySpaceClickListener.onClick();
                        }
                    } else if (child instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) child;
                        final int count = group.getChildCount();
                        float x = e.getRawX();
                        float y = e.getRawY();
                        View touchChild = null;
                        for (int i = count - 1; i >= 0; i--) {
                            final View innerChild = group.getChildAt(i);
                            int[] position = new int[2];
                            innerChild.getLocationOnScreen(position);
                            if (x >= position[0]
                                    && x <= position[0] + innerChild.getMeasuredWidth()
                                    && y >= position[1]
                                    && y <= position[1] + innerChild.getMeasuredHeight()) {
                                touchChild = innerChild;
                                break;
                            }
                        }
                        if (touchChild == null)
                            if (mEmptySpaceClickListener != null) {
                                mEmptySpaceClickListener.onClick();
                            }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    private void loadMessages(MessageInfo lastMessage) {
        if (mOnLoadMsgListener != null) {
            mOnLoadMsgListener.loadMessages(lastMessage);
        }
    }

    /**
     * 点击未读消息提示，滚动到最底部
     */
    protected void onScrollToEnd() {
        isScrollBottom = true;
        mMessageLayout.scrollToPosition(mAdapter.getItemCount() - 1);
        mUnReadMsg.setVisibility(GONE); // 容错
    }

    @Override
    public void addMessage(List<MessageInfo> messages, boolean front) {
        mAdapter.addMessage(messages, front);
        if (!front) {
            if (isScrollBottom) {
                mMessageLayout.scrollToPosition(mAdapter.getItemCount() - 1);
            } else {
                mUnReadMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 为了解决消息过多，所有消息先进入缓存池，然后再轮询上屏
     *
     * @param messages 消息
     */
    @Override
    public void addMessage(MessageInfo messages) {
        // 自己的消息直接上屏
        if (messages.isSelf()) {
            innerAddMessage(messages);
        } else {
            synchronized (mCacheMessages) {
                // 第一次为空，要把handler 跑起来
                if (mCacheMessages.isEmpty()) {
                    mMsgHandler.removeMessages(0);
                    mMsgHandler.sendEmptyMessageDelayed(0, 500);
                }
                mCacheMessages.add(messages);
            }
        }
    }

    /**
     * 从 window 上移除 停掉 轮询消息
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMsgHandler.removeMessages(0);
    }

    /**
     * 再次添加到 window 上，启动消息轮序
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMsgHandler.removeMessages(0);
        mMsgHandler.sendEmptyMessageDelayed(0, 500);

        // 重新添加到 window 上，自动滚动到底部
        mMsgHandler.postDelayed(() -> mMessageLayout.scrollToPosition(mAdapter.getItemCount() - 1), 500);
    }

    private void innerAddMessage(MessageInfo messages) {
        mAdapter.addMessage(messages);
        if (isScrollBottom || messages.isSelf()) {
            mMessageLayout.scrollToPosition(mAdapter.getItemCount() - 1);
        } else {
            mUnReadMsg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void deleteMessage(MessageInfo message) {
        mAdapter.deleteMessage(message);
    }

    @Override
    public void updateMessage(MessageInfo message) {
        mAdapter.updateMessage(message);
    }


    public void setOnLoadMessageListener(onLoadMessageListener l) {
        this.mOnLoadMsgListener = l;
    }

    public void setEmptySpaceClickListener(OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    /**
     * 未读消息提示显示在左侧
     */
    public void unreadViewShowLeft() {
        LayoutParams layoutParams = (LayoutParams) mUnReadMsg.getLayoutParams();
        layoutParams.removeRule(CENTER_HORIZONTAL);
        layoutParams.addRule(ALIGN_PARENT_LEFT);
        mUnReadMsg.setLayoutParams(layoutParams);
    }

    /**
     * 未读消息提示显示在中间
     */
    public void unreadViewShowCenter() {
        LayoutParams layoutParams = (LayoutParams) mUnReadMsg.getLayoutParams();
        layoutParams.removeRule(ALIGN_PARENT_LEFT);
        layoutParams.addRule(CENTER_HORIZONTAL);
        mUnReadMsg.setLayoutParams(layoutParams);
    }

    /**
     * 处理消息 间隔200毫秒上屏一条消息，如果缓存超过 500 则一次刷新上屏
     */
    private void handlerMessage() {
        synchronized (mCacheMessages) {
            if (!mCacheMessages.isEmpty()) {
                if (mCacheMessages.size() > 500) {
                    addMessage(mCacheMessages, false);
                    mCacheMessages.clear();
                } else {
                    MessageInfo messageInfo = mCacheMessages.remove(0);
                    innerAddMessage(messageInfo);
                }
                mMsgHandler.removeMessages(0);
                mMsgHandler.sendEmptyMessageDelayed(0, 200);
            }
        }
    }

    /**
     * 缓存消息处理器
     */
    class MyHandler extends Handler {
        WeakReference<ChatLayout> mChatLayout;

        public MyHandler(ChatLayout chatLayout) {
            this.mChatLayout = new WeakReference<>(chatLayout);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            ChatLayout chatLayout = mChatLayout.get();
            if (chatLayout != null) {
                chatLayout.handlerMessage();
            }
        }
    }

    public interface onLoadMessageListener {
        void loadMessages(MessageInfo lastMessage);
    }


    public interface OnEmptySpaceClickListener {
        void onClick();
    }
}
