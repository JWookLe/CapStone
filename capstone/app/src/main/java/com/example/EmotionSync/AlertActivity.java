package com.example.EmotionSync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.model.Notification;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // ✅ 툴바 설정
        Toolbar toolbar = findViewById(R.id.alertToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        // ✅ RecyclerView 초기화
        recyclerView = findViewById(R.id.alertRecyclerView);

        // ✅ JWT 토큰 가져오기
        jwtToken = getToken();

        Log.d("🔑TOKEN", "불러온 JWT = " + jwtToken);

        // ✅ Retrofit API 서비스 생성
        apiService = RetrofitClient.getApiService();

        // ✅ 서버에서 알림 불러오기
        loadNotifications();

        // ✅ 알림 확인 처리 → hasUnread = false 저장
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

            securePrefs.edit().putBoolean("hasUnread", false).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications() {
        Call<List<Notification>> call = apiService.getNotifications("Bearer " + jwtToken);
        call.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notificationList = response.body();

                    Log.d("Alert", "알림 개수: " + notificationList.size());

                    NotificationAdapter adapter = new NotificationAdapter(notificationList, AlertActivity.this);

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(AlertActivity.this));
                } else {
                    Toast.makeText(AlertActivity.this, "알림 불러오기 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Toast.makeText(AlertActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
