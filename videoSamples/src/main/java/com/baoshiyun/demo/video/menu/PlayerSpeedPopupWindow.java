package com.baoshiyun.demo.video.menu;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baoshiyun.video.R;


/**
 * Created by liujunting on 2019-04-28.
 * 播放倍速popupWindow
 */
public class PlayerSpeedPopupWindow extends PlayerRightPopupWindow {
    private OnItemClickListener mItemClickListener;
    private MyAdapter mAdapter;
    private final String[] mSpeedArray = new String[]{"0.5", "0.8", "1.0", "1.25", "1.5", "2.0"};

    public PlayerSpeedPopupWindow(Context context) {
        super(context);
    }

    public void show(View parentView, String curSpeed, OnItemClickListener l) {
        this.mItemClickListener = l;
        mAdapter = new MyAdapter(curSpeed);
        setAdapter(mAdapter);
        show(parentView);
    }

    public void setSelectPos(String speed) {
        if (mAdapter != null && isShowing()) {
            mAdapter.setSelectSpeed(speed);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private String mCurSpeed;

        public MyAdapter(String curSpeed) {
            this.mCurSpeed = curSpeed;
        }

        public void setSelectPos(String curSpeed) {
            this.mCurSpeed = curSpeed;
            notifyDataSetChanged();
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
                    mItemClickListener.onItemClick(mSpeedArray[i]);
                }
            });

            myViewHolder.mTv.setText("x " + mSpeedArray[i]);

            if (mSpeedArray[i].equals(mCurSpeed)) {
                myViewHolder.mTv.setTextColor(Color.parseColor("#00CAFF"));
            } else {
                myViewHolder.mTv.setTextColor(Color.WHITE);
            }

        }

        @Override
        public int getItemCount() {
            return mSpeedArray.length;
        }

        public void setSelectSpeed(String speed) {
            this.mCurSpeed = speed;
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
        void onItemClick(String speed);
    }
}
