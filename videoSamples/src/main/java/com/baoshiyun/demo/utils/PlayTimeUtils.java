package com.baoshiyun.demo.utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by liujunting on 2017/10/27.
 */

public class PlayTimeUtils {
    static StringBuilder sFormatBuilder = new StringBuilder();
    static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    /**
     * yyyy-MM-dd-HH:mm:ss
     */
    public static String PATTERN1 = "yyyy-MM-dd HH:mm:ss";

    /**
     * 转换时间为 时:分:秒
     *
     * @param timeMs 毫秒值
     * @return
     */
    public static String formatMs(int timeMs) {
        return formatMs(false, timeMs);
    }

    /**
     * 转换时间为 时:分:秒
     *
     * @param showHour 显示小时
     * @param timeMs   毫秒值
     * @return
     */
    public static String formatMs(boolean showHour, int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        sFormatBuilder.setLength(0);
        if (hours > 0 || showHour) {
            return sFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return sFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
