package com.example.EmotionSyncServer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShareDto {
    private Long id;
    
    @JsonProperty("user1_id")
    private String user1Id;  // 공유한 사람 ID
    
    @JsonProperty("user2_id")
    private String user2Id;  // 공유받은 사람 ID
    
    @JsonProperty("contentURL")
    private String contentURL;
    
    @JsonProperty("is_liked")
    private boolean isLiked;    // 좋아요 상태
    
    @JsonProperty("is_disliked")
    private boolean isDisliked; // 싫어요 상태
} 