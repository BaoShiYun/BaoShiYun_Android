package com.baoshiyun.demo.download;

import com.baoshiyun.warrior.video.downloader.BSYVideoDownloader;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载管理
 */
public class DownloadManager {
    private static Map<String, BSYVideoDownloader> downloadTask = new HashMap<>();

    /**
     * 添加任务
     *
     * @param mediaId    媒体id
     * @param downloader 下载器
     */
    public static void putTask(String mediaId, BSYVideoDownloader downloader) {
        synchronized (downloadTask) {
            downloadTask.put(mediaId, downloader);

        }
    }

    /**
     * 移除任务
     *
     * @param mediaId
     */
    public static void removeTask(String mediaId) {
        synchronized (downloadTask) {
            BSYVideoDownloader task = downloadTask.remove(mediaId);
            if (task != null) {
                task.pause();
            }

        }
    }

    /**
     * 获取task
     *
     * @param mediaId
     * @return
     */
    public static BSYVideoDownloader getTask(String mediaId) {
        synchronized (downloadTask) {
            BSYVideoDownloader task = downloadTask.remove(mediaId);
            return task;
        }
    }
}
