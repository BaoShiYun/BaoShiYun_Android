package com.baoshiyun.demo.chat;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.demo.chat.holder.MessageBaseHolder;
import com.baoshiyun.demo.chat.holder.MessageHeaderHolder;
import com.baoshiyun.warrior.im.MessageInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * 消息 adapter
 */
public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int MAX_MESSAGE_SIZE = 2000; // 最大消息数量
    public static final int MSG_TYPE_HEADER_VIEW = -99;
    private final RecyclerView mRecyclerView;
    private final RecyclerView.ItemAnimator mMessageItemAnimator;
    private boolean mLoading = true;
    private List<MessageInfo> mDataSource = new ArrayList<>();
    // 所有消息id
    private HashSet<String> mMessageIds = new HashSet<>();

    // 最后添加消息的时间
    private long mLastAddMessageTime = 0;

    public MessageListAdapter(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        mMessageItemAnimator = mRecyclerView.getItemAnimator();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageBaseHolder.Factory.getInstance(parent, this, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final MessageInfo msg = getItem(position);
        MessageBaseHolder baseHolder = (MessageBaseHolder) holder;
        // 第一个条目是loading
        if (getItemViewType(position) == MSG_TYPE_HEADER_VIEW) {
            ((MessageHeaderHolder) baseHolder).setLoadingStatus(mLoading);
        }
        baseHolder.layoutViews(msg, position);
    }

    public void showLoading() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        notifyItemChanged(0);
    }

    public void dismissLoading() {
        mLoading = false;
        notifyItemChanged(0);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MSG_TYPE_HEADER_VIEW;
        }
        MessageInfo msg = getItem(position);
        return msg.getMsgType();
    }


    public MessageInfo getItem(int position) {
        if (position == 0 || mDataSource.size() == 0) {
            return null;
        }
        return mDataSource.get(position - 1);
    }

    /**
     * 批量添加消息
     *
     * @param messages 消息列表
     * @param front    插入到列表头部还是末尾
     */
    public void addMessage(List<MessageInfo> messages, boolean front) {
        // 清空动画
        mRecyclerView.setItemAnimator(null);
        // 移除已经存在的消息
        Iterator<MessageInfo> iterator = messages.iterator();
        while (iterator.hasNext()) {
            MessageInfo next = iterator.next();
            // 如果存在,则更新后移除
            if (mMessageIds.contains(next.getId())) {
                updateMessage(next);
                iterator.remove();
            } else {
                mMessageIds.add(next.getId());
            }
        }

        // 关掉loading
        dismissLoading();

        // 如果全部移除了，则不处理
        if (messages.isEmpty()) {
            return;
        }

        if (front) {
            mDataSource.addAll(0, messages);
            // 因为有 header 所以 notify position 要 +1 处理
            notifyItemRangeInserted(1, messages.size());
        } else {
            // 超出的数量
            int outOfRangeSize = mDataSource.size() + messages.size() - MAX_MESSAGE_SIZE;
            if (outOfRangeSize > 0) {
                int removeCount = outOfRangeSize;
                while (removeCount > 0) {
                    MessageInfo remove = mDataSource.remove(0);
                    if (remove != null) {
                        mMessageIds.remove(remove.getId());
                    }
                    removeCount--;
                }
                // 因为有 header 所以 notify position 要 +1 处理
                notifyItemRangeRemoved(1, outOfRangeSize);
            }
            mDataSource.addAll(messages);
            // 因为有 header 所以 notify position 要 +1 处理
            notifyItemRangeInserted(mDataSource.size() - messages.size() + 1, messages.size());
        }
    }

    /**
     * 向末尾添加一条消息
     *
     * @param messages 消息
     */
    public void addMessage(MessageInfo messages) {
        long l = System.currentTimeMillis();
        if (l - mLastAddMessageTime > 300) {
            mRecyclerView.setItemAnimator(mMessageItemAnimator);
        } else {
            mRecyclerView.setItemAnimator(null);
        }

        if (mDataSource.size() + 1 > MAX_MESSAGE_SIZE) {
            deleteMessage(0);
        }
        if (!mMessageIds.contains(messages.getId())) { // 不存在则添加
            mDataSource.add(messages);
            mMessageIds.add(messages.getId());
            // 因为有 header 所以 notify position 要 +1 处理
            notifyItemInserted(mDataSource.size() - 1 + 1);
        } else {
            updateMessage(messages);
        }

        // 更新最后添加消息的时间
        mLastAddMessageTime = l;
    }

    /**
     * 删除一条消息
     *
     * @param message 消息
     */
    public void deleteMessage(MessageInfo message) {
        int index = getMessageIndex(message.getId());
        if (index >= 0) {
            deleteMessage(index);
        }
    }

    /**
     * 删除消息
     *
     * @param index 角标
     */
    public void deleteMessage(int index) {
        if (mDataSource.size() - 1 > index) {
            MessageInfo remove = mDataSource.remove(index);
            if (remove != null) {
                mMessageIds.remove(remove.getId());
            }
            // 因为有 header 所以 notify position 要 +1 处理
            notifyItemRemoved(index + 1);
        }
    }

    /**
     * 更新一条消息
     *
     * @param message 消息
     */
    public void updateMessage(MessageInfo message) {
        int index = getMessageIndex(message.getId());
        if (index >= 0) {
            mDataSource.remove(index);
            mDataSource.add(index, message);
            // 因为有 header 所以 notify position 要 +1 处理
            notifyItemChanged(index + 1);
        }
    }

    /**
     * 通过 msg id 查找 index
     *
     * @param msgId 消息id
     * @return 消息列表中的索引
     */
    private int getMessageIndex(String msgId) {
        for (int i = 0; i < mDataSource.size(); i++) {
            if (mDataSource.get(i).getId().equals(msgId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 第一个消息
     *
     * @return 第一个消息
     */
    public MessageInfo getFirstMessage() {
        if (mDataSource.size() > 0) {
            return mDataSource.get(0);
        }
        return null;
    }
}