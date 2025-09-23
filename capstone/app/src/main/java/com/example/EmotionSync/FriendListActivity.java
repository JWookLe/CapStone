package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.adapter.FriendListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendListActivity extends AppCompatActivity {
    private static final String TAG = "FriendListActivity";
    private RecyclerView recyclerView;
    private FriendListAdapter adapter;

    //감정 및 설문 응답 상태 변수 추가
    private String selectedEmotion;
    private int responseQ1, responseQ2, responseQ3, responseQ4;
    private String responseQ5Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Log.d(TAG, "onCreate 시작");

        //Intent로 감정 데이터 수신
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("emotion") && intent.hasExtra("responseQ5Text")) {
            selectedEmotion = intent.getStringExtra("emotion");
            responseQ1 = intent.getIntExtra("responseQ1", 3);
            responseQ2 = intent.getIntExtra("responseQ2", 3);
            responseQ3 = intent.getIntExtra("responseQ3", 3);
            responseQ4 = intent.getIntExtra("responseQ4", 3);
            responseQ5Text = intent.getStringExtra("responseQ5Text");
        } else {
            Toast.makeText(this, "감정 데이터가 없습니다. 홈으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        //  RecyclerView 연결
        recyclerView = findViewById(R.id.recycler_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //  친구 목록 불러오기
        loadFriends();

        //  친구 추가 버튼
        Button btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAddFriend.setOnClickListener(v -> {
            startActivity(new Intent(FriendListActivity.this, AddFriendActivity.class));
        });

        Button btnViewFriendRequests = findViewById(R.id.btnViewFriendRequests);
        btnViewFriendRequests.setOnClickListener(v -> {
            Intent intent2 = new Intent(FriendListActivity.this, FriendRequestListActivity.class);
            startActivity(intent2);
        });

        //  하단바 동작 유지
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_friends);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent homeIntent = new Intent(this, ContentRecommendationActivity.class);
                homeIntent.putExtra("emotion", selectedEmotion);
                homeIntent.putExtra("responseQ1", responseQ1);
                homeIntent.putExtra("responseQ2", responseQ2);
                homeIntent.putExtra("responseQ3", responseQ3);
                homeIntent.putExtra("responseQ4", responseQ4);
                homeIntent.putExtra("responseQ5Text", responseQ5Text);
                startActivity(homeIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_friends) {
                return true;
            } else if (id == R.id.nav_match_rate) {
                startActivity(new Intent(this, MatchRateFriendListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyInfoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFriends(); // 항상 화면에 올 때마다 친구목록 새로고침
    }

    //  친구 목록 불러오는 함수
    private void loadFriends() {
        Log.d(TAG, "loadFriends 시작");

        try {
            // 🔹 EncryptedSharedPreferences에서 JWT 토큰 꺼내기
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

            if (token == null) {
                Log.e(TAG, "토큰이 없습니다.");
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "토큰 확인됨: " + token.substring(0, 20) + "...");

            // 🔹 Retrofit 호출
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<List<com.example.EmotionSync.model.FriendItem>> call = apiService.getFriendList("Bearer " + token);
            Log.d(TAG, "API 호출 준비 완료");

            call.enqueue(new Callback<List<com.example.EmotionSync.model.FriendItem>>() {
                @Override
                public void onResponse(Call<List<com.example.EmotionSync.model.FriendItem>> call, Response<List<com.example.EmotionSync.model.FriendItem>> response) {
                    Log.d(TAG, "서버 응답 받음. 코드: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        List<com.example.EmotionSync.model.FriendItem> friendList = response.body();
                        Log.d(TAG, "친구 목록 응답: " + friendList);
                        adapter = new FriendListAdapter(FriendListActivity.this, friendList, FriendListAdapter.MODE_FRIEND);
                        adapter.setOnFriendClickListener(friend -> {
                            Intent intentChat = new Intent(FriendListActivity.this, ChatRoomActivity.class);
                            intentChat.putExtra("friendId", friend.getUserId());
                            intentChat.putExtra("friendName", friend.getName());
                            startActivity(intentChat);
                        });
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "친구 목록 불러오기 실패: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "error body is null";
                            Log.e(TAG, "에러 응답: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "에러 응답 파싱 실패", e);
                        }
                        Toast.makeText(FriendListActivity.this, "친구 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<com.example.EmotionSync.model.FriendItem>> call, Throwable t) {
                    Log.e(TAG, "서버 통신 실패", t);
                    Toast.makeText(FriendListActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "EncryptedSharedPreferences 초기화 실패", e);
            Toast.makeText(this, "보안 설정 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
