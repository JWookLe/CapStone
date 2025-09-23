package com.example.EmotionSync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private Context context;
    private List<ContentItem> items;

    public RecommendationAdapter(Context context, List<ContentItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContentItem item = items.get(position);
        holder.titleTextView.setText(item.getTitle());
        
        // 이미지 로딩
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .into(holder.imageView);
        }

        // 클릭 이벤트 설정
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContentDetailActivity.class);
            intent.putExtra("contentType", item.getType());
            intent.putExtra("contentId", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("contentUrl", item.getImageUrl());
            
            Log.d("RecommendationAdapter", "ContentDetailActivity로 이동 - Type: " + item.getType() + 
                ", ID: " + item.getId() + ", Title: " + item.getTitle());
            
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
} 