package com.baoshiyun.demo.chat.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.chat.R;
import com.baoshiyun.demo.chat.MessageListAdapter;
import com.baoshiyun.warrior.im.MessageInfo;

public abstract class MessageBaseHolder extends RecyclerView.ViewHolder {

    public MessageListAdapter mAdapter;
    protected View rootView;

    public MessageBaseHolder(View itemView) {
        super(itemView);
        rootView = itemView;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = (MessageListAdapter) adapter;
    }

    public abstract void layoutViews(final MessageInfo msg, final int position);

    public static class Factory {

        public static RecyclerView.ViewHolder getInstance(ViewGroup parent, RecyclerView.Adapter adapter, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            RecyclerView.ViewHolder holder = null;
            View view = null;

            // 头部的holder
            if (viewType == MessageListAdapter.MSG_TYPE_HEADER_VIEW) {
                view = inflater.inflate(R.layout.bsyl_view_message_holder_header_loading, parent, false);
                holder = new MessageHeaderHolder(view);
                return holder;
            }

            view = inflater.inflate(R.layout.bsyl_view_message_adapter_common_container, parent, false);
            // 具体消息holder
            switch (viewType) {
                case MessageInfo.MSG_TYPE_TEXT:
                    holder = new MessageChatHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_IMAGE:
                    holder = new MessageImageHolder(view);
                    break;
                case MessageInfo.MSG_TYPE_NOTICE:
                    holder = new MessageNoticeHolder(view);
                    break;
            }
            if (holder != null) {
                ((MessageEmptyHolder) holder).setAdapter(adapter);
            }
            return holder;
        }
    }
}
