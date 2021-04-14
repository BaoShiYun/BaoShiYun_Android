package com.baoshiyun.demo.chat;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MessageLayoutUI extends RecyclerView {
    public MessageLayoutUI(Context context) {
        super(context);
        init();
    }

    public MessageLayoutUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageLayoutUI(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutFrozen(false);
        setItemViewCacheSize(0);
        setHasFixedSize(true);
        setFocusableInTouchMode(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(Recycler recycler, State state) {
                /**
                 *     java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder{2eeb960 position=419 id=-1, oldPos=399, pLpos:399 scrap [attachedScrap] tmpDetached no parent} com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout{c69b398 VFED..... ......ID 0,0-1080,1226 #7f0900df app:id/chat_message_layout}, adapter:com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageListAdapter@d49f7f1, layout:androidx.recyclerview.widget.LinearLayoutManager@7702fd6, context:com.bokecc.dwlivemoduledemo.activity.HKYLiveActivity@7383136
                 *     at androidx.recyclerview.widget.RecyclerView$Recycler.validateViewHolderForOffsetPosition(RecyclerView.java:5715)
                 *     at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(RecyclerView.java:5898)
                 *     at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:5858)
                 *     at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:5854)
                 *     at androidx.recyclerview.widget.LinearLayoutManager$LayoutState.next(LinearLayoutManager.java:2230)
                 *     at androidx.recyclerview.widget.LinearLayoutManager.layoutChunk(LinearLayoutManager.java:1557)
                 *     at androidx.recyclerview.widget.LinearLayoutManager.fill(LinearLayoutManager.java:1517)
                 *     at androidx.recyclerview.widget.LinearLayoutManager.onLayoutChildren(LinearLayoutManager.java:612)
                 *     at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep1(RecyclerView.java:3875)
                 *     at androidx.recyclerview.widget.RecyclerView.dispatchLayout(RecyclerView.java:3639)
                 *     at androidx.recyclerview.widget.RecyclerView.consumePendingUpdateOperations(RecyclerView.java:1888)
                 *     at androidx.recyclerview.widget.RecyclerView$1.run(RecyclerView.java:407)
                 *     at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1086)
                 *     at android.view.Choreographer.doCallbacks(Choreographer.java:909)
                 *     at android.view.Choreographer.doFrame(Choreographer.java:832)
                 *     at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1071)
                 *     at android.os.Handler.handleCallback(Handler.java:883)
                 *     at android.os.Handler.dispatchMessage(Handler.java:100)
                 *     at android.os.Looper.loop(Looper.java:227)
                 *     at android.app.ActivityThread.main(ActivityThread.java:7799)
                 *     at java.lang.reflect.Method.invoke(Native Method)
                 *     at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
                 *     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:970)
                 */
                // 修复异常,可能是因为消息过多消息列表更新后没有及时通知adapter,导致异常发生,
                // 因 IM 代码逻辑复杂,故只处理异常,不进行定位修复。
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(linearLayoutManager);
    }
}
