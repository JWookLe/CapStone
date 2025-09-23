package com.example.EmotionSync;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context context;
    private List<FriendRequestItem> requestList;

    public FriendRequestAdapter(Context context, List<FriendRequestItem> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public FriendRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestAdapter.ViewHolder holder, int position) {
        // 안전한 방식으로 현재 위치를 가져옴
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        FriendRequestItem item = requestList.get(pos);
        holder.tvRequesterId.setText(item.getRequesterId());

        holder.btnAccept.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("jwt_token", null);

            if (token == null) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            apiService.acceptFriendRequest("Bearer " + token, item.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "친구 요청 수락 완료!", Toast.LENGTH_SHORT).show();
                        requestList.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, requestList.size());
                    } else {
                        Toast.makeText(context, "수락 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequesterId;
        Button btnAccept;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequesterId = itemView.findViewById(R.id.tv_requester_id);
            btnAccept = itemView.findViewById(R.id.btn_accept);
        }
    }
}