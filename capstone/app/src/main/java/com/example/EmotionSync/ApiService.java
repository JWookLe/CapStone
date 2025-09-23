package com.example.EmotionSync;

import com.example.EmotionSync.model.ChatMessage;
import com.example.EmotionSync.model.FriendItem;
import com.example.EmotionSync.model.MatchRateDto;
import com.example.EmotionSync.model.Notification;
import com.example.EmotionSync.model.Share;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // 회원가입 요청을 위한 메서드 (서버에 사용자 정보를 보냄)

    @POST("/api/users/register")
    Call<Map<String, Object>> registerUser(@Body Map<String, String> userInfo);

    // - `@Body`: 요청 본문(body)에 `userInfo` 데이터를 포함하여 보냄
    // - `Call<Map<String, Object>>`: 서버의 응답을 Map 형태로 받음 (JSON을 자동 변환)
    // - `userInfo`: 사용자의 아이디, 비밀번호, 이름, 전화번호 등의 정보를 포함하는 Map<String, String> 객체
    @POST("/api/users/login")
    Call<Map<String, Object>> login(@Body Map<String, String> loginInfo);

    @GET("/api/users/verify-token")
    Call<Map<String, Object>> verifyToken(@Header("Authorization") String token);

    //구글 로그인
    @POST("/api/auth/google")
    Call<Map<String, Object>> socialLogin(@Body Map<String, String> data);

    // 카카오 로그인
    @POST("/api/auth/kakao")
    Call<Map<String, Object>> kakaoLogin(@Body Map<String, String> tokenData);


    @POST("/api/friend-request/by-code")
    Call<Void> sendFriendRequestByCode(
            @Header("Authorization") String token,
            @Query("code") String code
    );


    //친구초대코드
    @Headers("Accept: text/plain")
    @POST("/api/invite-code")
    Call<String> createInviteCode(@Header("Authorization") String token);
    // 인증번호 발송 요청 (아이디 찾기용)
    @POST("/api/users/find-id/send-verification")
    Call<Map<String, Object>> sendVerificationForFindId(@Body Map<String, String> userInfo);

    @GET("/api/friends")
    Call<List<FriendItem>> getFriendList(@Header("Authorization") String token);

    //친구요청aip
    @GET("/api/friend-request/received")
    Call<List<FriendRequestItem>> getReceivedFriendRequests(
            @Header("Authorization") String token
    );
    //친구요청수락 API
    @POST("/api/friend-request/{id}/accept")
    Call<Void> acceptFriendRequest(
            @Header("Authorization") String token,
            @Path("id") Long requestId
    );

    // 인증번호 확인 및 아이디 찾기
    @POST("/api/users/find-id/verify")
    Call<Map<String, Object>> verifyAndFindId(@Body Map<String, String> verificationInfo);

    // 인증번호 발송 요청 (비밀번호 찾기용)
    @POST("/api/users/find-password/send-verification")
    Call<Map<String, Object>> sendVerificationForFindPassword(@Body Map<String, String> userInfo);

    // 인증번호 확인 (비밀번호 찾기용)
    @POST("/api/users/find-password/verify")
    Call<Map<String, Object>> verifyForFindPassword(@Body Map<String, String> verificationInfo);

    // 기존 비밀번호와 동일한지 확인 (비밀번호 재사용 방지)
    @Headers("Content-Type: application/json")
    @POST("/api/users/check-password-same")
    Call<Map<String, Object>> checkPasswordSame(@Body Map<String, String> passwordData);

    // 비밀번호 재설정
    @POST("/api/users/reset-password")
    Call<Map<String, Object>> resetPassword(@Body Map<String, String> passwordInfo);

    // 감정 기록 저장
    @POST("/api/emotions")
    Call<Map<String, Object>> recordEmotion(
            @Header("Authorization") String token,
            @Body Map<String, String> emotionData
    );

    // 설문 기록 저장 추가
    @POST("/api/surveys/record")
    Call<Map<String, Object>> recordEmotionSurvey(
            @Header("Authorization") String token,
            @Body Map<String, Object> surveyData
    );

    @GET("api/recommendations")
    Call<List<ContentItem>> getContentRecommendations(
            @Header("Authorization") String token,
            @Query("emotion") String emotion,
            @Query("q1") int q1,
            @Query("q2") int q2,
            @Query("q3") int q3,
            @Query("q4") int q4,
            @Query("q5") String q5Text
    );

    // 비밀번호 확인 API
    @POST("/api/users/verify-password")
    Call<Map<String, Object>> verifyPassword(
            @Header("Authorization") String token,
            @Body Map<String, String> passwordData
    );

    // 비밀번호 변경 API
    @POST("/api/users/change-password")
    Call<Map<String, Object>> changePassword(
            @Header("Authorization") String token,
            @Body Map<String, String> passwordData
    );

    @GET("/api/emotion/recommend")
    Call<ContentRecommendationResponse> recommendContent(
            @Header("Authorization") String token,
            @Query("emotionCode") String emotionCode
    );

    @GET("/api/users/info")
    Call<Map<String, Object>> getUserInfo(
            @Header("Authorization") String token
    );

    @POST("/api/users/delete")
    Call<Map<String, Object>> deleteAccount(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );

    // 컨텐츠 상세 정보 조회
    @GET("/content/details")
    Call<Map<String, Object>> getContentDetails(
        @Header("Authorization") String token,
        @Query("type") String type,
        @Query("id") String id
    );

    // 친구 목록 조회
    @GET("api/friends")
    Call<List<FriendItem>> getFriends(
        @Header("Authorization") String token
    );

    // 컨텐츠 공유
    @POST("/api/shares/share")
    Call<Void> shareContent(
        @Header("Authorization") String token,
        @Body Share share
    );

    // 받은 공유 목록 조회
    @GET("/api/shares/received/{userId}")
    Call<List<Share>> getReceivedShares(
        @Header("Authorization") String token,
        @Path("userId") String userId
    );

    // 보낸 공유 목록 조회
    @GET("/api/shares/sent/{userId}")
    Call<List<Share>> getSentShares(
        @Header("Authorization") String token,
        @Path("userId") String userId
    );

    // 좋아요 처리
    @POST("/api/shares/{shareId}/like")
    Call<Void> likeShare(
        @Header("Authorization") String token,
        @Path("shareId") String shareId
    );

    // 싫어요 처리
    @POST("/api/shares/{shareId}/dislike")
    Call<Void> dislikeShare(
        @Header("Authorization") String token,
        @Path("shareId") String shareId
    );

    @GET("/api/shares")
    Call<Share> getSharesBetween(
        @Header("Authorization") String token,
        @Query("sharedBy") String sharedBy,
        @Query("sharedTo") String sharedTo,
        @Query("contentURL") String contentUrl
    );

    @POST("/api/chat/messages")
    Call<Void> sendChatMessage(@Header("Authorization") String token, @Body ChatMessage message);

    // 취향매칭률 조회
    @GET("/api/shares/match-rate")
    Call<MatchRateDto> getMatchRate(
        @Header("Authorization") String token,
        @Query("user1Id") String user1Id,
        @Query("user2Id") String user2Id
    );

    @GET("/api/notifications")
    Call<List<Notification>> getNotifications(@Header("Authorization") String token);
}