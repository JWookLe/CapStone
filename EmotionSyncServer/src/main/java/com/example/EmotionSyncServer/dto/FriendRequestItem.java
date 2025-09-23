package com.example.EmotionSyncServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FriendRequestItem {
    private Long id;
    private String requesterId;
}