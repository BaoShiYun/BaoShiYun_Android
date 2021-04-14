package com.baoshiyun.demo.chat.face;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.widget.EditText;
import android.widget.TextView;

import com.baoshiyun.chat.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FaceManager {

    static class HOLDER {
        static FaceManager manager = new FaceManager();
    }

    private static ArrayList<Emoji> emojiList = new ArrayList<>();
    private static LruCache<String, Bitmap> drawableCache = new LruCache(1024);

    public static ArrayList<Emoji> getEmojiList() {
        return emojiList;
    }

    public static ArrayList<FaceGroup> getCustomFaceList() {
        return new ArrayList<>();
    }

    private FaceManager() {
    }

    public static FaceManager getManager() {
        return HOLDER.manager;
    }

    public static void loadFaceFiles(Context context) {
        Context applicationContext = context.getApplicationContext();
        new Thread(() -> {
            String[] emojiFilters = applicationContext.getResources().getStringArray(R.array.BSYEmojiFilter);
            for (int i = 0; i < emojiFilters.length; i++) {
                loadAssetBitmap(applicationContext,
                        emojiFilters[i],
                        "emoji/" + emojiFilters[i] + "@2x.png",
                        true);
            }
        }).start();
    }

    private static Emoji loadAssetBitmap(Context context, String filter, String assetPath, boolean isEmoji) {
        InputStream is = null;
        try {
            int drawableWidth = ScreenUtil.getPxByDp(context, 32);
            Emoji emoji = new Emoji(drawableWidth);
            Resources resources = context.getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = DisplayMetrics.DENSITY_XXHIGH;
            options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
            options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
            context.getAssets().list("");
            is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, drawableWidth, drawableWidth), options);
            if (bitmap != null) {
                drawableCache.put(filter, bitmap);

                emoji.setIcon(bitmap);
                emoji.setFilter(filter);
                if (isEmoji) {
                    emojiList.add(emoji);
                }

            }
            return emoji;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static boolean isFaceChar(String faceChar) {
        return drawableCache.get(faceChar) != null;
    }

    public static void handlerEmojiText(TextView comment, String content, boolean typing) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "\\[(\\S+?)\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        boolean imageFound = false;
        while (m.find()) {
            String emojiName = m.group();
            Bitmap bitmap = drawableCache.get(emojiName);
            if (bitmap != null) {
                imageFound = true;
                sb.setSpan(new ImageSpan(comment.getContext(), bitmap),
                        m.start(), m.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        // 如果没有发现表情图片，并且当前是输入状态，不再重设输入框
        if (!imageFound && typing) {
            return;
        }
        int selection = comment.getSelectionStart();
        comment.setText(sb);
        if (comment instanceof EditText) {
            ((EditText) comment).setSelection(selection);
        }
    }

    public static Bitmap getEmoji(String name) {
        return drawableCache.get(name);
    }
}
