package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentRecommendationActivity extends AppCompatActivity {

    private TextView tvEmotionInfo;
    private TextView tvEmotionDescription;
    private TextView tvPlaylistTitle;
    private TextView tvPlaylistDescription;
    private ImageView ivPlaylistCover;
    private Button btnPlayPlaylist;
    private Button btnShareWithFriends;
    private Button btnBackToHome;

    private RecyclerView rvMovies;
    private RecyclerView rvMusic;
    private RecyclerView rvYoutube;

    private ContentAdapter movieAdapter;
    private ContentAdapter musicAdapter;
    private ContentAdapter youtubeAdapter;

    private String selectedEmotion;
    private int responseQ1, responseQ2, responseQ3, responseQ4;
    private String responseQ5Text;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_recommendation);

        //0613김건하 toolbar(알림)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView ivBell = findViewById(R.id.ivNotificationBell);
        View badgeDot = findViewById(R.id.badgeDot);

        ivBell.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertActivity.class);
            startActivity(intent);
        });

        boolean hasUnread = true;
        badgeDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
                Intent friendIntent = new Intent(this, FriendListActivity.class);
                friendIntent.putExtra("emotion", selectedEmotion);
                friendIntent.putExtra("responseQ1", responseQ1);
                friendIntent.putExtra("responseQ2", responseQ2);
                friendIntent.putExtra("responseQ3", responseQ3);
                friendIntent.putExtra("responseQ4", responseQ4);
                friendIntent.putExtra("responseQ5Text", responseQ5Text);
                startActivity(friendIntent);
                overridePendingTransition(0, 0);
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

            jwtToken = securePrefs.getString("jwt_token", "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            jwtToken = "";
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("emotion") && intent.hasExtra("responseQ5Text")) {
            selectedEmotion = intent.getStringExtra("emotion");
            responseQ1 = intent.getIntExtra("responseQ1", 3);
            responseQ2 = intent.getIntExtra("responseQ2", 3);
            responseQ3 = intent.getIntExtra("responseQ3", 3);
            responseQ4 = intent.getIntExtra("responseQ4", 3);
            responseQ5Text = intent.getStringExtra("responseQ5Text");
        } else {
            Toast.makeText(this, "감정 정보가 누락되었습니다. 처음부터 다시 진행해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        initializeViews();
        setupToolbar();
        updateEmotionInfo();
        setupRecyclerViews();
        loadRecommendations();
        setupButtons();
    }

    //0613김건하


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notification) {
            // 종 버튼 눌렀을 때 알림 화면으로 이동
            Intent intent = new Intent(this, AlertActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

            boolean hasUnread = securePrefs.getBoolean("hasUnread", true);
            View badgeDot = findViewById(R.id.badgeDot); // ← 종 아이콘 옆에 있는 View
            if (badgeDot != null) {
                badgeDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> goBackToHome());
    }

    @Override
    public void onBackPressed() {
        goBackToHome();
    }

    private void initializeViews() {
        tvEmotionInfo = findViewById(R.id.tvEmotionInfo);
        tvEmotionDescription = findViewById(R.id.tvEmotionDescription);
        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle);
        tvPlaylistDescription = findViewById(R.id.tvPlaylistDescription);
        ivPlaylistCover = findViewById(R.id.ivPlaylistCover);
        btnPlayPlaylist = findViewById(R.id.btnPlayPlaylist);
        btnShareWithFriends = findViewById(R.id.btnShareWithFriends);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        rvMovies = findViewById(R.id.rvMovies);
        rvMusic = findViewById(R.id.rvMusic);
        rvYoutube = findViewById(R.id.rvYoutube);
    }

    private void setupRecyclerViews() {
        rvMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        movieAdapter = new ContentAdapter(this, new ArrayList<>());
        rvMovies.setAdapter(movieAdapter);

        rvMusic.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        musicAdapter = new ContentAdapter(this, new ArrayList<>());
        rvMusic.setAdapter(musicAdapter);

        rvYoutube.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        youtubeAdapter = new ContentAdapter(this, new ArrayList<>());
        rvYoutube.setAdapter(youtubeAdapter);
    }

    private void updateEmotionInfo() {
        tvEmotionInfo.setText("나의 감정: " + selectedEmotion);
        tvEmotionDescription.setText(getEmotionDescription(selectedEmotion));
        int color = getColorForEmotion(selectedEmotion);
        findViewById(R.id.layoutEmotionInfo).setBackgroundColor(color);
        updatePlaylistInfo();
    }

    private String getEmotionDescription(String emotion) {
        switch (emotion) {
            case "기쁨": return "긍정적인 감정을 느끼고 있군요.";
            case "슬픔": return "위로가 되는 콘텐츠로 마음을 달래보세요.";
            case "화남": return "감정을 풀 수 있는 콘텐츠를 추천합니다.";
            case "불안": return "마음을 진정시키는 콘텐츠를 추천합니다.";
            default: return "감정 상태에 맞는 콘텐츠를 보여드릴게요.";
        }
    }

    private int getColorForEmotion(String emotion) {
        switch (emotion) {
            case "기쁨": return ContextCompat.getColor(this, R.color.joy_background);
            case "슬픔": return ContextCompat.getColor(this, R.color.sadness_background);
            case "화남": return ContextCompat.getColor(this, R.color.anger_background);
            case "불안": return ContextCompat.getColor(this, R.color.anxiety_background);
            default: return Color.WHITE;
        }
    }

    private void updatePlaylistInfo() {
        tvPlaylistTitle.setText("플레이리스트: " + selectedEmotion);
        tvPlaylistDescription.setText(getPlaylistDescription(selectedEmotion));
        ivPlaylistCover.setImageResource(getPlaylistCoverResource(selectedEmotion));
    }

    private String getPlaylistDescription(String emotion) {
        switch (emotion) {
            case "기쁨": return "기분 좋은 음악 모음";
            case "슬픔": return "마음을 달래는 음악";
            case "화남": return "화를 풀어주는 음악";
            case "불안": return "불안을 가라앉히는 음악";
            default: return "감정에 맞는 음악 모음";
        }
    }

    private int getPlaylistCoverResource(String emotion) {
        switch (emotion) {
            case "기쁨": return R.drawable.playlist_joy;
            case "슬픔": return R.drawable.playlist_sadness;
            case "화남": return R.drawable.playlist_anger;
            case "불안": return R.drawable.playlist_anxiety;
            default: return R.drawable.playlist_default;
        }
    }

    private void loadRecommendations() {
        if (jwtToken == null || jwtToken.isEmpty()) return;

        Call<List<ContentItem>> call = apiService.getContentRecommendations(
                "Bearer " + jwtToken, selectedEmotion, responseQ1, responseQ2, responseQ3, responseQ4, responseQ5Text
        );

        call.enqueue(new Callback<List<ContentItem>>() {
            @Override
            public void onResponse(Call<List<ContentItem>> call, Response<List<ContentItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateRecommendations(response.body());
                } else {
                    Toast.makeText(ContentRecommendationActivity.this, "추천 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ContentItem>> call, Throwable t) {
                Toast.makeText(ContentRecommendationActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecommendations(List<ContentItem> recommendations) {
        List<ContentItem> movies = new ArrayList<>();
        List<ContentItem> music = new ArrayList<>();
        List<ContentItem> youtube = new ArrayList<>();

        for (ContentItem item : recommendations) {
            switch (item.getType().toLowerCase()) {
                case "movie": movies.add(item); break;
                case "music": music.add(item); break;
                case "video":
                case "youtube": youtube.add(item); break;
            }
        }

        movieAdapter.updateContent(movies);
        musicAdapter.updateContent(music);
        youtubeAdapter.updateContent(youtube);
    }

    private void setupButtons() {
        if (btnPlayPlaylist != null) {
            btnPlayPlaylist.setOnClickListener(v ->
                    Toast.makeText(this, "재생 기능은 구현 예정입니다", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnShareWithFriends != null) {
            btnShareWithFriends.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, selectedEmotion + "에 맞는 컨텐츠 추천!");
                startActivity(Intent.createChooser(shareIntent, "공유하기"));
            });
        }

        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> goBackToHome());
        }
    }

    private void goBackToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}