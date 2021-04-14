package com.baoshiyun.demo.video.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baoshiyun.demo.BSYApplication;
import com.baoshiyun.demo.BSYFileUtils;
import com.baoshiyun.demo.R;
import com.baoshiyun.warrior.core.thread.ThreadUtils;
import com.baoshiyun.warrior.core.utils.Logger;
import com.baoshiyun.warrior.video.Definition;
import com.baoshiyun.warrior.video.downloader.BSYVideoDownloader;
import com.baoshiyun.warrior.video.downloader.bean.VideoData;
import com.baoshiyun.warrior.video.downloader.utils.NetSpeedUtils;

import java.io.File;

/**
 * m3u8 下载测试页面
 * Created by ljt on 2020/10/26.
 */
public class DownloadActivity extends AppCompatActivity {
    private static final String TAG = "M3U8DownloadActivity";
    private ProgressBar mProgressBar;
    private BSYVideoDownloader downloader;
    private TextView mStatusView;
    private TextView mSpeedView;

    private String mMediaId;

    public static void start(Context context, String mediaId) {
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.putExtra("bsy-mediaId", mediaId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaId = getIntent().getStringExtra("bsy-mediaId");
        setContentView(R.layout.activity_m3u8_download);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mStatusView = (TextView) findViewById(R.id.status);
        mSpeedView = (TextView) findViewById(R.id.speed);

        findViewById(R.id.pause).setOnClickListener(view -> {
            if (downloader.isRunning()) {
                downloader.pause();
            }
        });
        findViewById(R.id.start).setOnClickListener(view -> {
            if (!downloader.isRunning() && !downloader.isFinished()) {
                downloader.updateAccessToken("accessToken");
                downloader.start();
            }
        });
        findViewById(R.id.delete).setOnClickListener(v -> deleteFile());

        createDownloadTask();
    }

    private void deleteFile() {
        downloader.pause();
        BSYApplication.application.downloadTask.remove(mMediaId);
        ThreadUtils.runOnSubThread(() -> {
            File videoDir = BSYFileUtils.getVideoDownloadDir(DownloadActivity.this, mMediaId);
            BSYFileUtils.deleteFile(videoDir);
            ThreadUtils.runOnUiThread(() -> Toast.makeText(this, "删除完成", Toast.LENGTH_LONG).show());
            ThreadUtils.runOnUiThread(() -> createDownloadTask());
        });
    }

    private void createDownloadTask() {
        // 查找系统中是否存在
        downloader = BSYApplication.application.downloadTask.get(mMediaId);
        if (downloader == null) {
            File videoDownloadFile = BSYFileUtils.getVideoDownloadFile(this, mMediaId);

            VideoData videoData = new VideoData()
                    .setTenantId("")
                    .setUserId("")
                    .setMediaId(mMediaId)
                    .setOutputFile(videoDownloadFile)
                    .setDefinition(Definition.LHD);

            downloader = new BSYVideoDownloader("accessToken", videoData);
            BSYApplication.application.downloadTask.put(mMediaId, downloader);
        }
        downloader.setDownloadListener(new BSYVideoDownloader.DownloadListener() {
            @Override
            public void onProgress(int progress, long speed, long fileSize) {
                String msg = " progress:" + progress + " speed:" + NetSpeedUtils.getInstance().displayFileSize(speed);
                mSpeedView.setText(msg);
                mProgressBar.setProgress(progress);
                Logger.d(TAG, msg);
            }

            @Override
            public void onStatusChanged(int status) {
                String statusStr = getStatusStr(status);
                String msg = "当前状态：" + statusStr;
                mStatusView.setText(msg);
                Logger.d(TAG, msg);
            }

            @Override
            public void onFinished(String mediaId, String playLocalPath) {
                String msg = "下载完成"
                        + "\n\nmediaId=" + mediaId
                        + "\n\nplayPath=" + playLocalPath;
                mStatusView.setText(msg);
                Logger.d(TAG, msg);
            }

            @Override
            public void onFailed(Throwable e) {
                String msg = "下载失败：" + e.getMessage();
                mStatusView.setText(msg);
                Logger.d(TAG, msg);
            }
        });
        mProgressBar.setProgress(downloader.getProgress());
        mStatusView.setText(getStatusStr(downloader.getStatus()));
    }

    private String getStatusStr(int status) {
        String statusStr = null;
        switch (status) {
            case BSYVideoDownloader.WAIT:
                statusStr = "WAIT";
                break;
            case BSYVideoDownloader.DOWNLOAD:
                statusStr = "DOWNLOAD";
                break;
            case BSYVideoDownloader.PAUSE:
                statusStr = "PAUSE";
                break;
            case BSYVideoDownloader.FINISH:
                statusStr = "FINISH";
                break;
            case BSYVideoDownloader.ERROR:
                statusStr = "ERROR";
                break;
        }
        return statusStr;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloader != null) {
            downloader.setDownloadListener(null);
        }
    }
}