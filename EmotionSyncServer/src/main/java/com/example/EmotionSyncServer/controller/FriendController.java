package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.annotation.CurrentUser;
import com.example.EmotionSyncServer.dto.FriendRequestItem;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.dto.FriendRequestDto;
import com.example.EmotionSyncServer.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EmotionSyncServer.repository.UserRepository;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getFriendList(@CurrentUser User user) {
        List<FriendListItem> friendList = user.getFriends().stream()
                .map(friend -> new FriendListItem(friend.getId(), friend.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(friendList);
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestFriend(
            @RequestBody FriendRequestDto requestDto,
            @CurrentUser User user) {
        friendService.sendFriendRequest(requestDto.getInviteCode(), user);
        return ResponseEntity.ok("친구 요청을 전송했습니다.");
    }

    @PostMapping("/api/friend-request/{id}/accept")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long id, @CurrentUser User receiver) {
        friendService.acceptFriendRequest(id, receiver);
        return ResponseEntity.ok("친구 요청을 수락했습니다.");
    }

    @GetMapping("/api/friend-request/received")
    public ResponseEntity<List<FriendRequestItem>> getReceivedRequests(@CurrentUser User receiver) {
        List<FriendRequestItem> requests = friendService.getReceivedFriendRequests(receiver);
        return ResponseEntity.ok(requests);
    }

    @Getter
    public static class FriendListItem {
        private final String userId;
        private final String name;

        public FriendListItem(String userId, String name) {
            this.userId = userId;
            this.name = name;
        }
    }
}