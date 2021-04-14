package com.baoshiyun.demo.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.live.R;
import com.baoshiyun.warrior.live.BSYRoomSdk;
import com.baoshiyun.warrior.live.LiveLineInfo;
import com.baoshiyun.warrior.live.LiveVideoDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * live 直播 更多功能的弹框
 * Created by ljt on 2021/3/24.
 */
public class LiveMoreDialog extends BottomSheetDialog {
    private final BSYRoomSdk mBsyRoom;
    private final String mMainStreamId;
    private View mFunctionView;
    private RecyclerView mRecyclerView;
    private TextListAdapter mTextListAdapter;

    /**
     * @param context
     * @param bsyRoom      直播间实例
     * @param mainStreamId 直播间live 流id
     */
    public LiveMoreDialog(Context context, BSYRoomSdk bsyRoom, String mainStreamId) {
        super(context);
        this.mBsyRoom = bsyRoom;
        this.mMainStreamId = mainStreamId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_live_more);
        mFunctionView = findViewById(R.id.live_more_function_view);
        mRecyclerView = findViewById(R.id.live_more_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTextListAdapter = new TextListAdapter();
        mRecyclerView.setAdapter(mTextListAdapter);

        findViewById(R.id.live_more_switch_definition).setOnClickListener(v -> {
            mFunctionView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            String selectedText = null;
            List<LiveVideoDefinition> definitions = mBsyRoom.getDefinitions(mMainStreamId);
            ArrayList<String> texts = new ArrayList<>();
            for (LiveVideoDefinition def : definitions) {
                texts.add(def.getDefName());
                if (def.isCurrent()) {
                    selectedText = def.getDefName();
                }
            }
            mTextListAdapter.updateList(texts, selectedText);
            mTextListAdapter.setOnItemClickListener((position, text) -> {
                LiveVideoDefinition def = definitions.get(position);
                mBsyRoom.changDefinition(mMainStreamId, def);

                this.dismiss();
            });
        });
        findViewById(R.id.live_more_switch_line).setOnClickListener(v -> {

            mFunctionView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            String selectedText = null;
            List<LiveLineInfo> liveLines = mBsyRoom.getLiveLines();
            ArrayList<String> texts = new ArrayList<>();
            for (LiveLineInfo line : liveLines) {
                texts.add(line.getLineName());
                if (line.isCurrent()) {
                    selectedText = line.getLineName();
                }
            }
            mTextListAdapter.updateList(texts, selectedText);
            mTextListAdapter.setOnItemClickListener((position, text) -> {
                LiveLineInfo liveLineInfo = liveLines.get(position);
                mBsyRoom.switchLine(liveLineInfo);

                this.dismiss();
            });
        });
        findViewById(R.id.live_more_dialog_cancel).setOnClickListener(v -> {
            this.dismiss();
        });
    }

    class TextListAdapter extends RecyclerView.Adapter<ViewHolder> {
        List<String> texts = new ArrayList<>();
        private OnItemClickListener mItemClickListener;
        private String selectedText;

        private void updateList(List<String> texts, String selectedText) {
            this.texts.addAll(texts);
            this.selectedText = selectedText;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 20, 0, 20);
            textView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String s = texts.get(position);
            TextView itemView = (TextView) holder.itemView;
            itemView.setText(s);
            if (s.equals(selectedText)) {
                itemView.setTextColor(Color.parseColor("#3B9EFE"));
                holder.itemView.setOnClickListener(null);
            } else {
                holder.itemView.setOnClickListener(v -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(position, s);
                    }
                });
                itemView.setTextColor(Color.parseColor("#F5F5F5"));

            }
        }

        @Override
        public int getItemCount() {
            return texts.size();
        }

        public void setOnItemClickListener(OnItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    interface OnItemClickListener {
        void onItemClick(int position, String text);
    }

}
