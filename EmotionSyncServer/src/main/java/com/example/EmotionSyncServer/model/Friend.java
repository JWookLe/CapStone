package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 요청 보낸 사람
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    // 친구 요청 받은 사람
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}