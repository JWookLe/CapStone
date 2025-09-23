package com.example.EmotionSync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.EmotionSync.R;
import com.example.EmotionSync.model.FriendItem;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {
    public static final int MODE_FRIEND = 0;
    public static final int MODE_MATCH_RATE = 1;
    private int mode = MODE_FRIEND;
    private Context context;
    private List<FriendItem> friends;
    private OnFriendClickListener friendClickListener;
    private OnShareClickListener shareClickListener;
    private OnMatchRateClickListener matchRateClickListener;

    public FriendListAdapter(Context context, List<FriendItem> friends, int mode) {
        this.context = context;
        this.friends = friends;
        this.mode = mode;
    }

    public interface OnFriendClickListener {
        void onFriendClick(FriendItem friend);
    }
    public interface OnShareClickListener {
        void onShareClick(FriendItem friend);
    }
    public interface OnMatchRateClickListener {
        void onMatchRateClick(FriendItem friend);
    }

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.friendClickListener = listener;
    }
    public void setOnShareClickListener(OnShareClickListener listener) {
        this.shareClickListener = listener;
    }
    public void setOnMatchRateClickListener(OnMatchRateClickListener listener) {
        this.matchRateClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friend = friends.get(position);
        holder.tvFriendName.setText(friend.getName());
        holder.tvUserId.setText(friend.getUserId());
        if (mode == MODE_FRIEND) {
            holder.btnChat.setVisibility(View.VISIBLE);
            holder.btnMatchRate.setVisibility(View.GONE);
        } else if (mode == MODE_MATCH_RATE) {
            holder.btnChat.setVisibility(View.GONE);
            holder.btnMatchRate.setVisibility(View.VISIBLE);
        }
        holder.btnChat.setOnClickListener(v -> {
            if (friendClickListener != null) friendClickListener.onFriendClick(friend);
        });
        holder.btnMatchRate.setOnClickListener(v -> {
            if (matchRateClickListener != null) matchRateClickListener.onMatchRateClick(friend);
        });
        // 전체 클릭 이벤트 제거(버튼만 동작)
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void updateFriends(List<FriendItem> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName, tvUserId;
        Button btnChat, btnMatchRate;
        FriendViewHolder(View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tv_name);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            btnChat = itemView.findViewById(R.id.btn_chat);
            btnMatchRate = itemView.findViewById(R.id.btnMatchRate);
        }
    }
} 