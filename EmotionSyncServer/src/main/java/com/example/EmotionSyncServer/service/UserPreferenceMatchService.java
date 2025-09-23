package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.model.UserPreferenceMatch;
import com.example.EmotionSyncServer.repository.UserPreferenceMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceMatchService {
    @Autowired
    private UserPreferenceMatchRepository repository;

    private User[] sortUsers(User user1, User user2) {
        if (user1.getId().compareTo(user2.getId()) <= 0) {
            return new User[]{user1, user2};
        } else {
            return new User[]{user2, user1};
        }
    }

    @Transactional
    public int updateMatchRate(User user1, User user2, boolean isLike, boolean isDislike) {
        User[] sorted = sortUsers(user1, user2);
        UserPreferenceMatch match = repository.findByUsers(sorted[0], sorted[1])
                .orElseGet(() -> {
                    UserPreferenceMatch newMatch = new UserPreferenceMatch();
                    newMatch.setUser1(sorted[0]);
                    newMatch.setUser2(sorted[1]);
                    newMatch.setMatchRate(0);
                    return newMatch;
                });

        int rate = match.getMatchRate();
        
        // 현재 상태 확인
        boolean wasLiked = match.isLiked();
        boolean wasDisliked = match.isDisliked();
        
        // 토글 로직
        if (isLike) {
            if (wasLiked) {
                // 이미 좋아요 상태면 취소 (점수 변화 없음)
                match.setLiked(false);
            } else {
                // 좋아요 적용 (이전 싫어요 취소)
                if (wasDisliked) {
                    rate += 1; // 싫어요 취소
                }
                rate += 2; // 좋아요 적용
                match.setLiked(true);
                match.setDisliked(false);
            }
        } else if (isDislike) {
            if (wasDisliked) {
                // 이미 싫어요 상태면 취소 (점수 변화 없음)
                match.setDisliked(false);
            } else {
                // 싫어요 적용 (이전 좋아요 취소)
                if (wasLiked) {
                    rate -= 2; // 좋아요 취소
                }
                rate -= 1; // 싫어요 적용
                match.setDisliked(true);
                match.setLiked(false);
            }
        }
        
        if (rate < 0) rate = 0;
        if (rate > 100) rate = 100;
        match.setMatchRate(rate);
        repository.save(match);
        return rate;
    }

    public int getMatchRate(User user1, User user2) {
        User[] sorted = sortUsers(user1, user2);
        return repository.findByUsers(sorted[0], sorted[1])
                .map(UserPreferenceMatch::getMatchRate)
                .orElse(0);
    }

    public boolean getCurrentLikeState(User user1, User user2) {
        User[] sorted = sortUsers(user1, user2);
        return repository.findByUsers(sorted[0], sorted[1])
                .map(UserPreferenceMatch::isLiked)
                .orElse(false);
    }

    public boolean getCurrentDislikeState(User user1, User user2) {
        User[] sorted = sortUsers(user1, user2);
        return repository.findByUsers(sorted[0], sorted[1])
                .map(UserPreferenceMatch::isDisliked)
                .orElse(false);
    }
} 