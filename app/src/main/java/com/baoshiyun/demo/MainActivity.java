package com.baoshiyun.demo;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by liujunting on 2020-10-16.
 */
public class MainActivity extends AppCompatActivity {
    String userId = "10010";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 启动播放列表
        findViewById(R.id.playlist).setOnClickListener(v -> {
            Intent intent = new Intent(this, VideoListActivity.class);
            intent.putExtra(VideoListActivity.BSY_TENANT_ID, getString(R.string.tenantId));
            intent.putExtra(VideoListActivity.BSY_USER_ID, userId);
            intent.putExtra(VideoListActivity.BSY_ACCESS_TOKEN, getString(R.string.accessstoken));
            MainActivity.this.startActivity(intent);
        });

        // 启动 Live
        findViewById(R.id.live).setOnClickListener(v -> {
            Intent intent = new Intent(this, LiveStartActivity.class);
            intent.putExtra(LiveStartActivity.BSY_TENANT_ID, getString(R.string.tenantId));
            MainActivity.this.startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
