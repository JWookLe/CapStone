package com.example.EmotionSync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private Context context;
    private List<ContentItem> contentList;
    private static final String TAG = "ContentAdapter";

    public ContentAdapter(Context context, List<ContentItem> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    /**
     * 콘텐츠 목록을 업데이트하는 메소드
     * @param newContentList 새로운 콘텐츠 아이템 목록
     */
    public void updateContent(List<ContentItem> newContentList) {
        this.contentList = newContentList;
        notifyDataSetChanged();
        Log.d(TAG, "Content updated. Size: " + (newContentList != null ? newContentList.size() : 0));
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        ContentItem item = contentList.get(position);

        // 제목 설정
        holder.titleTextView.setText(item.getTitle());

        // 부제목 설정 (소스 정보 표시)
        String subtitle = item.getSource() != null ? item.getSource() : "";
        holder.subtitleTextView.setText(subtitle);

        // 이미지 로드
        if (item.hasImageUrl()) {
            Log.d(TAG, "Loading image from URL: " + item.getImageUrl());
            Glide.with(context)
                    .load(item.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // 클릭 이벤트 처리
        holder.cardContent.setOnClickListener(v -> {
            if (item.getLinkUrl() != null && !item.getLinkUrl().isEmpty()) {
                try {
                    // ContentDetailActivity로 이동
                    Intent intent = new Intent(context, ContentDetailActivity.class);
                    intent.putExtra("id", item.getId());
                    intent.putExtra("type", item.getType());
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("description", item.getOverview());
                    intent.putExtra("imageUrl", item.getImageUrl());
                    intent.putExtra("backdropUrl", item.getBackdropUrl());
                    intent.putExtra("contentUrl", item.getLinkUrl());

                    // YouTube 정보
                    if ("youtube".equals(item.getType())) {
                        intent.putExtra("channelName", item.getChannelName());
                        intent.putExtra("uploadDate", item.getUploadDate());
                        intent.putExtra("viewCount", item.getViewCount());
                        intent.putExtra("likeCount", item.getLikeCount());
                        intent.putExtra("commentCount", item.getCommentCount());
                        intent.putExtra("duration", item.getDuration());
                    }
                    // 영화 정보
                    else if ("movie".equals(item.getType())) {
                        intent.putExtra("releaseDate", item.getReleaseDate());
                        intent.putExtra("runtime", item.getRuntime());
                        intent.putExtra("rating", item.getRating());
                        intent.putStringArrayListExtra("genres", (java.util.ArrayList<String>) item.getGenres());
                        intent.putExtra("director", item.getDirector());
                        intent.putStringArrayListExtra("cast", (java.util.ArrayList<String>) item.getCast());
                    }

                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening content detail: " + e.getMessage(), e);
                    Toast.makeText(context, "컨텐츠를 열 수 없습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "컨텐츠 정보가 없습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList != null ? contentList.size() : 0;
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        CardView cardContent;
        ImageView imageView;
        TextView titleTextView;
        TextView subtitleTextView;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContent = itemView.findViewById(R.id.cardContent);
            imageView = itemView.findViewById(R.id.contentImage);
            titleTextView = itemView.findViewById(R.id.contentTitle);
            subtitleTextView = itemView.findViewById(R.id.contentSubtitle);
        }
    }
}
