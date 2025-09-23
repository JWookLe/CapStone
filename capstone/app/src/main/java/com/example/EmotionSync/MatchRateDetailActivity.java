package com.example.EmotionSync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.model.MatchRateDto;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRateDetailActivity extends AppCompatActivity {
    private CircularProgressBar progressBar;
    private TextView percentText, friendNameText;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_rate_detail);

        progressBar = findViewById(R.id.circularProgressBar);
        percentText = findViewById(R.id.percentText);
        friendNameText = findViewById(R.id.friendNameText);

        String friendId = getIntent().getStringExtra("friendId");
        String friendName = getIntent().getStringExtra("friendName");
        friendNameText.setText(friendName + "님과의 취향매칭률");

        apiService = RetrofitClient.getApiService();
        loadMatchRate(friendId);
    }

    private void loadMatchRate(String friendId) {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            String token = prefs.getString("jwt_token", null);
            String myUserId = prefs.getString("user_id", null);
            if (token == null || myUserId == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // Callback 타입을 MatchRateDto로 변경
            apiService.getMatchRate("Bearer " + token, myUserId, friendId).enqueue(new Callback<MatchRateDto>() {
                @Override
                public void onResponse(Call<MatchRateDto> call, Response<MatchRateDto> response) {
                    Log.d("매칭률", "response=" + response.body());
                    if (response.isSuccessful() && response.body() != null) {
                        MatchRateDto matchRateData = response.body();
                        int matchRate = matchRateData.getMatchRate();
                        boolean currentLikeState = matchRateData.isCurrentLikeState();
                        boolean currentDislikeState = matchRateData.isCurrentDislikeState();

                        progressBar.setProgressWithAnimation((float) matchRate, 1200L);
                        percentText.setText(matchRate + "%");

                        // 현재 선택 상태 로그 (디버깅용)
                        Log.d("매칭률", "Like: " + currentLikeState + ", Dislike: " + currentDislikeState);
                    } else {
                        Toast.makeText(MatchRateDetailActivity.this, "매칭률 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MatchRateDto> call, Throwable t) {
                    Toast.makeText(MatchRateDetailActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
} 