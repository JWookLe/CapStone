package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preference_match")
public class UserPreferenceMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    @Column(nullable = false)
    private int matchRate = 0; // 0~100

    @Column(name = "is_liked")
    private boolean isLiked = false;

    @Column(name = "is_disliked")
    private boolean isDisliked = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }
    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }
    public int getMatchRate() { return matchRate; }
    public void setMatchRate(int matchRate) { this.matchRate = matchRate; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
    public boolean isDisliked() { return isDisliked; }
    public void setDisliked(boolean disliked) { isDisliked = disliked; }
} 