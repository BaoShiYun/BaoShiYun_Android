## 抱石云直播集成

### 1 直播间初始化
``` java
RoomEnterParams params = new RoomEnterParams.Builder()
    .setRoomParams("roomId","enterCode) // 直播间 id,直播间 enterCode
    .setTenantId("tenantId") // 抱石云租户id
    .setUserId("userId") // 用户id
    .setNickname("nickname")  // 用户昵称
    .setAvatarUrl("avatarUrl")) // 用户头像 url
    .build();
    
 // 抱石云教室初始化
 BSYRoomSdk mLiveRoom = BSYRoomSdk.createInstance(context, mEnterParams);
 // 添加教室回调
 mLiveRoom.addHandler(handler);
 // 进入教室
 mLiveRoom.enterRoom();
```

### 2 直播间功能方法

``` java
public abstract class BSYRoomSdk{
    /**
     * 实例化 instantiation
     *
     * @param context     上下文对象
     * @param enterParams 启动参数
     */
    public static BSYRoomSdk createInstance(Context context, RoomEnterParams enterParams) {}
    
    /**
     * 添加事件处理器
     *
     * @param eventHandler 事件处理器
     */
    public abstract void addHandler(BSYRoomEventHandler eventHandler);

    /**
     * 移除事件处理器
     *
     * @param eventHandler
     */
    public abstract void removeHandler(BSYRoomEventHandler eventHandler);

    /**
     * 进入直播间
     */
    public abstract void enterRoom();

    /**
     * 刷新直播间
     *
     * @param callback 回调
     */
    public abstract void refreshRoom(OnCallback callback);

    /**
     * 退出直播间
     */
    public abstract void exitRoom();

    /**
     * 设置视频渲染窗口
     *
     * @param streamId 视频流id
     */
    public abstract SurfaceView setupVideoRenders(String streamId);

    /**
     * 移除视频渲染窗口
     *
     * @param streamId 视频流id
     */
    public abstract void removeVideoRenders(String streamId);

    /**
     * 获取直播间线路
     *
     * @return 返回所有线路列表
     */
    public abstract List<LiveLineInfo> getLiveLines();

    /**
     * 切换直播间线路
     *
     * @param line 线路信息
     */
    public abstract void switchLine(LiveLineInfo line);

    /**
     * 获取清晰度
     *
     * @param streamId 当前视频流id
     * @return 当前视频流的所支持的清晰度
     */
    public abstract List<LiveVideoDefinition> getDefinitions(String streamId);

    /**
     * 获取当前清晰度
     *
     * @return 当前清晰度
     */
    public abstract LiveVideoDefinition getCurDefinition();

    /**
     * 切换清晰度
     *
     * @param streamId   视频流id
     * @param definition 需要设置的清晰度
     * @return 是否设置成功
     */
    public abstract boolean changDefinition(String streamId, LiveVideoDefinition definition);

    /**
     * 直播间的详情页信息
     *
     * @param callback 回调
     */
    public abstract void getDetailPageInfo(OnCallback<LiveDetailPageInfo> callback);

    /**
     * 直播间测验列表
     *
     * @param callback 回调
     */
    public abstract void getExamList(OnCallback<List<LiveExam>> callback);

    /**
     * 获取测验 token
     *
     * @param callback 回调
     */
    public abstract void getExamToken(OnCallback<String> callback);

    /**
     * 直播间活动列表
     *
     * @param callback 回调
     */
    public abstract void getActivityList(OnCallback<List<LiveActivity>> callback);

    /**
     * 直播间反馈
     *
     * @param content  反馈内容
     * @param callback 回调
     */
    public abstract void reportFeedback(String content, OnCallback<Object> callback);

    /**
     * 举手
     *
     * @param callback 回调
     */
    public abstract void raiseHand(OnCallback<Object> callback);

    /**
     * 取消举手
     *
     * @param callback 回调
     */
    public abstract void stopRaiseHand(OnCallback<Object> callback);

    /**
     * 停止互动(下台)
     *
     * @param callback 回调
     */
    public abstract void stopSpeak(OnCallback<Object> callback);

    /**
     * 接受互动（上台）
     *
     * @param accept   是否接收
     * @param callback 回调
     */
    public abstract void acceptSpeak(boolean accept, OnCallback<Object> callback);

    /**
     * 点赞
     */
    public abstract void thinkGood();

    /**
     * 发送 聊天消息
     *
     * @param msg      消息对象
     * @param retry    是否重试
     * @param callback 回调
     */
    public abstract void sendChatMessage(MessageInfo msg, boolean retry, IMKitCallback<MessageInfo> callback);

    /**
     * 获取历史消息
     * @param lastMessage 最后一条消息
     * @param callback    回调
     */
    public abstract void loadChatMessages(MessageInfo lastMessage, IMKitCallback<List<MessageInfo>> callback);

    /**
     * 切换前后摄像头
     */
    public abstract void switchCamera();

    /**
     * @return 是否在举手
     */
    public abstract boolean isRaiseHand();

    /**
     * @return 是否在互动中
     */
    public abstract boolean isSpeaking();

    /**
     * @return 是否允许互动
     */
    public abstract boolean isRaiseHandDisable();

    /**
     * @return 本地视频是否可用
     */
    public abstract boolean isLocalVideoDisable();

    /**
     * @return 本地音频是否可用
     */
    public abstract boolean isLocalAudioDisable();

    /**
     * 静默本地视频
     *
     * @param isMute 是否静默
     */
    public abstract void muteLocalVideo(boolean isMute);

    /**
     * 静默本地音频
     *
     * @param isMute 是否静默
     */
    public abstract void muteLocalAudio(boolean isMute);

    /**
     * 本地视频是否静默
     *
     * @return true-已经静默，false-未静默
     */
    public abstract boolean isMuteLocalVideo();

    /**
     * 本地音频是否静默
     *
     * @return true-已经静默，false-未静默
     */
    public abstract boolean isMuteLocalAudio();
}
```

##### 2.4.4 事件回调 BSYRoomEventHandler

直播间状态监听
```
    /**
     * 进入直播间成功
     *
     * @param roomId   直播间id
     * @param roomName 直播间名称
     */
    void onEnterRoomSuccess(String roomId, String roomName);

    /**
     * 进入教室失败
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    void onEnterRoomFail(int code, String msg);

    /**
     * 直播间的状态变更
     *
     * @param state {@link RoomState}
     */
    void onRoomStateChanged(int state);

    /**
     * 强制下线
     */
    void onForceOffline();

    /**
     * 点赞数量变更
     *
     * @param totalNum 总数
     * @param increase 距离上次的增加的个数
     */
    void onFavorNumChanged(int totalNum, int increase);

    /**
     * 直播间成员数量变更
     *
     * @param show   是否显示直播间成员数量
     * @param number 当前人数
     */
    void onRoomMemberNumChanged(boolean show, int number);
```

live 直播视频监听
```
 /**
     * live 直播视频状态发生变更
     *
     * @param streamId 视频流id
     * @param state    视频状态
     */
    void onLiveVideoStateChanged(String streamId, PlayState state);
```

直播间聊天消息和弹幕消息监听
```
    /**
     * 聊天被禁止或者被开启
     *
     * @param muted 是否禁止
     */
    void onMuteChat(boolean muted);
    /**
     * 直播间弹幕消息
     *
     * @param msgParts 一条消息的所有片段
     */
    void onRoomDanMuMsg(List<NoticeMsg.NoticeMsgPart> msgParts);

    /**
     * 直播间聊天消息
     *
     * @param messageInfo
     */
    void onRoomChatMsg(MessageInfo messageInfo);
```

活动监听
```
    /**
     * 收到口令红包活动
     *
     * @param activityUrl
     */
    void onReceiveActivity(String activityUrl);

    /**
     * 收到测验
     *
     * @param examUrl
     */
    void onReceiveExam(String examUrl);
    
    /**
     * 直播间货架变更
     *
     * @param shelf    货架信息
     * @param needShow 是否显示
     */
    void onRoomShelfChanged(ShelfMsg shelf, boolean needShow);
```

互动直播事件监听
```
 /**
     * 课件的状态发生变更
     *
     * @param show           是否显示课件
     * @param courseWareView 课件view show 为 false 不显示课件时，课件 view 为 null
     */
    void onCourseWareChanged(boolean show, ICourseWareView courseWareView);

    /**
     * 视频是否被禁用
     *
     * @param disable true-禁用，false-未禁用
     */
    void onLocalVideoDisable(boolean disable);

    /**
     * 音频是否被禁用
     *
     * @param disable true-禁用，false-未禁用
     */
    void onLocalAudioDisable(boolean disable);

    /**
     * 互动成员发生变更，此列表中不包含老师身份的成员，老师请通过 方法
     * {@link #onVideoTeacherStateChanged(String, int),#onAudioTeacherStateChanged(String, int)}
     * 监听视频和音频互动状态
     *
     * @param speakers 互动成员列表
     */
    void onSpeakerChanged(List<Speaker> speakers);

    /**
     * 被邀请讲话(上台)
     */
    void onInvitedSpeak();

    /**
     * 直播间互动开关状态变化
     *
     * @param disable true-禁用,false-未禁用
     */
    void onRaiseHandDisable(boolean disable);

    /**
     * 被移除举手队列
     *
     * @param inQueue 是否在队列
     * @param number  序号
     */
    void onRaiseHandStateChanged(boolean inQueue, int number);

    /**
     * 自己的互动状态变更
     *
     * @param speaking 是否在互动
     */
    void onSpeakStateChanged(boolean speaking);

    /**
     * 老师视频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link RemoteState}
     */
    void onVideoTeacherStateChanged(String uid, int state);

    /**
     * 老师音频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link RemoteState}
     */
    void onAudioTeacherStateChanged(String uid, int state);

    /**
     * 其他用户视频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link RemoteState}
     */
    void onVideoSubscribeStateChanged(String uid, int state);

    /**
     * 其他用户音频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link RemoteState}
     */
    void onAudioSubscribeStateChanged(String uid, int state);

    /**
     * 本地自己视频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link PublishState}
     */
    void onVideoPublishStateChanged(String uid, int state);

    /**
     * 本地自己音频状态变更通知
     *
     * @param uid   用户id
     * @param state {@link PublishState}
     */
    void onAudioPublishStateChanged(String uid, int state);

    /**
     * 用户音频音量监听
     *
     * @param uid    用户id
     * @param volume 音量值 [0-255]
     */
    void onAudioVolumeIndication(String uid, int volume);
```

常量定义
```
    /**
     * 直播间流的发布状态
     */
    interface PublishState {
        /**
         * 空闲状态
         */
        int PUB_STATE_IDLE = 0;             // 音视频发布状态 空闲
        /**
         * 关掉了摄像头或者麦克风 未发布状态
         */
        int PUB_STATE_NO_PUBLISHED = 1;     // 音视频发布状态 未发布
        /**
         * 准备发布的状态
         */
        int PUB_STATE_PUBLISHING = 2;       // 音视频发布状态 开始发布
        /**
         * 已经发布的状态
         */
        int PUB_STATE_PUBLISHED = 3;        // 音视频发布状态 已经发布
    }

    /**
     * 直播间远程用户的的状态
     */
    interface RemoteState {
        /**
         * 初始状态，播放停止, 可能是 mute 或 disable 视频
         */
        int REMOTE_STREAM_STATE_STOPPED = 0;    // 远端流状态。 初始状态，播放停止, 可能是 mute 或 disable 视频
        /**
         * 收到远端用户的第一个流数据包
         */
        int REMOTE_STREAM_STATE_STARTING = 1;   // 远端流状态。 收到远端用户的第一个流数据包
        /**
         * 本地正常解码播放
         */
        int REMOTE_STREAM_STATE_DECODING = 2;   // 远端流状态。 正常解码播放
        /**
         * 流卡顿
         */
        int REMOTE_STREAM_STATE_FROZEN = 3;     // 远端流状态。 流卡顿
        /**
         * 流播放失败
         */
        int REMOTE_STREAM_STATE_FAILED = 4;     // 远端流状态。 流播放失败
    }

    /**
     * 直播间状态
     */
    interface RoomState {
        /**
         * 未开始
         */
        int ROOM_STATE_READY = 0;   // 未开始
        /**
         * 已经开始
         */
        int ROOM_STATE_RUNNING = 1; // 已开始
        /**
         * 已经停止
         */
        int ROOM_STATE_STOPPED = 2; // 已停止
    }
```


