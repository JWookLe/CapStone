package com.example.EmotionSync;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private Context context;
    private List<ContentItem> musicList;

    public MusicAdapter(Context context, List<ContentItem> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        ContentItem music = musicList.get(position);
        holder.title.setText(music.getTitle());

        if (music.hasImageUrl()) {
            Glide.with(context)
                    .load(music.getImageUrl())
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            String link = music.getLinkUrl();
            if (link != null && !link.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        MusicViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.musicTitle);
            image = itemView.findViewById(R.id.musicImage);
        }
    }
}
