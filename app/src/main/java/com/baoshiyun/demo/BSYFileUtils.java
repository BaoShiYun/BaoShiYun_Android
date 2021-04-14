package com.baoshiyun.demo;

import android.content.Context;

import com.baoshiyun.warrior.core.utils.MD5Utils;

import java.io.File;

/**
 * 文件操作相关
 * Created by ljt on 2020/11/2.
 */
public class BSYFileUtils {

    /**
     * 文件下载路径
     * 请每一个 m3u8 文件保持一个单独的文件夹，文件夹下会保存 m3u8 其他文件
     *
     * @param context
     * @param mediaId
     */
    public static File getVideoDownloadFile(Context context, String mediaId) {
        File videoDir = getVideoDownloadDir(context, mediaId);
        return new File(videoDir, getVideoFileName(mediaId) + ".m3u8");
    }

    /**
     * 获取视频文件保存的文件夹，一个 m3u8 所有的文件保存在一个以mediaId做md5的文件夹下
     *
     * @param context
     * @param mediaId
     * @return
     */
    public static File getVideoDownloadDir(Context context, String mediaId) {
        String downloadDir = context.getExternalFilesDir(null) + File.separator + "download" + File.separator + "m3u8";
        File dir = new File(downloadDir + File.separator + getVideoFileName(mediaId));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 获取 video 存储保存的 文件名，不包含后缀
     *
     * @param mediaId
     * @return
     */
    public static String getVideoFileName(String mediaId) {
        if (mediaId.startsWith("http")) {
            // 为了保证同一个文件可能因为鉴权的key不同导致，下载多份文件
            mediaId = getFileNameByUrl(mediaId);
        }
        String fileName = MD5Utils.getMD5(mediaId);
        return fileName;
    }

    /**
     * 获取 url 中的文件名称
     *
     * @param url
     * @return
     */
    private static String getFileNameByUrl(String url) {
        int endIndex = url.lastIndexOf("?");
        if (endIndex <= 0) {
            endIndex = url.length();
        }
        String fileName = url.substring(url.lastIndexOf("/"), endIndex);
        return fileName;
    }

    /**
     * 删除文件夹
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            String[] children = file.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteFile(new File(file, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

}
