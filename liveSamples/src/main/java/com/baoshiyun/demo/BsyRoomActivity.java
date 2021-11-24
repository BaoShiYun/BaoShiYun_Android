package com.baoshiyun.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baoshiyun.demo.chat.ChatLayout;
import com.baoshiyun.demo.chat.face.FaceManager;
import com.baoshiyun.demo.chat.input.RtcRealInputLayout;
import com.baoshiyun.demo.chat.keyboard.KeyboardHeightProvider;
import com.baoshiyun.demo.dialog.BottomSheetDialog;
import com.baoshiyun.demo.dialog.InteractMoreDialog;
import com.baoshiyun.demo.dialog.LiveMoreDialog;
import com.baoshiyun.demo.view.GoodsButton;
import com.baoshiyun.demo.view.RoomVideosLayout;
import com.baoshiyun.demo.view.ShelfLayout;
import com.baoshiyun.live.R;
import com.baoshiyun.warrior.im.MessageInfo;
import com.baoshiyun.warrior.im.base.IMKitCallback;
import com.baoshiyun.warrior.live.BSYRoomBaseEventHandler;
import com.baoshiyun.warrior.live.BSYRoomSdk;
import com.baoshiyun.warrior.live.PlayState;
import com.baoshiyun.warrior.live.RoomEnterParams;
import com.baoshiyun.warrior.live.Speaker;
import com.baoshiyun.warrior.live.im.protocol.NoticeMsg;
import com.baoshiyun.warrior.live.im.protocol.ShelfMsg;
import com.baoshiyun.warrior.whiteboard.ICourseWareView;
import com.flyco.roundview.RoundTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * rtc 互动直播
 * Created by ljt on 2021/3/18.
 */
public class BsyRoomActivity extends AppCompatActivity {
    private static final String TAG = BsyRoomActivity.class.getSimpleName();
    public static final String ENTER_PARAMS = "enter_params";
    public static final String INTERACT_ROOM = "interact_room";

    private static final int REQUEST_CODE = 0x1001;
    // 键盘监听
    private KeyboardHeightProvider keyboardHeightProvider;
    private BSYRoomSdk mBsyRoomSdk;

    private FrameLayout mTopMainView;
    private RoomVideosLayout mSeatVideosLayout;
    private TextView mRoomTitleTv;
    private RoundTextView mRoomOnlineNumberTv;
    private GoodsButton mGoodsButton;
    private ShelfLayout mShelfView;
    private TextView mRoomStateView;
    private RtcRealInputLayout mInputLayout;
    private RoundTextView mInputBtn;
    private ChatLayout mChatMsgView;

    // 是否是互动直播间
    private boolean mInteractRoom;
    // live 直播 主视频流id
    private String mMainStreamId;
    private InteractMoreDialog mInteractMoreDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoomEnterParams enterParams = (RoomEnterParams) getIntent().getSerializableExtra(ENTER_PARAMS);
        mInteractRoom = getIntent().getBooleanExtra(INTERACT_ROOM, false);

        setContentView(R.layout.activity_bsy_room);

        initView();
        initKeyboardHeightProvider();

        // im 图标加载
        FaceManager.loadFaceFiles(this);

        mBsyRoomSdk = BSYRoomSdk.createInstance(this, enterParams);
        mBsyRoomSdk.addHandler(new EventListener());

        // 请求权限后进入直播间
        boolean b = checkPermissions(REQUEST_CODE);
        if (b) {
            mBsyRoomSdk.enterRoom();
        }
    }

    private void initView() {
        // 主课件窗口
        mTopMainView = findViewById(R.id.room_top_main_view);
        // 互动直播其他人视频
        mSeatVideosLayout = findViewById(R.id.room_seat_video_container);
        if (mInteractRoom) {
            mSeatVideosLayout.setVisibility(View.VISIBLE);
        } else {
            mSeatVideosLayout.setVisibility(View.GONE);
        }
        // 聊天相关
        mInputBtn = findViewById(R.id.room_input_btn);
        mInputLayout = findViewById(R.id.room_input_layout);
        mChatMsgView = findViewById(R.id.room_chat_msg_view);

        // 直播间状态view
        mRoomStateView = findViewById(R.id.room_state_view);

        // 退出按钮
        findViewById(R.id.room_back).setOnClickListener(v -> {
            this.finish();
        });
        // 更多功能
        findViewById(R.id.room_more_btn).setOnClickListener(v -> {
            showMoreFunctionDialog();
        });

        mRoomTitleTv = findViewById(R.id.room_title);
        mRoomOnlineNumberTv = findViewById(R.id.room_online_number);
        mGoodsButton = findViewById(R.id.room_goods_button);
        // 货架入口
        mShelfView = findViewById(R.id.room_shelf_view);
        // 点赞
        mGoodsButton.setOnClickListener(v -> mBsyRoomSdk.thinkGood());
        // 显示聊天输入框
        mInputBtn.setOnClickListener(v -> mInputLayout.showDefaultInput());
        // 发送消息
        mInputLayout.setMessageHandler((msg, callback) -> sendMessage(msg, false, callback));
        // 加载历史消息
        mChatMsgView.setOnLoadMessageListener(lastMessage -> {
            loadHistoryMessage(lastMessage, true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyboardHeightProvider.addKeyboardHeightObserver(mInputLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        keyboardHeightProvider.clearObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBsyRoomSdk.exitRoom();
        keyboardHeightProvider.close();
    }

    // region 键盘监听
    private void initKeyboardHeightProvider() {
        keyboardHeightProvider = new KeyboardHeightProvider(this);
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        keyboardHeightProvider.start();
                        rootView.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
    }
    // endregion

    // region 聊天消息相关

    /**
     * 加载历史消息
     *
     * @param lastMessage
     */
    private void loadHistoryMessage(MessageInfo lastMessage, boolean front) {
        mBsyRoomSdk.loadChatMessages(lastMessage, new IMKitCallback<List<MessageInfo>>() {
            @Override
            public void onSuccess(List<MessageInfo> messageInfos) {
                mChatMsgView.addMessage(messageInfos, front);
            }

            @Override
            public void onError(String s, int i, String s1) {
                // 失败后的处理,防止一直loading
                ArrayList<MessageInfo> messages = new ArrayList<>();
                mChatMsgView.addMessage(messages, front);
            }
        });

    }

    /**
     * 发送聊天消息
     *
     * @param message  消息
     * @param isRetry  是否重试
     * @param callback 回调
     */
    private void sendMessage(String message, boolean isRetry, IMKitCallback callback) {
        // 先添加的数据表，失败或者成功再更新

        MessageInfo messageInfo = mBsyRoomSdk.sendChatMessage(message,null, new IMKitCallback<MessageInfo>() {
            @Override
            public void onSuccess(MessageInfo data) {
                if (callback != null) {
                    callback.onSuccess(data);
                }
                mChatMsgView.updateMessage(data);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (callback != null) {
                    callback.onError(module, errCode, errMsg);
                }
            }
        });
        mChatMsgView.addMessage(messageInfo);

    }
    // endregion

    /**
     * 直播间事件监听
     */
    class EventListener extends BSYRoomBaseEventHandler {
        /**
         * 进入直播间成功
         *
         * @param roomId
         * @param roomName
         */
        @Override
        public void onEnterRoomSuccess(String roomId, String roomName) {
            super.onEnterRoomSuccess(roomId, roomName);
            mRoomTitleTv.setText(roomName);
            // 进入直播间成功加载历史消息
            loadHistoryMessage(null, false);
        }

        /**
         * 进入教室失败
         *
         * @param code 错误码
         * @param msg  错误信息
         */
        @Override
        public void onEnterRoomFail(int code, String msg) {
            super.onEnterRoomFail(code, msg);
            showEnterFailDialog(code, msg);
        }

        /**
         * 直播token 过期，需要重新进入直播间
         */
        @Override
        public void onTokenInvalid() {
            super.onTokenInvalid();
            showTokenInvalidDialog();
        }

        /**
         * 直播间的状态变更
         *
         * @param state {@link RoomState}
         */
        @Override
        public void onRoomStateChanged(int state) {
            super.onRoomStateChanged(state);
            mRoomStateView.setVisibility(View.VISIBLE);
            if (state == RoomState.ROOM_STATE_READY) {
                mRoomStateView.setText("直播未开始");
            } else if (state == RoomState.ROOM_STATE_RUNNING) {
                mRoomStateView.setVisibility(View.GONE);
            } else if (state == RoomState.ROOM_STATE_STOPPED) {
                mRoomStateView.setText("直播已停止");
            }
        }

        /**
         * 收到口令红包活动
         *
         * @param activityUrl
         */
        @Override
        public void onReceiveActivity(String activityUrl) {
            super.onReceiveActivity(activityUrl);
            showWebDialog(activityUrl);
        }

        /**
         * 收到测验
         *
         * @param examUrl
         */
        @Override
        public void onReceiveExam(String examUrl) {
            super.onReceiveExam(examUrl);
            showWebDialog(examUrl);
        }

        /**
         * 强制下线
         */
        @Override
        public void onForceOffline() {
            super.onForceOffline();
            showForceOfflineDialog();
        }

        /**
         * 聊天被禁止或者被开启
         *
         * @param muted 是否禁止
         */
        @Override
        public void onMuteChat(boolean muted) {
            super.onMuteChat(muted);
            if (muted) {
                mInputBtn.setText("当前全员禁言…");
            } else {
                mInputBtn.setText("快来说两句吧…");
            }
        }

        /**
         * 点赞数量变更
         *
         * @param totalNum 总数
         * @param increase 距离上次的增加的个数
         */
        @Override
        public void onFavorNumChanged(int totalNum, int increase) {
            super.onFavorNumChanged(totalNum, increase);
            if (increase == 0) {
                mGoodsButton.setTotalHearts(totalNum);
            } else if (increase == 1) {
                mGoodsButton.addHeart();
            } else {
                mGoodsButton.addHearts(increase, 5);
            }
        }

        /**
         * 直播间成员数量变更
         *
         * @param show   是否显示直播间成员数量
         * @param number 当前人数
         */
        @Override
        public void onRoomMemberNumChanged(boolean show, int number) {
            super.onRoomMemberNumChanged(show, number);
            if (show) {
                mRoomOnlineNumberTv.setVisibility(View.VISIBLE);
                mRoomOnlineNumberTv.setText("人数：" + number);
            }
        }

        /**
         * 直播间货架变更
         *
         * @param shelf    货架信息
         * @param needShow 是否显示
         */
        @Override
        public void onRoomShelfChanged(ShelfMsg shelf, boolean needShow) {
            super.onRoomShelfChanged(shelf, needShow);
            if (needShow) {
                mShelfView.setVisibility(View.VISIBLE);
                mShelfView.setShelf(shelf);
                mShelfView.setOnClickListener(v -> showWebDialog(shelf.getMobileActionUrl()));
            } else {
                mShelfView.setVisibility(View.GONE);
            }
        }

        /**
         * 直播间弹幕消息
         *
         * @param msgParts 一条消息的所有片段
         */
        @Override
        public void onRoomDanMuMsg(List<NoticeMsg.NoticeMsgPart> msgParts) {
            super.onRoomDanMuMsg(msgParts);
        }

        /**
         * 直播间聊天消息
         *
         * @param messageInfo
         */
        @Override
        public void onRoomChatMsg(MessageInfo messageInfo) {
            super.onRoomChatMsg(messageInfo);
            // 替换 \r 换行符号 为 \n
            String extra = messageInfo.getExtra().toString();
            extra = extra.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            messageInfo.setExtra(extra);
            mChatMsgView.addMessage(messageInfo);
        }

        /**
         * live 直播视频流渲染处理
         *
         * @param streamId 视频流id
         * @param state    状态
         */
        @Override
        public void onLiveVideoStateChanged(String streamId, PlayState state) {
            super.onLiveVideoStateChanged(streamId, state);
            switch (state) {
                case READYED:
                    SurfaceView surfaceView = mBsyRoomSdk.setupVideoRenders(streamId);
                    mTopMainView.removeAllViews();
                    mTopMainView.addView(surfaceView, new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER));
                    mMainStreamId = streamId;
                    break;
                case PREPAREING:
                case BUFFERING:
                    mRoomStateView.setVisibility(View.VISIBLE);
                    mRoomStateView.setText("请稍后~");
                    break;
                case PREPARED:
                case BUFFERING_END:
                    mRoomStateView.setVisibility(View.GONE);
                    break;
                case ERROR:
                    mRoomStateView.setVisibility(View.VISIBLE);
                    mRoomStateView.setText("直播开小差啦~");
                case FINISHED:
                    mBsyRoomSdk.removeVideoRenders(streamId);
                    mTopMainView.removeAllViews();
                    mMainStreamId = null;
                    break;
            }
        }

        // region 互动直播相关

        /**
         * 课件的状态发生变更
         *
         * @param show           是否显示课件
         * @param courseWareView 课件view show 为 false 不显示课件时，课件 view 为 null
         */
        @Override
        public void onCourseWareChanged(boolean show, ICourseWareView courseWareView) {
            super.onCourseWareChanged(show, courseWareView);
            mTopMainView.removeAllViews();
            if (show) {
                mTopMainView.addView(courseWareView.getView());
            }
        }

        /**
         * 视频是否被禁用
         *
         * @param disable true-禁用，false-未禁用
         */
        @Override
        public void onLocalVideoDisable(boolean disable) {
            super.onLocalVideoDisable(disable);
            // 设置视频是否禁用的状态
            if (mInteractMoreDialog != null && mInteractMoreDialog.isShowing()) {
                mInteractMoreDialog.setCameraState(disable, mBsyRoomSdk.isMuteLocalVideo());
            }
        }

        /**
         * 音频是否被禁用
         *
         * @param disable true-禁用，false-未禁用
         */
        @Override
        public void onLocalAudioDisable(boolean disable) {
            super.onLocalAudioDisable(disable);
            // 设置音频是否禁用的状态
            if (mInteractMoreDialog != null && mInteractMoreDialog.isShowing()) {
                mInteractMoreDialog.setMicState(disable, mBsyRoomSdk.isMuteLocalAudio());
            }
        }

        /**
         * 互动成员发生变更，此列表中不包含老师身份的成员，老师请通过 方法
         * {@link #onVideoTeacherStateChanged(String, int),#onAudioTeacherStateChanged(String, int)}
         * 监听视频和音频互动状态
         *
         * @param speakers 互动成员列表
         */
        @Override
        public void onSpeakerChanged(List<Speaker> speakers) {
            super.onSpeakerChanged(speakers);
            mSeatVideosLayout.speakerChanged(speakers);
        }

        /**
         * 被邀请讲话(上台)
         */
        @Override
        public void onInvitedSpeak() {
            super.onInvitedSpeak();
            showInvitedSpeakDialog();
        }

        /**
         * @param disable true-禁用,false-未禁用
         */
        @Override
        public void onRaiseHandDisable(boolean disable) {
            super.onRaiseHandDisable(disable);
            // 设置视频是否禁用的状态
            if (mInteractMoreDialog != null && mInteractMoreDialog.isShowing()) {
                mInteractMoreDialog.setRaiseHandDisable(disable);
            }
        }

        /**
         * 举手状态变更
         *
         * @param inQueue 是否在队列
         * @param number  序号
         */
        @Override
        public void onRaiseHandStateChanged(boolean inQueue, int number) {
            super.onRaiseHandStateChanged(inQueue, number);
            // 设置视频是否禁用的状态
            if (mInteractMoreDialog != null && mInteractMoreDialog.isShowing()) {
                mInteractMoreDialog.setRaiseHandDisable(mBsyRoomSdk.isRaiseHandDisable());
            }
        }

        /**
         * @param speaking 是否在互动
         */
        @Override
        public void onSpeakStateChanged(boolean speaking) {
            super.onSpeakStateChanged(speaking);
            // 设置视频是否禁用的状态
            if (mInteractMoreDialog != null && mInteractMoreDialog.isShowing()) {
                mInteractMoreDialog.setRaiseHandDisable(mBsyRoomSdk.isRaiseHandDisable());
            }
        }

        /**
         * 老师视频状态变更通知
         *
         * @param uid   用户id
         * @param state {@link RemoteState}
         */
        @Override
        public void onVideoTeacherStateChanged(String uid, int state) {
            super.onVideoTeacherStateChanged(uid, state);
            // 老师视频状态
            if (state == RemoteState.REMOTE_STREAM_STATE_STOPPED
                    || state == RemoteState.REMOTE_STREAM_STATE_FAILED) {
                mBsyRoomSdk.removeVideoRenders(uid);
                mSeatVideosLayout.removeTeacherRenderView();
            } else {
                SurfaceView surfaceView = mBsyRoomSdk.setupVideoRenders(uid);
                mSeatVideosLayout.setupTeacherRenderView(uid, surfaceView);
            }
        }

        /**
         * 老师音频状态变更通知
         *
         * @param uid   用户id
         * @param state {@link RemoteState}
         */
        @Override
        public void onAudioTeacherStateChanged(String uid, int state) {
            super.onAudioTeacherStateChanged(uid, state);
            // 老师的音频状态
            mSeatVideosLayout.onAudioStateChanged(uid,
                    !(state == RemoteState.REMOTE_STREAM_STATE_STOPPED
                            || state == RemoteState.REMOTE_STREAM_STATE_FAILED));
        }

        /**
         * 其他用户视频状态变更通知
         * int REMOTE_STREAM_STATE_STOPPED = 0;     流停止
         * int REMOTE_STREAM_STATE_STARTING = 1;    流开始
         * int REMOTE_STREAM_STATE_DECODING = 2;    流解码成功
         * int REMOTE_STREAM_STATE_FROZEN = 3;      流卡顿
         * int REMOTE_STREAM_STATE_FAILED = 4;      流失败
         *
         * @param uid   用户id
         * @param state {@link RemoteState}
         */
        @Override
        public void onVideoSubscribeStateChanged(String uid, int state) {
            super.onVideoSubscribeStateChanged(uid, state);
            // 学生视频状态
            if (state == RemoteState.REMOTE_STREAM_STATE_STOPPED
                    || state == RemoteState.REMOTE_STREAM_STATE_FAILED) {
                mBsyRoomSdk.removeVideoRenders(uid);
                mSeatVideosLayout.removeStudentRenderView(uid);
            } else {
                SurfaceView surfaceView = mBsyRoomSdk.setupVideoRenders(uid);
                mSeatVideosLayout.setupStudentRenderView(uid, surfaceView);
            }
        }

        /**
         * 其他用户音频状态变更通知
         *
         * @param uid   用户id
         * @param state {@link RemoteState}
         */
        @Override
        public void onAudioSubscribeStateChanged(String uid, int state) {
            super.onAudioSubscribeStateChanged(uid, state);
            // 其他用户的音频状态
            mSeatVideosLayout.onAudioStateChanged(uid,
                    !(state == RemoteState.REMOTE_STREAM_STATE_STOPPED
                            || state == RemoteState.REMOTE_STREAM_STATE_FAILED));
        }

        /**
         * 本地自己视频状态变更通知
         * <p>
         * int PUB_STATE_IDLE = 0;          空闲
         * int PUB_STATE_NO_PUBLISHED = 1;  未发布
         * int PUB_STATE_PUBLISHING = 2;    发布中
         * int PUB_STATE_PUBLISHED = 3;     已经发布
         *
         * @param uid   用户id
         * @param state {@link PublishState}
         */
        @Override
        public void onVideoPublishStateChanged(String uid, int state) {
            super.onVideoPublishStateChanged(uid, state);
            // 自己本地视频发布状态
            if (state < PublishState.PUB_STATE_PUBLISHING) {
                mBsyRoomSdk.removeVideoRenders(uid);
                mSeatVideosLayout.removeStudentRenderView(uid);
            } else {
                SurfaceView surfaceView = mBsyRoomSdk.setupVideoRenders(uid);
                mSeatVideosLayout.setupStudentRenderView(uid, surfaceView);
            }
        }

        /**
         * 本地自己音频状态变更通知
         *
         * @param uid   用户id
         * @param state {@link PublishState}
         */
        @Override
        public void onAudioPublishStateChanged(String uid, int state) {
            super.onAudioPublishStateChanged(uid, state);
            // 自己本地音频状态
            mSeatVideosLayout.onAudioStateChanged(uid, state >= PublishState.PUB_STATE_PUBLISHING);
        }

        /**
         * 用户音频音量监听
         *
         * @param uid    用户id
         * @param volume 音量值 [0-255]
         */
        @Override
        public void onAudioVolumeIndication(String uid, int volume) {
            super.onAudioVolumeIndication(uid, volume);
            mSeatVideosLayout.onAudioVolumeIndication(uid, volume);
        }
        // endregion
    }

    // region 直播间事件dialog

    /**
     * 显示邀请上台的弹框
     */
    private void showInvitedSpeakDialog() {
        new AlertDialog.Builder(this)
                .setMessage("老师向您发起了上台邀请，确定接受上台？（如果接受，请允许App调取本地摄像头和麦克风设备，保证正常连接）")
                .setCancelable(false)
                .setPositiveButton("接受", (dialog, which) -> {
                    acceptInvited(true);
                    dialog.dismiss();
                })
                .setNegativeButton("拒绝", (dialog, which) -> {
                    acceptInvited(false);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * 接受上台邀请
     *
     * @param accept 是否接受
     */
    private void acceptInvited(boolean accept) {
        mBsyRoomSdk.acceptSpeak(accept,
                new BSYRoomSdk.OnCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                    }

                    @Override
                    public void onFailed(Throwable throwable) {

                    }
                });
    }

    /**
     * 显示更多功能 dialog
     */
    private void showMoreFunctionDialog() {
        if (mInteractRoom) {
            mInteractMoreDialog = new InteractMoreDialog(this, mBsyRoomSdk);
            mInteractMoreDialog.show();
        } else {
            LiveMoreDialog dialog = new LiveMoreDialog(this, mBsyRoomSdk, mMainStreamId);
            dialog.show();
        }
    }


    /**
     * 显示进入直播间失败弹框
     *
     * @param code
     * @param msg
     */
    private void showEnterFailDialog(int code, String msg) {
        new AlertDialog.Builder(this)
                .setMessage("进入直播间失败 code:" + code + ", msg:" + msg)
                .setCancelable(false)
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    /**
     * 直播间 token 过期
     */
    private void showTokenInvalidDialog() {
        new AlertDialog.Builder(this)
                .setMessage("直播间token过期，请退出重新进入直播间")
                .setCancelable(false)
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    /**
     * 被踢下线弹框
     */
    private void showForceOfflineDialog() {
        new AlertDialog.Builder(this)
                .setMessage("账号已被踢下线")
                .setCancelable(false)
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    /**
     * 显示 webView dialog
     *
     * @param activityUrl
     */
    private void showWebDialog(String activityUrl) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        WebView webView = new WebView(this);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        dialog.setContentView(webView);
        dialog.show();
        webView.loadUrl(activityUrl);
    }

    // endregion

    // region 权限处理

    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isNotice = true;
        int allAllow = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < grantResults.length; i++) {
            allAllow += grantResults[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // 用户选择了"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    if (isNotice) {
                        Toast.makeText(this, "请手动打开该应用需要的权限", Toast.LENGTH_SHORT).show();
                        isNotice = false;
                    }
                }
            }
        }
        if (allAllow == PackageManager.PERMISSION_GRANTED) {
            mBsyRoomSdk.enterRoom();
        } else {
            this.finish();
        }
    }

    /**
     * 申请直播间需要的权限
     *
     * @param requestCode 申请 code
     * @return true-已经获取所有权限，false-部分权限未获取
     */
    private boolean checkPermissions(int requestCode) {
        return checkPermissions(requestCode,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    /**
     * 检查需要的权限
     *
     * @param requestCode 申请 code
     * @param permission  权限列表
     * @return true-已经获取所有权限，false-部分权限未获取
     */
    private boolean checkPermissions(int requestCode, String[] permission) {
        List<String> permissions = new ArrayList<String>();
        for (String per : permission) {
            int permissionCode = ActivityCompat.checkSelfPermission(this, per);
            if (permissionCode != PackageManager.PERMISSION_GRANTED) {
                permissions.add(per);
            }
        }
        if (!permissions.isEmpty()) {
            String[] needRequestPermissions = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, needRequestPermissions, requestCode);
            return false;
        } else {
            return true;
        }
    }

    // endregion
}
