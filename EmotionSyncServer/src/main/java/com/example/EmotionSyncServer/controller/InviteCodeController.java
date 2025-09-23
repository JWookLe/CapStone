package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.annotation.CurrentUser;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.service.InviteCodeService;
import com.example.EmotionSyncServer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EmotionSyncServer.model.InviteCode;
import com.example.EmotionSyncServer.service.ChatRoomService;
import com.example.EmotionSyncServer.service.FriendService;
import com.example.EmotionSyncServer.repository.InviteCodeRepository;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;
    private final FriendService friendService;
    private final ChatRoomService chatRoomService;
    private final InviteCodeRepository inviteCodeRepository;
    private final NotificationService notificationService;
    @PostMapping("/invite-code")
    public ResponseEntity<String> createInviteCode(@CurrentUser User user) {
        String code = inviteCodeService.createInviteCode(user);
        return ResponseEntity.ok(code);
    }

    @PostMapping("/friend-request/by-code")
    public ResponseEntity<?> addFriendByInviteCode(@CurrentUser User sender,
                                                   @RequestParam String code) {
        try {
            InviteCode inviteCode = inviteCodeService.validateCode(code);
            User receiver = inviteCode.getUser();

            if (receiver.equals(sender)) {
                return ResponseEntity.badRequest().body("자기 자신을 친구로 추가할 수 없습니다.");
            }

            try {
                friendService.createFriendRelation(sender, receiver);
                notificationService.sendNotification(
                        receiver.getId(),
                        "FRIEND_ADDED",
                        sender.getName() + "님과 친구가 되었습니다.",
                        sender.getId()
                );
                notificationService.sendNotification(
                        sender.getId(),
                        "FRIEND_ADDED",
                        receiver.getName() + "님과 친구가 되었습니다.",
                        receiver.getId()
                );
                chatRoomService.createRoomIfNotExists(sender, receiver);

                inviteCode.markAsUsed();
                inviteCodeRepository.save(inviteCode);

                return ResponseEntity.ok("친구 추가 및 채팅방 생성 완료");
            } catch (Exception e) {
                // 친구 관계 생성 중 발생한 오류 로깅
                System.err.println("친구 관계 생성 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("친구 관계 생성 중 오류가 발생했습니다: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 오류 로깅
            System.err.println("친구 추가 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("친구 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public InviteCode validateCode(String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "유효하지 않은 코드입니다"));

        if (inviteCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "초대코드가 만료되었습니다");
        }

        if (inviteCode.isUsed()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이미 사용된 코드입니다");
        }

        return inviteCode;
    }
}
