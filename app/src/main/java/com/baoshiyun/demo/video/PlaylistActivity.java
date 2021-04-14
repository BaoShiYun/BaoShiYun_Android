//package com.baoshiyun.trailblazer.video;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.baoshiyun.trailblazer.BSYFileUtils;
//import com.baoshiyun.trailblazer.R;
//import com.baoshiyun.trailblazer.video.download.DownloadActivity;
//import com.baoshiyun.warrior.video.BSYVideoSdk;
//import com.baoshiyun.warrior.video.Definition;
//import com.baoshiyun.videoui.bean.BSYPlaylist;
//import com.baoshiyun.videoui.bean.UIVideoInfo;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 播放列表
// * Created by ljt on 2020/11/02.
// */
//public class PlaylistActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(com.baoshiyun.trailblazer.R.layout.activity_m3u8_playlist);
//
//        RecyclerView mRecyclerView = findViewById(com.baoshiyun.trailblazer.R.id.recyclerView);
//        ArrayList<VideoItem> playList = new ArrayList<>();
//        playList.add(new VideoItem("播放mediaId", "media-843023072985088"));
//        playList.add(new VideoItem("播放url", "media-842706454937601"));
//        playList.add(new VideoItem("播放url", "media-842706459131904"));
//        playList.add(new VideoItem("播放url", "http://s43rxm7c.vod2.danghongyun.com/target/hls/2019/06/30/988_a9188e9df3024d988bd02ba06aa6ab1f_16_1280x720.m3u8"));
//        playList.add(new VideoItem("播放url", "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/gear4/prog_index.m3u8"));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setAdapter(new PlaylistAdapter(playList));
//
//
//        EditText mediaIdEditText = findViewById(com.baoshiyun.trailblazer.R.id.mediaid_edit_text);
//        findViewById(com.baoshiyun.trailblazer.R.id.play_btn).setOnClickListener(v -> {
//            String mediaId = mediaIdEditText.getText().toString().trim();
//            playVideo(this, mediaId);
//        });
//
//        // 下载自定义的视频
//        findViewById(com.baoshiyun.trailblazer.R.id.download_btn).setOnClickListener(v -> {
//            String mediaId = mediaIdEditText.getText().toString().trim();
//            if (!TextUtils.isEmpty(mediaId)) {
//                DownloadActivity.start(v.getContext(), mediaId);
//            }
//        });
//    }
//
//    /**
//     * 播放视频
//     *
//     * @param context
//     * @param mediaId
//     */
//    private void playVideo(Context context, String mediaId) {
//        // 设置播放信息
//        BSYVideoSdk.setAuthData("1665871018", "56492878", "bsy1000000000007.1608638882925.f1343c69231242ddad3680df18704efa");
//
//        File videoDownloadFile = BSYFileUtils.getVideoDownloadFile(context, mediaId);
//        boolean isLocal = videoDownloadFile.exists();
//
//        BSYPlaylist playlist;
//        if (isLocal) {
//            playlist = new BSYPlaylist.Builder(333, "离线数据")
//                    .addOfflineMediaInfo(mediaId, "离线数据:" + mediaId, "video-1234567890", Definition.LUD, videoDownloadFile.getAbsolutePath())
//                    .build();
//        } else {
//            List<UIVideoInfo> videos = null;
//            if (mediaId.startsWith("http")) { // 直接是在线的视频url
//                String playUrl = mediaId;
//                UIVideoInfo uiVideoInfo = new UIVideoInfo("video-1234567890", Definition.LUD, playUrl);
//                videos = new ArrayList<>();
//                videos.add(uiVideoInfo);
//
//                playlist = new BSYPlaylist.Builder(111, "在线数据")
//                        .addMediaInfo(mediaId, "在线数据3:" + mediaId, false, videos)
//                        .build();
//            } else {
//                playlist = new BSYPlaylist.Builder(222, "在线数据")
//                        .addOnlineMediaInfo(mediaId, "在线数据:" + mediaId)
//                        .build();
//            }
//
//        }
//        BSYPlayerActivity.startPlayer(this, playlist);
//    }
//
//    /**
//     * 播放列表适配器
//     */
//    class PlaylistAdapter extends RecyclerView.Adapter<PlayListItemViewHolder> {
//        private List<VideoItem> playlist;
//
//        public PlaylistAdapter(List<VideoItem> playlist) {
//            this.playlist = playlist;
//        }
//
//        @NonNull
//        @Override
//        public PlayListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = View.inflate(parent.getContext(), com.baoshiyun.trailblazer.R.layout.item_playlist, null);
//            return new PlayListItemViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull PlayListItemViewHolder holder, int position) {
//            holder.titleTv.setText(playlist.get(position).title);
//            holder.urlTv.setText(playlist.get(position).mediaId);
//            // item 点击
//            holder.itemView.setOnClickListener(v -> {
//                playVideo(v.getContext(), playlist.get(position).mediaId);
//            });
//            // 播放按钮点击
//            holder.playBtn.setOnClickListener(v -> {
//                playVideo(v.getContext(), playlist.get(position).mediaId);
//            });
//            // 下载按钮点击
//            holder.downloadBtn.setOnClickListener(v ->
//                    DownloadActivity.start(v.getContext(), playlist.get(position).mediaId));
//        }
//
//        @Override
//        public int getItemCount() {
//            return playlist.size();
//        }
//    }
//
//    class PlayListItemViewHolder extends RecyclerView.ViewHolder {
//
//        private final TextView titleTv;
//        private final TextView urlTv;
//        private final View playBtn;
//        private final View downloadBtn;
//
//        public PlayListItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            titleTv = itemView.findViewById(com.baoshiyun.trailblazer.R.id.item_playlist_title);
//            urlTv = itemView.findViewById(com.baoshiyun.trailblazer.R.id.item_playlist_url);
//            playBtn = itemView.findViewById(com.baoshiyun.trailblazer.R.id.item_play_btn);
//            downloadBtn = itemView.findViewById(R.id.item_download_btn);
//        }
//    }
//
//    class VideoItem {
//        private final String title;
//        private final String mediaId;
//
//        VideoItem(String title, String mediaId) {
//            this.title = title;
//            this.mediaId = mediaId;
//        }
//    }
//
//}