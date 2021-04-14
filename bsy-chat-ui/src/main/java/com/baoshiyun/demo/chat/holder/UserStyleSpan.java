package com.baoshiyun.demo.chat.holder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baoshiyun.demo.chat.face.ScreenUtil;

/**
 * 这个是在用户消息的前面的标签
 */
public class UserStyleSpan extends ReplacementSpan {
    private Context context;
    private int bgColor;
    private int textColor;

    public UserStyleSpan(Context context, int bgColor, int textColor) {
        super();
        this.context = context;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int originalColor = paint.getColor();
        paint.setColor(this.bgColor);
        // 画圆角矩形背景
        canvas.drawRoundRect(
                new RectF(x,
                        top,
                        x + ((int) paint.measureText(text, start, end) + ScreenUtil.dip2px(context, 6)),
                        bottom),
                ScreenUtil.dip2px(context, 1),
                ScreenUtil.dip2px(context, 1),
                paint);
        paint.setColor(this.textColor);
        // 画文字,两边各增加3dp
        canvas.drawText(text, start, end, x + ScreenUtil.dip2px(context, 3), y - ScreenUtil.dip2px(context, 1F), paint);
        // 将paint复原
        paint.setColor(originalColor);
    }
}
