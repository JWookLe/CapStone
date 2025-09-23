package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.adapter.FriendListAdapter;
import com.example.EmotionSync.model.FriendItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRateFriendListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private List<FriendItem> friendList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_rate_friend_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendListAdapter(this, friendList, FriendListAdapter.MODE_MATCH_RATE);
        adapter.setOnMatchRateClickListener(friend -> {
            Intent intent = new Intent(this, MatchRateDetailActivity.class);
            intent.putExtra("friendId", friend.getUserId());
            intent.putExtra("friendName", friend.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();
        loadFriends();
    }

    private void loadFriends() {
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
            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }
            apiService.getFriendList("Bearer " + token).enqueue(new Callback<List<FriendItem>>() {
                @Override
                public void onResponse(Call<List<FriendItem>> call, Response<List<FriendItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        friendList.clear();
                        friendList.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MatchRateFriendListActivity.this, "친구 목록을 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<FriendItem>> call, Throwable t) {
                    Toast.makeText(MatchRateFriendListActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
} 