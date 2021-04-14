package com.baoshiyun.demo.chat.holder;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoshiyun.chat.R;
import com.baoshiyun.warrior.im.MessageInfo;
import com.baoshiyun.warrior.im.MessageInfoImage;
import com.baoshiyun.warrior.im.RoleStyle;
import com.bumptech.glide.Glide;

public class MessageImageHolder extends MessageEmptyHolder {
    private static final int DEFAULT_RADIUS = 10;
    private static final int DEFAULT_MAX_SIZE = 440;

    private TextView mContentView;
    private ImageView mImageView;

    public MessageImageHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.bsyl_view_message_holder_image;
    }

    @Override
    public void initVariableViews() {
        mContentView = rootView.findViewById(R.id.nameText);
        mImageView = rootView.findViewById(R.id.imageView);
        chatTimeText.setVisibility(View.GONE);
    }

    @Override
    public void layoutViews(MessageInfo msg, int position) {
        super.layoutViews(msg, position);
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

        SpannableStringBuilder content = new SpannableStringBuilder(prefix);

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

        mContentView.setText(content);
        // 处理图片
        performHKYImage(msg, position);
    }


    //惠科云自定的图片消息
    private void performHKYImage(MessageInfo msg, int position) {
        MessageInfoImage imgMsg = null;
        if (msg instanceof MessageInfoImage) {
            imgMsg = (MessageInfoImage) msg;
        }
        if (imgMsg == null) {
            return;
        }
        mImageView.setLayoutParams(getImageParams(mImageView.getLayoutParams(), imgMsg));
        Glide.with(mImageView.getContext()).load(imgMsg.getThumbnailUrl()).into(mImageView);
        MessageInfoImage finalImgMsg = imgMsg;
//        mImageView.setOnClickListener(v ->
//                PreviewAlbumActivity.startPreview(mContentView.getContext(),
//                        Collections.singletonList(finalImgMsg.getUrl()),
//                        0,
//                        true));
    }


    private ViewGroup.LayoutParams getImageParams(ViewGroup.LayoutParams params, final MessageInfoImage msg) {
        if (msg.getWidth() == 0 || msg.getHeight() == 0) {
            return params;
        }
        if (msg.getWidth() > msg.getHeight()) {
            // 宽大于高 宽度设置到最大值
            params.width = DEFAULT_MAX_SIZE;
            params.height = (int) (DEFAULT_MAX_SIZE * ((float) msg.getHeight() / msg.getWidth()));
        } else {
            // 宽小于高 高度设置为最大值
            // 这里判断图片的宽度和文字的宽度
            params.width = (int) (DEFAULT_MAX_SIZE * (float) msg.getWidth() / msg.getHeight());
            params.height = DEFAULT_MAX_SIZE;
        }
        return params;
    }
}
