package com.example.EmotionSync.model;

// MatchRateDto.java
public class MatchRateDto {
    private int matchRate;
    private boolean currentLikeState;
    private boolean currentDislikeState;

    // Getter/Setter
    public int getMatchRate() { return matchRate; }
    public void setMatchRate(int matchRate) { this.matchRate = matchRate; }
    public boolean isCurrentLikeState() { return currentLikeState; }
    public void setCurrentLikeState(boolean currentLikeState) { this.currentLikeState = currentLikeState; }
    public boolean isCurrentDislikeState() { return currentDislikeState; }
    public void setCurrentDislikeState(boolean currentDislikeState) { this.currentDislikeState = currentDislikeState; }
}