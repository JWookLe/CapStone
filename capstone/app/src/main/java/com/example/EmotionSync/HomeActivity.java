package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 토큰 체크
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    getApplicationContext(),
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String token = securePrefs.getString("jwt_token", null);
            if (token == null || token.isEmpty() || !isTokenValid(token)) {
                // 토큰이 없거나 유효하지 않으면 로그인 화면으로 이동
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        apiService = RetrofitClient.getApiService();

        // UI 요소 초기화 - MaterialCardView를 사용
        com.google.android.material.card.MaterialCardView btnHappy = findViewById(R.id.btnHappy);
        com.google.android.material.card.MaterialCardView btnSad = findViewById(R.id.btnSad);
        com.google.android.material.card.MaterialCardView btnAngry = findViewById(R.id.btnAngry);
        com.google.android.material.card.MaterialCardView btnAnxious = findViewById(R.id.btnAnxious);
        com.google.android.material.button.MaterialButton logoutButton = findViewById(R.id.logoutButton);

        // 감정 버튼 클릭 이벤트 설정
        btnHappy.setOnClickListener(v -> saveEmotionRecord("기쁨"));
        btnSad.setOnClickListener(v -> saveEmotionRecord("슬픔"));
        btnAngry.setOnClickListener(v -> saveEmotionRecord("화남"));
        btnAnxious.setOnClickListener(v -> saveEmotionRecord("불안"));

        // 로그아웃 버튼 클릭 이벤트
        logoutButton.setOnClickListener(v -> {
            try {
                // 토큰 삭제
                MasterKey masterKey = new MasterKey.Builder(this)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();

                SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                        getApplicationContext(),
                        "secure_prefs",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                securePrefs.edit().remove("jwt_token").apply();

                // 로그인 화면으로 이동
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "로그아웃 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEmotionRecord(String emotion) {
        try {
            // EncryptedSharedPreferences에서 토큰 가져오기
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    getApplicationContext(),
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String token = securePrefs.getString("jwt_token", null);

            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // 토큰에서 사용자 ID 추출
            String userId = getUserIdFromToken(token);

            if (userId == null) {
                Toast.makeText(this, "인증 정보가 유효하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // 감정 기록 데이터 준비
            Map<String, String> emotionData = new HashMap<>();
            emotionData.put("userId", userId);
            emotionData.put("emotionType", emotion);
            emotionData.put("contextData", "앱에서 감정 버튼 클릭");

            // API 호출
            Call<Map<String, Object>> call = apiService.recordEmotion(
                    "Bearer " + token,
                    emotionData
            );

            // 디버깅을 위한 로그 추가
            Log.d("EmotionRecord", "API 요청 정보:");
            Log.d("EmotionRecord", "Token: " + token);
            Log.d("EmotionRecord", "Emotion Data: " + emotionData.toString());

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    // 응답 코드와 에러 바디 로깅
                    Log.d("EmotionRecord", "응답 코드: " + response.code());
                    Log.d("EmotionRecord", "응답 성공 여부: " + response.isSuccessful());

                    if (!response.isSuccessful() && response.errorBody() != null) {
                        try {
                            Log.e("EmotionRecord", "에러 바디: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> responseData = response.body();
                        Log.d("EmotionRecord", "응답 데이터: " + responseData.toString());

                        // 서버 응답에서 record_id 또는 recordId 추출
                        if (responseData.containsKey("record_id")) {
                            long recordId = ((Number) responseData.get("record_id")).longValue();

                            // recordId 저장
                            SharedPreferences prefs = getSharedPreferences("emotion_sync_prefs", MODE_PRIVATE);
                            prefs.edit().putLong("latest_emotion_record_id", recordId).apply();

                            Log.d("EmotionRecord", "감정 기록 ID: " + recordId);

                            // 성공 시 설문 화면으로 이동
                            startEmotionSurvey(emotion, recordId);
                        } else if (responseData.containsKey("recordId")) {
                            long recordId = ((Number) responseData.get("recordId")).longValue();

                            // recordId 저장
                            SharedPreferences prefs = getSharedPreferences("emotion_sync_prefs", MODE_PRIVATE);
                            prefs.edit().putLong("latest_emotion_record_id", recordId).apply();

                            Log.d("EmotionRecord", "감정 기록 ID: " + recordId);

                            // 성공 시 설문 화면으로 이동
                            startEmotionSurvey(emotion, recordId);
                        } else {
                            // recordId가 없는 경우 사용자에게 알려주지만 설문으로 넘어가지 않음
                            Toast.makeText(HomeActivity.this,
                                    "감정이 기록되었지만 설문을 시작할 수 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 서버 응답 실패 시 설문으로 이동하지 않음
                        Toast.makeText(HomeActivity.this,
                                "감정 기록에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // 네트워크 실패 시 설문으로 이동하지 않음
                    Toast.makeText(HomeActivity.this,
                            "서버 연결에 실패했습니다. 네트워크 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 설문으로 이동하지 않고 오류 메시지만 표시
            Toast.makeText(this, "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    // 감정 설문 화면으로 이동하는 메서드 수정
    private void startEmotionSurvey(String emotion, long recordId) {
        // EmotionSurveyActivity로 이동하는 Intent 생성
        Intent intent = new Intent(HomeActivity.this, EmotionSurveyActivity.class);
        intent.putExtra("emotion", emotion); // 선택한 감정 정보 전달
        intent.putExtra("recordId", recordId); // 감정 기록 ID 전달
        startActivity(intent);

        Toast.makeText(this, emotion + " 감정이 선택되었습니다.", Toast.LENGTH_SHORT).show();
    }

    // JWT 토큰에서 사용자 ID 추출
    private String getUserIdFromToken(String token) {
        try {
            // JWT 토큰의 시크릿 키 (서버와 동일한 키 사용해야 함)
            String secretKey = "my-super-secure-jwt-secret-key-2025-very-long-and-random";

            // JWT 토큰 파싱
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰의 subject에서 사용자 ID 가져오기
            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // JWT 토큰 유효성 검사 함수 추가
    private boolean isTokenValid(String token) {
        try {
            String secretKey = "my-super-secure-jwt-secret-key-2025-very-long-and-random";
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration() != null) {
                return claims.getExpiration().getTime() > System.currentTimeMillis();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}