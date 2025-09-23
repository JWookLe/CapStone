package com.example.EmotionSync;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ContentItem {
    @SerializedName("type")
    private String type;
    @SerializedName("title")
    private String title;
    private String subtitle;
    private int colorResId;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("source")
    private String source;
    @SerializedName("link_url")
    private String linkUrl;
    
    // YouTube 관련 필드
    @SerializedName("channel_name")
    private String channelName;
    @SerializedName("upload_date")
    private String uploadDate;
    @SerializedName("view_count")
    private String viewCount;
    @SerializedName("like_count")
    private String likeCount;
    @SerializedName("comment_count")
    private String commentCount;
    @SerializedName("duration")
    private String duration;
    
    // TMDB 영화 관련 필드
    @SerializedName("overview")
    private String overview;
    @SerializedName("backdrop_url")
    private String backdropUrl;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("runtime")
    private String runtime;
    @SerializedName("rating")
    private String rating;
    @SerializedName("genres")
    private List<String> genres;
    @SerializedName("director")
    private String director;
    @SerializedName("cast")
    private List<String> cast;

    @SerializedName("id")
    private String id;

    // 새로운 생성자
    public ContentItem(String type, String title, String source, String imageUrl, String linkUrl) {
        this.type = type;
        this.title = title;
        this.source = source;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.colorResId = 0; // 기본값을 0으로 변경
    }

    // 기존 생성자도 수정
    public ContentItem(String title, String subtitle, int colorResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.colorResId = colorResId;
        this.imageUrl = null;
        this.type = null;
        this.source = null;
        this.linkUrl = null;
    }

    public ContentItem(String title, String imageUrl, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.colorResId = 0; // 기본값을 0으로 변경
        this.type = null;
        this.source = null;
        this.linkUrl = null;
    }

    // API 응답을 위한 새로운 생성자
    public ContentItem(String title, String subtitle, String imageUrl, String linkUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.colorResId = R.color.colorPrimary;
    }

    // Getter
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getColorResId() {
        return colorResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSource() {
        return source;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getViewCount() {
        return viewCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getDuration() {
        return duration;
    }

    public String getOverview() {
        return overview;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getRating() {
        return rating;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getDirector() {
        return director;
    }

    public List<String> getCast() {
        return cast;
    }

    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public String getId() {
        return id;
    }
}
