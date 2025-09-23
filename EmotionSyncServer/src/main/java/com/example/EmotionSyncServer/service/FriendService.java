package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.dto.FriendRequestItem;
import com.example.EmotionSyncServer.model.*;
import com.example.EmotionSyncServer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final NotificationService notificationService;
    public void sendFriendRequest(String inviteCode, User requester) {
        User receiver = userRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("초대코드에 해당하는 사용자가 없습니다."));

        if (receiver.getId().equals(requester.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        if (friendRepository.existsByRequesterAndReceiver(requester, receiver)) {
            throw new IllegalArgumentException("이미 요청했거나 친구입니다.");
        }

        InviteCode codeEntity = inviteCodeRepository.findByCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("초대코드를 찾을 수 없습니다."));

        if (codeEntity.isUsed()) {
            throw new IllegalStateException("이미 사용된 초대코드입니다.");
        }

        codeEntity.markAsUsed();
        inviteCodeRepository.save(codeEntity);


        Friend friend = Friend.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendStatus.PENDING)
                .build();

        friendRepository.save(friend);
    }

    public void acceptFriendRequest(Long requestId, User receiver) {
        Friend friend = friendRepository.findByIdAndReceiver(requestId, receiver)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청이 존재하지 않거나 권한이 없습니다."));

        if (!friend.getReceiver().getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("해당 친구 요청을 수락할 권한이 없습니다.");
        }

        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new IllegalStateException("이미 수락된 요청입니다.");
        }

        friend.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(friend);

        // (선택) 양방향 관계 맺기
        createFriendRelation(friend.getRequester(), friend.getReceiver());

        // ✅ 친구 요청 보낸 사람에게 알림 전송
        notificationService.sendNotification(
                friend.getRequester().getId(),
                "FRIEND_ACCEPTED",
                receiver.getName() + "님이 친구 요청을 수락했습니다",
                receiver.getId()
        );
    }

    @Transactional
    public void createFriendRelation(User user1, User user2) {
        try {
            // 영속성 컨텍스트에서 엔티티를 다시 조회
            User managedUser1 = userRepository.findById(user1.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + user1.getId()));
            User managedUser2 = userRepository.findById(user2.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + user2.getId()));

            // 친구 요청 상태 확인
            boolean hasPendingRequest = friendRepository.existsByRequesterAndReceiverAndStatus(
                    managedUser1, managedUser2, FriendStatus.PENDING) ||
                    friendRepository.existsByRequesterAndReceiverAndStatus(
                            managedUser2, managedUser1, FriendStatus.PENDING);

            if (hasPendingRequest) {
                throw new IllegalStateException("이미 친구 요청이 존재합니다.");
            }

            // 이미 친구 관계가 있는지 확인
            boolean isAlreadyFriend = friendRepository.existsByRequesterAndReceiverAndStatus(
                    managedUser1, managedUser2, FriendStatus.ACCEPTED) ||
                    friendRepository.existsByRequesterAndReceiverAndStatus(
                            managedUser2, managedUser1, FriendStatus.ACCEPTED);

            if (isAlreadyFriend) {
                throw new IllegalStateException("이미 친구 관계가 존재합니다.");
            }

            // 양방향 친구 관계 설정
            managedUser1.addFriend(managedUser2);

            // Friend 엔티티 생성 및 저장
            Friend friend1 = Friend.builder()
                    .requester(managedUser1)
                    .receiver(managedUser2)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            Friend friend2 = Friend.builder()
                    .requester(managedUser2)
                    .receiver(managedUser1)
                    .status(FriendStatus.ACCEPTED)
                    .build();

            friendRepository.save(friend1);
            friendRepository.save(friend2);

            // 영속성 저장
            userRepository.save(managedUser1);
            userRepository.save(managedUser2);
        } catch (Exception e) {
            throw new RuntimeException("친구 관계 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public List<FriendRequestItem> getReceivedFriendRequests(User receiver) {
        List<Friend> pendingRequests = friendRepository.findByReceiverAndStatus(receiver, FriendStatus.PENDING);

        return pendingRequests.stream()
                .map(friend -> new FriendRequestItem(friend.getId(), friend.getRequester().getId()))
                .toList();
    }


}