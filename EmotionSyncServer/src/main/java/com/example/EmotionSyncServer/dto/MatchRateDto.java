package com.example.EmotionSyncServer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MatchRateDto {
    @JsonProperty("matchRate")
    private int matchRate;
    
    @JsonProperty("currentLikeState")
    private boolean currentLikeState;
    
    @JsonProperty("currentDislikeState")
    private boolean currentDislikeState;
} 