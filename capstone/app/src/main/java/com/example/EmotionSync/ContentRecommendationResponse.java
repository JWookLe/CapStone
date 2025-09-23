package com.example.EmotionSync;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 콘텐츠 추천 API 응답을 담는 모델 클래스
 */
public class ContentRecommendationResponse {
    @SerializedName("movies")
    private List<ContentItem> movies;

    @SerializedName("music")
    private List<ContentItem> music;

    @SerializedName("youtube")
    private List<ContentItem> youtube;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    public ContentRecommendationResponse() {
        // 기본 생성자 (Retrofit 요구사항)
    }

    public List<ContentItem> getMovies() {
        return movies;
    }

    public void setMovies(List<ContentItem> movies) {
        this.movies = movies;
    }

    public List<ContentItem> getMusic() {
        return music;
    }

    public void setMusic(List<ContentItem> music) {
        this.music = music;
    }

    public List<ContentItem> getYoutube() {
        return youtube;
    }

    public void setYoutube(List<ContentItem> youtube) {
        this.youtube = youtube;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
} 