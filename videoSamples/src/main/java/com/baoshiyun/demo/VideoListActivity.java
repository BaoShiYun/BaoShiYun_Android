package com.baoshiyun.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.demo.download.DownloadManager;
import com.baoshiyun.demo.utils.BSYFileUtils;
import com.baoshiyun.demo.video.BSYPlayerActivity;
import com.baoshiyun.video.R;
import com.baoshiyun.warrior.video.Definition;
import com.baoshiyun.warrior.video.downloader.BSYVideoDownloader;
import com.baoshiyun.warrior.video.downloader.bean.VideoData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频列表
 */
public class VideoListActivity extends AppCompatActivity {
    public static final String BSY_TENANT_ID = "bsy_tenant_id";
    private RecyclerView mRecyclerView;
    private List<VideoItem> videoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoList.add(new VideoItem("第一个视频", "media-848899839229952"));
        videoList.add(new VideoItem("第一个视频", "media-847045725388800"));
        videoList.add(new VideoItem("第一个视频", "media-848899856007168"));
        videoList.add(new VideoItem("第一个视频", "media-848899874390016"));

        mRecyclerView.setAdapter(new Adapter());
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = View.inflate(parent.getContext(), R.layout.item_video_item, null);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VideoItem videoItem = videoList.get(position);
            holder.idTv.setText(videoItem.mediaId);
            holder.titleTv.setText(videoItem.title);
            holder.downloadStatusTv.setText("下载");

            holder.itemView.setOnClickListener(v -> {
                File videoDownloadFile = BSYFileUtils.getVideoDownloadFile(holder.titleTv.getContext(),
                        videoItem.mediaId);
                Intent intent = new Intent(v.getContext(), BSYPlayerActivity.class);
                intent.putExtra(BSYPlayerActivity.MEDIA_TITLE, videoItem.title);
                intent.putExtra(BSYPlayerActivity.MEDIA_ID, videoItem.mediaId);
                if (videoDownloadFile.exists()) {
                    intent.putExtra(BSYPlayerActivity.IS_LOCAL, true);
                    intent.putExtra(BSYPlayerActivity.LOCAL_FILE_PATH, videoDownloadFile.getAbsolutePath());
                }
                v.getContext().startActivity(intent);
            });

            BSYVideoDownloader task = DownloadManager.getTask(videoItem.mediaId);
            if (task != null) {
                task.setDownloadListener(new BSYVideoDownloader.DownloadListener() {
                    @Override
                    public void onProgress(int progress, long speed, long fileSize) {
                        holder.downloadProgressBar.setProgress(progress);
                    }

                    @Override
                    public void onStatusChanged(int status) {
                        holder.downloadStatusTv.setText(getStatusStr(status));
                    }

                    @Override
                    public void onFinished(String mediaId, String playLocalPath) {
                        DownloadManager.removeTask(mediaId);
                        holder.downloadStatusTv.setText("完成");
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        holder.downloadStatusTv.setText("出错");
                    }
                });
            } else {
                File videoDownloadFile = BSYFileUtils.getVideoDownloadFile(holder.titleTv.getContext(),
                        videoItem.mediaId);

                if (videoDownloadFile.exists()) {
                    holder.downloadStatusTv.setText("完成");
                    holder.downloadProgressBar.setProgress(100);
                } else {
                    holder.downloadStatusTv.setText("下载");
                }
            }

            holder.downloadStatusTv.setOnClickListener(v -> {
                if (task == null) {
                    File videoDownloadFile = BSYFileUtils.getVideoDownloadFile(v.getContext(), videoItem.mediaId);
                    if (videoDownloadFile.exists()) {
                        return;
                    }
                    VideoData videoData = new VideoData()
                            .setTenantId(AuthorizationManager.tenantId)
                            .setUserId(AuthorizationManager.userId)
                            .setMediaId(videoItem.mediaId)
                            .setOutputFile(videoDownloadFile)
                            .setDefinition(Definition.LHD);

                    BSYVideoDownloader downloader = new BSYVideoDownloader(AuthorizationManager.accessToken, videoData);
                    DownloadManager.putTask(videoItem.mediaId, downloader);
                    downloader.start();

                    notifyDataSetChanged();
                } else {
                    int status = task.getStatus();
                    if (status == BSYVideoDownloader.DOWNLOAD) {
                        task.pause();
                    } else if (status == BSYVideoDownloader.WAIT
                            || status == BSYVideoDownloader.PAUSE
                            || status == BSYVideoDownloader.ERROR) {
                        task.start();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return videoList.size();
        }
    }

    private String getStatusStr(int status) {
        String statusStr = null;
        switch (status) {
            case BSYVideoDownloader.WAIT:
                statusStr = "等待";
                break;
            case BSYVideoDownloader.DOWNLOAD:
                statusStr = "下载中";
                break;
            case BSYVideoDownloader.PAUSE:
                statusStr = "暂停";
                break;
            case BSYVideoDownloader.FINISH:
                statusStr = "完成";
                break;
            case BSYVideoDownloader.ERROR:
                statusStr = "出错";
                break;
        }
        return statusStr;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView downloadStatusTv;
        private final TextView titleTv;
        private final TextView idTv;
        private final ProgressBar downloadProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadStatusTv = itemView.findViewById(R.id.item_video_download_status);
            downloadProgressBar = itemView.findViewById(R.id.item_video_download_progress);
            titleTv = itemView.findViewById(R.id.item_video_title);
            idTv = itemView.findViewById(R.id.item_video_id);
        }
    }

    class VideoItem {
        private final String title;
        private final String mediaId;

        VideoItem(String title, String mediaId) {
            this.title = title;
            this.mediaId = mediaId;
        }
    }
}
