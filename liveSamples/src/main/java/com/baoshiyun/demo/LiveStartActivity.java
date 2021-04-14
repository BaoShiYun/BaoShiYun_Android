package com.baoshiyun.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baoshiyun.live.R;
import com.baoshiyun.warrior.live.RoomEnterParams;

/**
 * 启动直播间页面
 * Created by ljt on 2021/3/18.
 */
public class LiveStartActivity extends AppCompatActivity {
    public static final String BSY_TENANT_ID = "bsy_tenant_id";

    private EditText mRoomIdET;
    private EditText mEnterCodeET;
    private RadioGroup mTypeRadioGroup;
    private EditText mUserIdET;
    private EditText mUserNicknameET;

    private SharedPreferences mSp;
    // 抱石云的租户id 在抱石云平台注册申请
    private String mBsyTenantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_start);
        mBsyTenantId = getIntent().getStringExtra(BSY_TENANT_ID);

        mRoomIdET = findViewById(R.id.roomIdEditText);
        mEnterCodeET = findViewById(R.id.enterCodeEditText);
        mUserIdET = findViewById(R.id.userIdEditText);
        mUserNicknameET = findViewById(R.id.userNicknameEditText);
        mTypeRadioGroup = findViewById(R.id.roomTypeRadioGroup);
        Button startBtn = findViewById(R.id.startLiveButton);

        startBtn.setOnClickListener(v -> startLive());

        mSp = getSharedPreferences("bsy-demo", Context.MODE_PRIVATE);
        // 恢复输入过的数据
        mRoomIdET.setText(mSp.getString("roomId", "live-846918715015168"));
        mEnterCodeET.setText(mSp.getString("enterCode", "b0b4acc8ca28440382777de9d95e7b58"));
        mUserIdET.setText(mSp.getString("userId", "10003042"));
        mUserNicknameET.setText(mSp.getString("nickname", "军亭哥"));
    }

    /**
     * 启动直播间
     */
    private void startLive() {
        String roomId = mRoomIdET.getText().toString();
        String enterCode = mEnterCodeET.getText().toString();
        String userId = mUserIdET.getText().toString();
        String nickname = mUserNicknameET.getText().toString();

        int checkedId = mTypeRadioGroup.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "请输入直播间id", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(enterCode)) {
            Toast.makeText(this, "请输入直播间口令", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "请输入用户id", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "请输入用户昵称", Toast.LENGTH_LONG).show();
            return;
        }
        // 记录输入过的数据
        SharedPreferences.Editor edit = mSp.edit();
        edit.putString("roomId", roomId);
        edit.putString("enterCode", enterCode);
        edit.putString("userId", userId);
        edit.putString("nickname", nickname);
        edit.commit();

        // 直播间的用户昵称(nickname)和头像(faceUrl)不是毕传参数，建议传入，否则聊天消息中无法获取用户昵称和头像
        RoomEnterParams params = new RoomEnterParams.Builder()
                .setRoomParams(roomId, enterCode)
                .setTenantId(mBsyTenantId)
                .setUserId(userId)
                .setNickname(nickname)
//                .setAvatarUrl("")
                .build();

        // 启动 直播间
        Intent intent = new Intent(this, BsyRoomActivity.class);
        intent.putExtra(BsyRoomActivity.INTERACT_ROOM, checkedId == R.id.roomTypeInteractRB);
        intent.putExtra(BsyRoomActivity.ENTER_PARAMS, params);
        startActivity(intent);
    }
}
