package com.baoshiyun.demo.video.menu;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.video.R;
import com.baoshiyun.warrior.video.Definition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujunting on 2019-04-28.
 * Description
 */
public class PlayerDefinitionPopupWindow extends PlayerRightPopupWindow {
    private OnItemClickListener mItemClickListener;
    private MyAdapter mAdapter;

    public PlayerDefinitionPopupWindow(Context context) {
        super(context);
    }

    public void show(View parentView, List<Definition> definitions, Definition curDCode,
                     OnItemClickListener l) {
        if (definitions == null) {
            return;
        }
        this.mItemClickListener = l;
        mAdapter = new MyAdapter(definitions, curDCode);
        setAdapter(mAdapter);
        show(parentView);
    }

    public void setSelectDCode(Definition curDCode) {
        if (mAdapter != null && isShowing()) {
            mAdapter.setSelectDCode(curDCode);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        List<Definition> definitions;
        private Definition mSelectDefinition;

        public MyAdapter(List<Definition> definitions, Definition curDCode) {
            this.definitions = new ArrayList<>();
            this.definitions.addAll(definitions);
            this.mSelectDefinition = curDCode;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = View.inflate(viewGroup.getContext(), R.layout.bsyv_p_item_right_menu,
                    null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

            myViewHolder.mTv.setOnClickListener((v) -> {
                if (mItemClickListener != null) {
                    Definition definition = definitions.get(i);
                    mItemClickListener.onItemClick(definition);
                }
            });
            myViewHolder.mTv.setText(definitions.get(i).getDefinitionName());

            if (mSelectDefinition == definitions.get(i)) {
                myViewHolder.mTv.setTextColor(Color.parseColor("#00CAFF"));
            } else {
                myViewHolder.mTv.setTextColor(Color.WHITE);
            }

        }

        @Override
        public int getItemCount() {
            return definitions.size();
        }

        public void setSelectDCode(Definition curDCode) {
            this.mSelectDefinition = curDCode;
            notifyDataSetChanged();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTv = itemView.findViewById(R.id.player_item_right_menu_tv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Definition definition);
    }

}
