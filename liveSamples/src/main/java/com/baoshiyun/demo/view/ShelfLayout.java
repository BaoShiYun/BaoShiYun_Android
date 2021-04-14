package com.baoshiyun.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baoshiyun.live.R;
import com.baoshiyun.warrior.live.im.protocol.ShelfMsg;
import com.bumptech.glide.Glide;

/**
 * 货架入口view
 */
public class ShelfLayout extends FrameLayout {
    public static final int SHELF_TEXT = 1;
    public static final int SHELF_PICTURE = 2;

    private ShelfMsg mShelf;
    private TextView mTextContent;
    private ImageView mImageContent;

    public ShelfLayout(@NonNull Context context) {
        this(context, null);
    }

    public ShelfLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShelfLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.bsyl_view_live_shelf, this);
        mTextContent = findViewById(R.id.live_shelf_text);
        mImageContent = findViewById(R.id.live_shelf_image);
    }

    public void setShelf(ShelfMsg shelf) {
        mShelf = shelf;
        if (mShelf != null) {
            mImageContent.setVisibility(View.GONE);
            mTextContent.setVisibility(View.GONE);
            // 货架入口展现方式：1-为文本，2-为图片-url
            int entrance_style = mShelf.getEntranceStyle();
            switch (entrance_style) {
                case SHELF_TEXT:
                    mTextContent.setVisibility(View.VISIBLE);
                    mTextContent.setText(mShelf.getEntranceContent());
                    break;
                case SHELF_PICTURE:
                    mImageContent.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).asBitmap().load(mShelf.getEntranceContent()).into(mImageContent);
                    break;
            }
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (mImageContent != null) {
            mImageContent.setOnClickListener(l);
        }
        if (mTextContent != null) {
            mTextContent.setOnClickListener(l);
        }
    }
}
