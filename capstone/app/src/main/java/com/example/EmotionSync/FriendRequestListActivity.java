package com.example.EmotionSync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_list);

        recyclerView = findViewById(R.id.recycler_friend_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        try {
            // 🔐 EncryptedSharedPreferences에서 토큰 불러오기
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
            Log.d("🔥TEST", "불러온 토큰: " + token);

            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Log.d("🔥TEST", "Retrofit 요청 준비");

            apiService.getReceivedFriendRequests("Bearer " + token).enqueue(new Callback<List<FriendRequestItem>>() {
                @Override
                public void onResponse(Call<List<FriendRequestItem>> call, Response<List<FriendRequestItem>> response) {
                    Log.d("🔥TEST", "서버 응답 수신됨");

                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("🔥TEST", "받은 요청 수: " + response.body().size());
                        adapter = new FriendRequestAdapter(FriendRequestListActivity.this, response.body());
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.d("🔥TEST", "요청 실패: " + response.code());
                        Toast.makeText(FriendRequestListActivity.this, "요청 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<FriendRequestItem>> call, Throwable t) {
                    Log.d("🔥TEST", "네트워크 오류: " + t.getMessage());
                    Toast.makeText(FriendRequestListActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("🔥TEST", "보안 설정 오류: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "보안 설정 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
}