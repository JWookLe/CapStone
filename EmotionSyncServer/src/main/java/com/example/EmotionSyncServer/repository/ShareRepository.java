package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.Share;
import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
    List<Share> findByUser1(User user1);
    List<Share> findByUser2(User user2);
    List<Share> findByUser1Id(String userId);
    List<Share> findByUser2Id(String userId);
    List<Share> findByUser1IdAndUser2Id(String user1Id, String user2Id);
    Optional<Share> findByUser1AndUser2AndContentURL(User user1, User user2, String contentURL);
    List<Share> findByUser1AndUser2(User user1, User user2);

    @Query("SELECT s FROM Share s WHERE s.user2 = :user2 AND (" +
            "LOWER(s.contentURL) = LOWER(:contentURL) OR " +
            "LOWER(s.contentURL) = LOWER(CONCAT('emotion-sync://', REPLACE(:contentURL, 'emotion-sync://', ''))) OR " +
            "LOWER(s.contentURL) LIKE LOWER(CONCAT('emotion-sync://', REPLACE(:contentURL, 'emotion-sync://', ''), '%')))")
    List<Share> findByUser2AndContentURL(@Param("user2") User user2, @Param("contentURL") String contentURL);

    @Query("SELECT s FROM Share s WHERE s.user2.id = :userId")
    List<Share> findAllByUser2Id(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Share s WHERE s.user1 = :user1 OR s.user2 = :user2")
    void deleteByUser1OrUser2(User user1, User user2);
}