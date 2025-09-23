package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.Share;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.ShareRepository;
import com.example.EmotionSyncServer.service.UserPreferenceMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShareService {

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private UserPreferenceMatchService matchService;

    @Transactional
    public Share shareContent(User user1, User user2, String contentURL) {
        // 이미 공유된 컨텐츠인지 확인
        Optional<Share> existingShare = shareRepository.findByUser1AndUser2AndContentURL(user1, user2, contentURL);
        if (existingShare.isPresent()) {
            return existingShare.get();
        }

        Share share = new Share();
        share.setUser1(user1);
        share.setUser2(user2);
        share.setContentURL(contentURL);
        share.setLiked(false);
        share.setDisliked(false);
        return shareRepository.save(share);
    }

    public List<Share> getReceivedShares(User user) {
        return shareRepository.findByUser2(user);
    }

    public List<Share> getSentShares(User user) {
        return shareRepository.findByUser1(user);
    }

    @Transactional
    public Share likeShare(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("공유 정보를 찾을 수 없습니다."));
        
        // 이미 좋아요가 눌려있으면 취소
        if (share.isLiked()) {
            share.setLiked(false);
            Share saved = shareRepository.save(share);
            // 매칭률 조정 (좋아요 취소)
            matchService.updateMatchRate(share.getUser1(), share.getUser2(), false, false);
            return saved;
        }
        
        // 좋아요 설정
        share.setLiked(true);
        share.setDisliked(false);
        Share saved = shareRepository.save(share);
        // 매칭률 증가
        matchService.updateMatchRate(share.getUser1(), share.getUser2(), true, false);
        return saved;
    }

    @Transactional
    public Share dislikeShare(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("공유 정보를 찾을 수 없습니다."));
        
        // 이미 싫어요가 눌려있으면 취소
        if (share.isDisliked()) {
            share.setDisliked(false);
            Share saved = shareRepository.save(share);
            // 매칭률 조정 (싫어요 취소)
            matchService.updateMatchRate(share.getUser1(), share.getUser2(), false, false);
            return saved;
        }
        
        // 싫어요 설정
        share.setLiked(false);
        share.setDisliked(true);
        Share saved = shareRepository.save(share);
        // 매칭률 감소
        matchService.updateMatchRate(share.getUser1(), share.getUser2(), false, true);
        return saved;
    }

    @Transactional
    public Share cancelLikeShare(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("공유 정보를 찾을 수 없습니다."));
        share.setLiked(false);
        Share saved = shareRepository.save(share);
        // 매칭률 조정 (좋아요 취소)
        matchService.updateMatchRate(share.getUser1(), share.getUser2(), false, false);
        return saved;
    }

    @Transactional
    public Share cancelDislikeShare(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("공유 정보를 찾을 수 없습니다."));
        share.setDisliked(false);
        Share saved = shareRepository.save(share);
        // 매칭률 조정 (싫어요 취소)
        matchService.updateMatchRate(share.getUser1(), share.getUser2(), false, false);
        return saved;
    }

    public Share findByUsers(User user1, User user2, String contentURL) {
        System.out.println("🔍 공유 정보 검색:");
        System.out.println("  - 공유자: " + user1.getId());
        System.out.println("  - 수신자: " + user2.getId());
        System.out.println("  - 컨텐츠: " + contentURL);

        Optional<Share> existingShare = shareRepository.findByUser1AndUser2AndContentURL(user1, user2, contentURL);

        if (existingShare.isPresent()) {
            System.out.println("✅ 기존 공유 정보 발견");
            return existingShare.get();
        }

        System.out.println("⚠️ 공유 정보가 없어 새로 생성합니다.");
        // 공유 정보가 없으면 새로 생성
        Share share = new Share();
        share.setUser1(user1);
        share.setUser2(user2);
        share.setContentURL(contentURL);
        share.setLiked(false);
        share.setDisliked(false);

        return shareRepository.save(share);
    }

    public List<Share> getSharedContents(User user) {
        return shareRepository.findByUser2Id(user.getId());
    }

    public List<Share> getSharedContentsByUser(User user1, User user2) {
        return shareRepository.findByUser1IdAndUser2Id(user1.getId(), user2.getId());
    }

    public List<Share> getSharedContentsByUserAndContent(User user, String contentURL) {
        System.out.println("🔍 공유 정보 검색:");
        System.out.println("  - 수신자 ID: " + user.getId());
        System.out.println("  - 원본 컨텐츠 URL: " + contentURL);

        // URL 형식 변환
        String formattedURL = contentURL;
        if (!contentURL.startsWith("emotion-sync://")) {
            formattedURL = "emotion-sync://" + contentURL.replaceFirst("^/", "");
        }
        System.out.println("  - 변환된 컨텐츠 URL: " + formattedURL);

        // 모든 공유 정보 검색
        List<Share> shares = shareRepository.findByUser2AndContentURL(user, formattedURL);

        System.out.println("📊 검색 결과:");
        System.out.println("  - 찾은 공유 정보 수: " + shares.size());
        if (!shares.isEmpty()) {
            Share share = shares.get(0);
            System.out.println("  - 첫 번째 공유 정보:");
            System.out.println("    * ID: " + share.getId());
            System.out.println("    * 공유자: " + share.getUser1().getId());
            System.out.println("    * 수신자: " + share.getUser2().getId());
            System.out.println("    * 컨텐츠: " + share.getContentURL());
        }

        return shares;
    }

    public List<Share> getAllSharesByUser(User user) {
        System.out.println("🔍 사용자의 모든 공유 정보 검색:");
        System.out.println("  - 사용자 ID: " + user.getId());

        List<Share> shares = shareRepository.findAllByUser2Id(user.getId());

        System.out.println("📊 검색 결과:");
        System.out.println("  - 찾은 공유 정보 수: " + shares.size());
        for (Share share : shares) {
            System.out.println("  - 공유 정보:");
            System.out.println("    * ID: " + share.getId());
            System.out.println("    * 공유자: " + share.getUser1().getId());
            System.out.println("    * 수신자: " + share.getUser2().getId());
            System.out.println("    * 컨텐츠: " + share.getContentURL());
        }

        return shares;
    }
}