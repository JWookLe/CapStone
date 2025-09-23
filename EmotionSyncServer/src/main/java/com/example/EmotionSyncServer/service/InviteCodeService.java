package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.InviteCode;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.InviteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;

    public String createInviteCode(User user) {
        String code = UUID.randomUUID().toString().substring(0, 8); // 예: "a1b2c3d4"
        InviteCode inviteCode = new InviteCode(code, user);
        inviteCodeRepository.save(inviteCode);
        return code;
    }

    public InviteCode validateCode(String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대코드입니다."));

        if (inviteCode.isExpired()) {
            throw new IllegalStateException("초대코드가 만료되었습니다.");
        }

        if (inviteCode.isUsed()) {
            throw new IllegalStateException("이미 사용된 초대코드입니다.");
        }

        return inviteCode;
    }

}