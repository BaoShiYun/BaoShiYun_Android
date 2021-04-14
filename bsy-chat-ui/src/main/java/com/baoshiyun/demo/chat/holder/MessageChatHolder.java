package com.baoshiyun.demo.chat.holder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Toast;

import com.baoshiyun.chat.R;
import com.baoshiyun.demo.chat.face.FaceManager;
import com.baoshiyun.warrior.im.MessageInfo;
import com.baoshiyun.warrior.im.RoleStyle;
import com.flyco.roundview.RoundTextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageChatHolder extends MessageEmptyHolder {
    private RoundTextView mContentView;

    public MessageChatHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.bsyl_view_message_holder_chat;
    }

    @Override
    public void initVariableViews() {
        mContentView = rootView.findViewById(R.id.chatContent);
        chatTimeText.setVisibility(View.GONE);
    }

    @Override
    public void layoutViews(MessageInfo msg, int position) {
        super.layoutViews(msg, position);

        rootView.findViewById(R.id.chatContent).setOnLongClickListener(v -> {
            copyMessageText(msg.getExtra().toString());
            return true;
        });

        // 使用缓存
        if (msg.drawCache instanceof SpannableStringBuilder) {
            mContentView.setText((SpannableStringBuilder) msg.drawCache);
            return;
        }

        // 消息组织形式 用户标签 用户名：消息内容

        RoleStyle roleStyle = msg.getRoleStyle();
        // 用户标签
        String roleTag = roleStyle.roleTag;

        // 用户昵称
        String nickname = msg.getGroupNameCard();
        if (TextUtils.isEmpty(nickname)) {
            nickname = msg.getFromUser();
        }
        nickname += "：";

        // 消息文本
        String msgText = msg.getExtra().toString();

        // 消息前缀显示内容
        String prefix = "";
        if (TextUtils.isEmpty(roleTag)) {
            prefix = nickname;
        } else {
            prefix = roleTag + "    " + nickname;
        }

        SpannableStringBuilder content = new SpannableStringBuilder(prefix + msgText);

        // 用户标签字号和颜色
        if (!TextUtils.isEmpty(roleTag)) {
            content.setSpan(new AbsoluteSizeSpan(13, true),
                    0,
                    roleTag.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.setSpan(
                    new UserStyleSpan(itemView.getContext(), Color.parseColor(roleStyle.bgColor), Color.parseColor(roleStyle.textColor)),
                    0,
                    roleTag.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 用户标签以后的字号
        content.setSpan(new AbsoluteSizeSpan(14, true),
                prefix.length() - nickname.length(),
                content.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 用户名称的颜色 start = 前缀的长度-昵称的长度
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#A6FFFFFF")),
                prefix.length() - nickname.length(),
                prefix.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // content 内容颜色 start = 前缀的长度
        content.setSpan(new ForegroundColorSpan(Color.parseColor("#CCCCCC")),
                prefix.length(),
                content.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 消息中的 表情符号 前缀以后的字符
        handlerEmoji(rootView.getContext(), content, prefix.length(), msg.getExtra().toString());
        mContentView.setText(content);
        // 缓存数据
        msg.drawCache = content;
    }

    /**
     * 复制消息
     *
     * @param msgText
     */
    private void copyMessageText(String msgText) {
        try {
            ClipboardManager clipboard = (ClipboardManager) rootView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                return;
            }
            ClipData clip = ClipData.newPlainText("text_message", msgText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(rootView.getContext(), "复制消息成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(rootView.getContext(), "文字太长，复制失败～", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlerEmoji(Context context, SpannableStringBuilder sb, int nameSize, String content) {
        String regex = "\\[(\\S+?)\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String emojiName = m.group();
            Bitmap bitmap = FaceManager.getEmoji(emojiName);
            if (bitmap != null) {
                sb.setSpan(new ImageSpan(context, bitmap),
                        nameSize + m.start(),
                        nameSize + m.end(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
    }


}
