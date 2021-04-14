package com.baoshiyun.demo.chat.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.baoshiyun.chat.R;
import com.baoshiyun.warrior.im.MessageInfo;
import com.baoshiyun.warrior.im.MessageInfoNotice;
import com.baoshiyun.warrior.live.im.protocol.NoticeMsg;
import com.flyco.roundview.RoundTextView;

import java.util.ArrayList;
import java.util.List;

public class MessageNoticeHolder extends MessageEmptyHolder {
    RoundTextView mContentView;
    private String icon = "icon ";
    Drawable drawable;

    public MessageNoticeHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.bsyl_view_message_holder_notice;
    }

    @Override
    public void initVariableViews() {
        mContentView = rootView.findViewById(R.id.chatContent);
        drawable = itemView.getContext().getResources().getDrawable(R.mipmap.bsyl_ic_input_danmu_icon);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
    }

    @Override
    public void layoutViews(MessageInfo msg, int position) {
        super.layoutViews(msg, position);
        // 使用缓存
        if (msg.drawCache instanceof SpannableStringBuilder) {
            mContentView.setText((SpannableStringBuilder) msg.drawCache);
            return;
        }

        // 消息数组
        List<NoticeMsg.NoticeMsgPart> noticeMsgParts = new ArrayList<>();
        if (msg instanceof MessageInfoNotice) {
            noticeMsgParts = ((MessageInfoNotice) msg).getNoticeParts();
        }

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(icon);
        // 拼装全部内容
        for (NoticeMsg.NoticeMsgPart noticeMsgPart : noticeMsgParts) {
            // 消息内容不为空
            if (!TextUtils.isEmpty(noticeMsgPart.getText())) {
                contentBuilder.append(noticeMsgPart.getText());
            }
        }
        SpannableStringBuilder contentSpan = new SpannableStringBuilder(contentBuilder.toString());
        // 绘制图标
        contentSpan.setSpan(
                new DanmuStyle(drawable),
                0,
                icon.length() - 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        int startIndex = icon.length();
        // 处理颜色
        for (NoticeMsg.NoticeMsgPart noticeMsgPart : noticeMsgParts) {
            String text = noticeMsgPart.getText();
            String colorStr = noticeMsgPart.getColor();
            // 消息内容不为空
            if (!TextUtils.isEmpty(text)) {
                int color;
                try {
                    color = Color.parseColor(colorStr);
                } catch (Exception ignored) {
                    color = Color.parseColor("#FF6017");
                }

                contentSpan.setSpan(
                        new ForegroundColorSpan(color),
                        startIndex,
                        startIndex + text.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // 移动开始角标
                startIndex += text.length();
            }
        }
        mContentView.setText(contentSpan);
        // 保存缓存
        msg.drawCache = contentSpan;
    }
}
