package com.baoshiyun.demo;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baoshiyun.warrior.video.downloader.BSYVideoDownloader;

import java.util.Set;

//import com.baoshiyun.trailblazer.video.PlaylistActivity;

/**
 * Created by liujunting on 2020-10-16.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 启动播放列表
        findViewById(R.id.playlist).setOnClickListener(v -> {
//            Intent intent = new Intent(this, PlaylistActivity.class);
//            MainActivity.this.startActivity(intent);
        });

        // 启动 Live
        findViewById(R.id.live).setOnClickListener(v -> {
//            RoomEnterParams roomEnterParams = new RoomEnterParams("", "", "")
//                    .setNickname("llll");
//            Intent intent = new Intent(MainActivity.this, HKYLiveActivity2.class);
//            intent.putExtra(HKYLiveActivity2.BUNDLE_KEY_CONTENT_ID, 1232);
//            intent.putExtra(HKYLiveActivity2.BUNDLE_KEY_ENTER_PARAMS, roomEnterParams);
//            startActivity(intent);
        });

        findViewById(R.id.live).setOnClickListener(v -> {
            Intent intent = new Intent(this, LiveStartActivity.class);
            intent.putExtra(LiveStartActivity.BSY_TENANT_ID, getString(R.string.tenantId));
            MainActivity.this.startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 暂停所有下载任务
        if (BSYApplication.application.downloadTask != null) {
            Set<String> keySet = BSYApplication.application.downloadTask.keySet();
            for (String key : keySet) {
                BSYVideoDownloader downloader = BSYApplication.application.downloadTask.get(key);
                downloader.pause();
            }
        }
    }
}
