package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFriendActivity extends AppCompatActivity {

    private EditText etInviteCode;
    private Button btnSendInvite;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        etInviteCode = findViewById(R.id.etInviteCode);
        btnSendInvite = findViewById(R.id.btnSendInvite);

        String senderId = getIntent().getStringExtra("senderId");
        String notificationId = getIntent().getStringExtra("notificationId");

        // ✅ JWT 토큰 불러오기
        jwtToken = getToken();

        // ✅ Retrofit 서비스 초기화
        apiService = RetrofitClient.getApiService();

        btnSendInvite.setOnClickListener(v -> {
            String code = etInviteCode.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "초대코드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("🔥JWT", "전송되는 토큰: Bearer " + jwtToken);
            // ✅ Retrofit 요청
            Call<Void> call = apiService.sendFriendRequestByCode("Bearer " + jwtToken, code);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddFriendActivity.this, "친구 요청 성공!", Toast.LENGTH_SHORT).show();
                        // ✅ 알림 뱃지 표시 설정
                        try {
                            MasterKey masterKey = new MasterKey.Builder(AddFriendActivity.this)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                    .build();

                            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                                    getApplicationContext(),
                                    "secure_prefs",
                                    masterKey,
                                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            );

                            securePrefs.edit().putBoolean("hasUnread", true).apply();  // ← 여기!

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        setResult(RESULT_OK); // 친구 추가 성공 결과 전달
                        finish();
                    } else {
                        Toast.makeText(AddFriendActivity.this, "요청 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("AddFriend", "네트워크 오류", t);
                    Toast.makeText(AddFriendActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String getToken() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString("jwt_token", null);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void moveToChatRoom(String friendId) {
        try {
            // 내 user_id 가져오기
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
            String currentUserId = prefs.getString("user_id", null);
            String provider = prefs.getString("provider", "local");
            if (currentUserId == null) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("friendId", friendId);
            intent.putExtra("currentUserId", currentUserId);
            intent.putExtra("currentProvider", provider);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "채팅방 이동 실패", Toast.LENGTH_SHORT).show();
        }
    }
}