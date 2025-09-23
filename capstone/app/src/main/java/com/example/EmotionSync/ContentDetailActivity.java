package com.example.EmotionSync;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.bumptech.glide.Glide;
import com.example.EmotionSync.adapter.FriendListAdapter;
import com.example.EmotionSync.model.FriendItem;
import com.example.EmotionSync.model.Share;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentDetailActivity extends AppCompatActivity {
    private TextView tvTitle;
    private TextView tvDescription;
    private ImageView ivContent;
    private ImageView ivBackdrop;
    private Button btnShare;
    private Button btnVisitSite;
    private ImageButton btnLike;
    private ImageButton btnDislike;
    private String contentUrl;
    private String contentType;
    private String contentId;
    private String contentTitle;
    private ApiService apiService;
    private String jwtToken;
    private boolean isShared;
    private String sharedBy;
    private String sharedTo;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2;

    // YouTube 관련 뷰
    private LinearLayout layoutYoutubeInfo;
    private TextView tvChannelName;
    private TextView tvViewCount;
    private TextView tvUploadDate;
    private TextView tvDuration;
    private TextView tvLikeCount;
    private TextView tvCommentCount;

    // 영화 관련 뷰
    private LinearLayout layoutMovieInfo;
    private TextView tvReleaseDate;
    private TextView tvRuntime;
    private TextView tvRating;
    private TextView tvGenres;
    private TextView tvDirector;
    private TextView tvCast;
    private TextView tvTags;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_detail);

        // 뷰 초기화
        initializeViews();

        // Intent에서 정보 가져오기
        Intent intent = getIntent();
        String contentType = intent.getStringExtra("contentType");
        String contentId = intent.getStringExtra("contentId");
        String deepLink = intent.getStringExtra("deepLink");
        String sharedBy = intent.getStringExtra("sharedBy");
        String sharedTo = intent.getStringExtra("sharedTo");
        boolean isShared = intent.getBooleanExtra("isShared", false);
        String title = intent.getStringExtra("title");
        String contentUrl = intent.getStringExtra("contentUrl");

        // 기존 파라미터 이름으로도 시도
        if (contentType == null) contentType = intent.getStringExtra("type");
        if (contentId == null) contentId = intent.getStringExtra("id");
        if (title == null) title = intent.getStringExtra("contentTitle");

        Log.d("ContentDetailActivity", "Intent에서 받은 정보 - Type: " + contentType + 
            ", ID: " + contentId + ", IsShared: " + isShared + 
            ", SharedBy: " + sharedBy + ", SharedTo: " + sharedTo);

        // 딥링크 처리
        if (deepLink != null) {
            Uri uri = Uri.parse(deepLink);
            String path = uri.getPath();
            if (path != null) {
                String[] segments = path.split("/");
                if (segments.length >= 3) {
                    contentType = segments[1];
                    contentId = segments[2];
                }
            }
            // 쿼리 파라미터에서 공유 정보 추출
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if (keyValue[0].equals("sharedBy")) {
                            sharedBy = keyValue[1];
                        } else if (keyValue[0].equals("sharedTo")) {
                            sharedTo = keyValue[1];
                        }
                    }
                }
                isShared = (sharedBy != null && sharedTo != null);
            }
        }

        // 변수 초기화
        this.contentType = contentType;
        this.contentId = contentId;
        this.isShared = isShared;
        this.sharedBy = sharedBy;
        this.sharedTo = sharedTo;
        this.contentTitle = title;
        this.contentUrl = contentUrl;

        Log.d("ContentDetailActivity", "Content info - ID: " + contentId + 
            ", Type: " + contentType + ", Title: " + title + 
            ", URL: " + contentUrl + ", IsShared: " + isShared + 
            ", SharedBy: " + sharedBy + ", SharedTo: " + sharedTo);

        // 현재 사용자 ID 가져오기
        String currentUserId;
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            currentUserId = securePrefs.getString("user_id", null);
            
            // 공유 정보가 있고 현재 사용자가 공유받은 사람인 경우
            if (isShared && sharedTo != null && sharedTo.equals(currentUserId)) {
                Log.d("ContentDetailActivity", "공유받은 컨텐츠 확인 - SharedBy: " + sharedBy + ", SharedTo: " + sharedTo);
            }
        } catch (Exception e) {
            Log.e("ContentDetailActivity", "사용자 ID 가져오기 실패", e);
            currentUserId = null;
        }

        // JWT 토큰 가져오기
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

        apiService = RetrofitClient.getClient().create(ApiService.class);
        if (contentId != null && contentType != null) {
            fetchContentDetails(contentType, contentId);
        } else {
            Toast.makeText(this, "상세 정보 요청에 필요한 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        setupButtons();
    }

    private void initializeViews() {
        // 기본 뷰 초기화
        tvTitle = findViewById(R.id.tvContentTitle);
        tvDescription = findViewById(R.id.tvContentDescription);
        ivContent = findViewById(R.id.ivContentImage);
        ivBackdrop = findViewById(R.id.ivBackdrop);
        btnShare = findViewById(R.id.btnShare);
        btnVisitSite = findViewById(R.id.btnVisitSite);
        btnLike = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);
        tvTags = findViewById(R.id.tvTags);
        btnClose = findViewById(R.id.btnClose);

        // YouTube 관련 뷰 초기화
        layoutYoutubeInfo = findViewById(R.id.layoutYoutubeInfo);
        tvChannelName = findViewById(R.id.tvChannelName);
        tvViewCount = findViewById(R.id.tvViewCount);
        tvUploadDate = findViewById(R.id.tvUploadDate);
        tvDuration = findViewById(R.id.tvDuration);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvCommentCount = findViewById(R.id.tvCommentCount);

        // 영화 관련 뷰 초기화
        layoutMovieInfo = findViewById(R.id.layoutMovieInfo);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        tvRuntime = findViewById(R.id.tvRuntime);
        tvRating = findViewById(R.id.tvRating);
        tvGenres = findViewById(R.id.tvGenres);
        tvDirector = findViewById(R.id.tvDirector);
        tvCast = findViewById(R.id.tvCast);
    }

    private void fetchContentDetails(String type, String id) {
        Log.d("ContentDetailActivity", "Fetching content details for type: " + type + ", id: " + id);
        apiService.getContentDetails("Bearer " + jwtToken, type, id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d("ContentDetailActivity", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    updateUIWithDetails(response.body());
                } else {
                    if (response.code() == 403 || response.code() == 401) {
                        Log.d("ContentDetailActivity", "Authentication failed");
                        Intent intent = new Intent(ContentDetailActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ContentDetailActivity.this, "상세 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("ContentDetailActivity", "Network error", t);
                Toast.makeText(ContentDetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithDetails(Map<String, Object> details) {
        String title = (String) details.get("title");
        String description = (String) details.get("overview");
        String imageUrl = (String) details.get("image_url");
        String backdropUrl = (String) details.get("backdrop_url");
        String contentUrlValue = (String) details.get("link_url");
        
        // contentTitle 업데이트
        this.contentTitle = title;
        
        tvTitle.setText(title);
        tvDescription.setText(description);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivContent);
        }
        if (backdropUrl != null && !backdropUrl.isEmpty()) {
            Glide.with(this).load(backdropUrl).into(ivBackdrop);
        }
        contentUrl = contentUrlValue;

        Log.d("ContentDetailActivity", "컨텐츠 정보 업데이트 완료 - Title: " + title + ", URL: " + contentUrl);

        // type별 UI
        if ("movie".equals(contentType)) {
            layoutMovieInfo.setVisibility(View.VISIBLE);
            layoutYoutubeInfo.setVisibility(View.GONE);
            // 영화 정보 바인딩
            tvReleaseDate.setText((String) details.get("release_date"));
            tvRuntime.setText(details.get("runtime") != null ? details.get("runtime") + "분" : "");
            tvRating.setText(details.get("vote_average") != null ? details.get("vote_average") + "/10" : "");
            // 장르
            List<String> genres = (List<String>) details.get("genres");
            if (genres != null && !genres.isEmpty()) {
                tvGenres.setText("장르: " + android.text.TextUtils.join(" ", genres));
            } else {
                tvGenres.setText("");
            }
            // 감독
            tvDirector.setText("감독: " + (String) details.get("director"));
            // 배우
            List<String> cast = (List<String>) details.get("cast");
            if (cast != null && !cast.isEmpty()) {
                tvCast.setText("출연: " + android.text.TextUtils.join(", ", cast));
            } else {
                tvCast.setText("");
            }
            tvTags.setVisibility(View.GONE);
        } else { // YouTube/음악/영상
            layoutMovieInfo.setVisibility(View.GONE);
            layoutYoutubeInfo.setVisibility(View.VISIBLE);
            tvChannelName.setText((String) details.get("channel_title"));
            tvViewCount.setText("👁️ " + (String) details.get("view_count"));
            tvLikeCount.setText("👍 " + (String) details.get("like_count"));
            tvCommentCount.setText("💬 " + (String) details.get("comment_count"));
            tvDuration.setText("⏱️ " + (String) details.get("duration"));
            tvUploadDate.setText("📅 " + (String) details.get("published_at"));
            // 태그
            List<String> tags = (List<String>) details.get("tags");
            if (tags != null && !tags.isEmpty()) {
                tvTags.setText("#" + android.text.TextUtils.join(" #", tags));
                tvTags.setVisibility(View.VISIBLE);
            } else {
                tvTags.setVisibility(View.GONE);
            }
        }
    }

    private void setupButtons() {
        btnShare.setOnClickListener(v -> showFriendSelectionDialog());
        btnVisitSite.setOnClickListener(v -> visitContentSite());
        btnClose.setOnClickListener(v -> finish());
        btnLike.setOnClickListener(v -> handleLikeDislike(true));
        btnDislike.setOnClickListener(v -> handleLikeDislike(false));
    }

    private void showFriendSelectionDialog() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            String token = securePrefs.getString("jwt_token", null);
            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            apiService.getFriends("Bearer " + token).enqueue(new Callback<List<FriendItem>>() {
                @Override
                public void onResponse(Call<List<FriendItem>> call, Response<List<FriendItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Dialog dialog = new Dialog(ContentDetailActivity.this);
                        dialog.setContentView(R.layout.dialog_friend_list);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_friends);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ContentDetailActivity.this));
                        FriendListAdapter adapter = new FriendListAdapter(ContentDetailActivity.this, response.body(), FriendListAdapter.MODE_FRIEND);
                        recyclerView.setAdapter(adapter);

                        // 공유+채팅방 이동 로직을 별도 메서드로 분리
                        adapter.setOnShareClickListener(friend -> shareToFriend(friend, securePrefs, apiService, token, dialog));
                        adapter.setOnFriendClickListener(friend -> shareToFriend(friend, securePrefs, apiService, token, dialog));
                        
                        dialog.show();
                    } else {
                        Toast.makeText(ContentDetailActivity.this, "친구 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<FriendItem>> call, Throwable t) {
                    Toast.makeText(ContentDetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(this, "보안 설정 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToFriend(FriendItem friend, SharedPreferences securePrefs, ApiService apiService, String token, Dialog dialog) {
        try {
            String currentUserId = securePrefs.getString("user_id", null);
            String provider = securePrefs.getString("provider", "local");
            if (currentUserId == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            String formattedUserId;
            Log.d("ContentDetailActivity", "Provider 확인: '" + provider + "'");
            
            if ("kakao".equals(provider) || "KAKAO".equals(provider)) {
                formattedUserId = "kakao_" + currentUserId + "@kakao.local";
            } else if ("google".equals(provider) || "GOOGLE".equals(provider)) {
                // 구글 계정은 서버에서 가져온 이메일 사용
                String email = securePrefs.getString("email", null);
                if (email != null && !email.isEmpty()) {
                    formattedUserId = email;
                    Log.d("ContentDetailActivity", "구글 계정 - 서버 이메일 사용: " + formattedUserId);
                } else {
                    formattedUserId = currentUserId;
                    Log.d("ContentDetailActivity", "구글 계정 - 이메일 없음, ID 사용: " + formattedUserId);
                }
            } else {
                formattedUserId = currentUserId;
            }
            final String finalUserId = formattedUserId;
            
            Log.d("ContentDetailActivity", "사용자 정보 - ID: " + currentUserId + ", Provider: " + provider + ", Formatted: " + formattedUserId);
            
            // contentTitle이 null인지 확인
            if (contentTitle == null || contentTitle.isEmpty()) {
                Log.e("ContentDetailActivity", "contentTitle이 null입니다. 공유를 중단합니다.");
                Toast.makeText(this, "컨텐츠 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 새로운 공유 객체 생성 (기존 공유 정보와 관계없이)
            Share share = new Share();
            // setUser1Id 메서드가 구글 계정을 카카오 계정으로 변환하는 문제를 해결하기 위해 직접 설정
            share.setUser1IdDirect(finalUserId);
            share.setUser2Id(friend.getUserId());
            
            // 공유 URL 생성 (기존 공유 정보와 관계없이 새로운 공유)
            String shareUrl = String.format("emotion-sync://content/%s/%s?sharedBy=%s&sharedTo=%s",
                    contentType, contentId, finalUserId, friend.getUserId());
            share.setContentUrl(shareUrl);
            share.setLiked(false);
            share.setDisliked(false);
            
            Log.d("ContentDetailActivity", "새로운 공유 생성 - From: " + finalUserId + ", To: " + friend.getUserId() + ", Content: " + contentType + "/" + contentId);
            Log.d("ContentDetailActivity", "공유할 컨텐츠 제목: " + contentTitle);
            
            apiService.shareContent("Bearer " + token, share).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String displayUrl = String.format("emotion-sync://content/%s/%s", contentType, contentId);
                        ChatMessage message = new ChatMessage(
                                UUID.randomUUID().toString(),
                                finalUserId,
                                friend.getUserId(),
                                String.format("%s 컨텐츠를 공유했습니다.\n%s", contentTitle, displayUrl),
                                System.currentTimeMillis(),
                                true,
                                false
                        );
                        
                        Log.d("ContentDetailActivity", "ChatRoomActivity로 전달할 정보:");
                        Log.d("ContentDetailActivity", "- friendId: " + friend.getUserId());
                        Log.d("ContentDetailActivity", "- friendName: " + friend.getName());
                        Log.d("ContentDetailActivity", "- contentId: " + contentId);
                        Log.d("ContentDetailActivity", "- contentType: " + contentType);
                        Log.d("ContentDetailActivity", "- title: " + contentTitle);
                        Log.d("ContentDetailActivity", "- contentUrl: " + contentUrl);
                        Log.d("ContentDetailActivity", "- message: " + message.getContent());
                        
                        Intent intent = new Intent(ContentDetailActivity.this, ChatRoomActivity.class);
                        intent.putExtra("friendId", friend.getUserId());
                        intent.putExtra("friendName", friend.getName());
                        intent.putExtra("contentId", contentId);
                        intent.putExtra("contentType", contentType);
                        intent.putExtra("title", contentTitle);
                        intent.putExtra("contentUrl", contentUrl);
                        intent.putExtra("message", message.getContent());
                        intent.putExtra("deepLink", shareUrl);  // 새로운 공유 정보가 포함된 URL
                        startActivity(intent);
                        dialog.dismiss();
                        Toast.makeText(ContentDetailActivity.this, friend.getName() + "에게 공유되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ContentDetailActivity", "공유 실패 - 코드: " + response.code());
                        Toast.makeText(ContentDetailActivity.this, "공유에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ContentDetailActivity", "공유 네트워크 오류", t);
                    Toast.makeText(ContentDetailActivity.this, "공유에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ContentDetailActivity", "Error sharing content", e);
            Toast.makeText(this, "공유 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLikeStatus(boolean isLiked, boolean isDisliked) {
        Log.d("ContentDetailActivity", "좋아요/싫어요 상태 업데이트 - Liked: " + isLiked + ", Disliked: " + isDisliked);
        
        // 애니메이션 효과를 위한 스케일 변환
        float scale = 1.2f;
        btnLike.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(100)
            .withEndAction(() -> {
                btnLike.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        // 좋아요 버튼 상태 업데이트
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_thumb_up_filled);
            btnLike.setBackgroundTintList(getColorStateList(R.color.like_color));
        } else {
            btnLike.setImageResource(R.drawable.ic_thumb_up);
            btnLike.setBackgroundTintList(getColorStateList(android.R.color.white));
        }

        // 싫어요 버튼 상태 업데이트
        if (isDisliked) {
            btnDislike.setImageResource(R.drawable.ic_thumb_down_filled);
            btnDislike.setBackgroundTintList(getColorStateList(R.color.dislike_color));
        } else {
            btnDislike.setImageResource(R.drawable.ic_thumb_down);
            btnDislike.setBackgroundTintList(getColorStateList(android.R.color.white));
        }
    }

    // 기존 메서드와의 호환성을 위한 오버로드
    private void updateLikeStatus(boolean isLiked) {
        updateLikeStatus(isLiked, false);
    }

    private void handleLikeDislike(boolean isLike) {
        String jwtToken = getJwtToken();
        if (jwtToken == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 추천 컨텐츠인 경우
        if (sharedBy == null || sharedTo == null) {
            Toast.makeText(this, "공유받은 컨텐츠에서만 좋아요/싫어요가 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // contentURL 생성
        String contentUrl = String.format("/content/%s/%s", contentType, contentId);
        Log.d("ContentDetailActivity", "좋아요/싫어요 처리 시작 - contentURL: " + contentUrl);

        apiService.getSharesBetween("Bearer " + jwtToken, sharedBy, sharedTo, contentUrl)
            .enqueue(new Callback<Share>() {
                @Override
                public void onResponse(Call<Share> call, Response<Share> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Share share = response.body();
                        String expectedUrl = String.format("emotion-sync://content/%s/%s", contentType, contentId);
                        
                        if (share.getContentUrl() != null && share.getContentUrl().contains(expectedUrl)) {
                            // 서버에서 토글 처리하므로 단순히 API 호출
                            Call<Void> toggleCall;
                            if (isLike) {
                                toggleCall = apiService.likeShare("Bearer " + jwtToken, share.getId().toString());
                                Log.d("ContentDetailActivity", "좋아요 토글 요청");
                            } else {
                                toggleCall = apiService.dislikeShare("Bearer " + jwtToken, share.getId().toString());
                                Log.d("ContentDetailActivity", "싫어요 토글 요청");
                            }

                            toggleCall.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        // 토글 후 상태를 다시 조회하여 UI 업데이트
                                        fetchShareInfo();
                                    } else if (response.code() == 403) {
                                        Log.e("ContentDetailActivity", "좋아요/싫어요 처리 권한 없음 - " + response.code());
                                        Toast.makeText(ContentDetailActivity.this, 
                                            "좋아요/싫어요 처리가 불가능합니다.", 
                                            Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e("ContentDetailActivity", "좋아요/싫어요 처리 실패 - " + response.code());
                                        Toast.makeText(ContentDetailActivity.this, 
                                            "처리 중 오류가 발생했습니다.", 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("ContentDetailActivity", "좋아요/싫어요 처리 네트워크 오류", t);
                                    Toast.makeText(ContentDetailActivity.this, 
                                        "네트워크 오류가 발생했습니다.", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ContentDetailActivity.this, 
                                "공유 정보를 찾을 수 없습니다.", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else if (response.code() == 403) {
                        handleTokenError(isLike);
                    } else {
                        Toast.makeText(ContentDetailActivity.this, 
                            "공유 정보를 가져올 수 없습니다.", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Share> call, Throwable t) {
                    Log.e("ContentDetailActivity", "공유 정보 조회 실패: " + t.getMessage());
                    Toast.makeText(ContentDetailActivity.this, 
                        "네트워크 오류가 발생했습니다.", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchShareInfo() {
        Log.d("ContentDetailActivity", "공유 정보 조회 시작");
        String jwtToken = getJwtToken();
        if (jwtToken == null) {
            Log.e("ContentDetailActivity", "JWT 토큰이 없습니다.");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // contentURL 생성
        String contentUrl = String.format("/content/%s/%s", contentType, contentId);
        Log.d("ContentDetailActivity", "조회할 contentURL: " + contentUrl);

        apiService.getSharesBetween("Bearer " + jwtToken, sharedBy, sharedTo, contentUrl)
            .enqueue(new Callback<Share>() {
                @Override
                public void onResponse(Call<Share> call, Response<Share> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Share share = response.body();
                        Log.d("ContentDetailActivity", "공유 정보 조회 성공: " + share.getId());
                        
                        // contentURL이 일치하는지 확인
                        String expectedUrl = String.format("emotion-sync://content/%s/%s", contentType, contentId);
                        if (share.getContentUrl() != null && share.getContentUrl().contains(expectedUrl)) {
                            Log.d("ContentDetailActivity", "대상 공유 정보 찾음: " + share.getId());
                            Log.d("ContentDetailActivity", "서버 응답 상태 - Liked: " + share.isLiked() + ", Disliked: " + share.isDisliked());
                            updateLikeStatus(share.isLiked(), share.isDisliked());
                        } else {
                            Log.d("ContentDetailActivity", "대상 공유 정보를 찾을 수 없음");
                            disableLikeButtons();
                            Toast.makeText(ContentDetailActivity.this, 
                                "공유 정보를 찾을 수 없습니다.", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ContentDetailActivity", "공유 정보 조회 실패: " + response.code());
                        if (response.code() == 403) {
                            handleTokenError(false);
                        } else {
                            disableLikeButtons();
                            Toast.makeText(ContentDetailActivity.this, 
                                "공유 정보를 가져올 수 없습니다. 컨텐츠 정보만 표시됩니다.", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Share> call, Throwable t) {
                    Log.e("ContentDetailActivity", "공유 정보 조회 실패: " + t.getMessage());
                    disableLikeButtons();
                    Toast.makeText(ContentDetailActivity.this, 
                        "네트워크 오류가 발생했습니다. 컨텐츠 정보만 표시됩니다.", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void disableLikeButtons() {
        runOnUiThread(() -> {
            if (btnLike != null) {
                btnLike.setEnabled(false);
                btnLike.setAlpha(0.5f);
            }
            if (btnDislike != null) {
                btnDislike.setEnabled(false);
                btnDislike.setAlpha(0.5f);
            }
        });
    }

    private void visitContentSite() {
        if (contentUrl != null && !contentUrl.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contentUrl));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "방문할 URL이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getJwtToken() {
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
            return securePrefs.getString("jwt_token", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleTokenError(boolean isLike) {
        // 토큰 갱신 로직을 구현해야 합니다.
        // 현재는 간단히 로그인 화면으로 이동하도록 합니다.
        Intent intent = new Intent(ContentDetailActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void shareContent() {
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
            if (token == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버에서 사용자 정보 조회
            ApiService apiService = RetrofitClient.getApiService();
            Call<Map<String, Object>> userInfoCall = apiService.getUserInfo("Bearer " + token);
            userInfoCall.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> userData = response.body();
                        String serverUserId = (String) userData.get("id");
                        String provider = (String) userData.get("provider");
                        String email = (String) userData.get("email");
                        
                        Log.d("ContentDetailActivity", "서버에서 조회한 사용자 정보 - ID: " + serverUserId + ", Provider: " + provider + ", Email: " + email);
                        
                        // 서버에서 가져온 정보로 공유 실행
                        executeShare(serverUserId, provider, email);
                    } else {
                        Toast.makeText(ContentDetailActivity.this, "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("ContentDetailActivity", "사용자 정보 조회 실패", t);
                    Toast.makeText(ContentDetailActivity.this, "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("ContentDetailActivity", "공유 준비 실패", e);
            Toast.makeText(this, "공유 준비 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeShare(String userId, String provider, String email) {
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
            String currentUserId = userId;
            String formattedUserId;
            
            Log.d("ContentDetailActivity", "Provider 확인: '" + provider + "'");
            
            if ("kakao".equals(provider) || "KAKAO".equals(provider)) {
                formattedUserId = "kakao_" + currentUserId + "@kakao.local";
            } else if ("google".equals(provider) || "GOOGLE".equals(provider)) {
                // 구글 계정은 서버에서 가져온 이메일 사용
                if (email != null && !email.isEmpty()) {
                    formattedUserId = email;
                    Log.d("ContentDetailActivity", "구글 계정 - 서버 이메일 사용: " + formattedUserId);
                } else {
                    formattedUserId = currentUserId;
                    Log.d("ContentDetailActivity", "구글 계정 - 이메일 없음, ID 사용: " + formattedUserId);
                }
            } else {
                formattedUserId = currentUserId;
            }
            final String finalUserId = formattedUserId;
            
            Log.d("ContentDetailActivity", "사용자 정보 - ID: " + currentUserId + ", Provider: " + provider + ", Formatted: " + formattedUserId);
            
            // contentTitle이 null인지 확인
            if (contentTitle == null || contentTitle.isEmpty()) {
                Log.e("ContentDetailActivity", "contentTitle이 null입니다. 공유를 중단합니다.");
                Toast.makeText(this, "컨텐츠 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 새로운 공유 객체 생성 (기존 공유 정보와 관계없이)
            Share share = new Share();
            // setUser1Id 메서드가 구글 계정을 카카오 계정으로 변환하는 문제를 해결하기 위해 직접 설정
            share.setUser1IdDirect(finalUserId);
            share.setUser2Id(sharedTo);
            
            // 공유 URL 생성 (기존 공유 정보와 관계없이 새로운 공유)
            String shareUrl = String.format("emotion-sync://content/%s/%s?sharedBy=%s&sharedTo=%s",
                    contentType, contentId, finalUserId, sharedTo);
            share.setContentUrl(shareUrl);
            share.setLiked(false);
            share.setDisliked(false);
            
            Log.d("ContentDetailActivity", "새로운 공유 생성 - From: " + finalUserId + ", To: " + sharedTo + ", Content: " + contentType + "/" + contentId);
            Log.d("ContentDetailActivity", "공유할 컨텐츠 제목: " + contentTitle);
            
            apiService.shareContent("Bearer " + token, share).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String displayUrl = String.format("emotion-sync://content/%s/%s", contentType, contentId);
                        ChatMessage message = new ChatMessage(
                                UUID.randomUUID().toString(),
                                finalUserId,
                                sharedTo,
                                String.format("%s 컨텐츠를 공유했습니다.\n%s", contentTitle, displayUrl),
                                System.currentTimeMillis(),
                                true,
                                false
                        );
                        
                        Log.d("ContentDetailActivity", "ChatRoomActivity로 전달할 정보:");
                        Log.d("ContentDetailActivity", "- friendId: " + sharedTo);
                        Log.d("ContentDetailActivity", "- friendName: " + sharedTo);
                        Log.d("ContentDetailActivity", "- contentId: " + contentId);
                        Log.d("ContentDetailActivity", "- contentType: " + contentType);
                        Log.d("ContentDetailActivity", "- title: " + contentTitle);
                        Log.d("ContentDetailActivity", "- contentUrl: " + contentUrl);
                        Log.d("ContentDetailActivity", "- message: " + message.getContent());
                        
                        Intent intent = new Intent(ContentDetailActivity.this, ChatRoomActivity.class);
                        intent.putExtra("friendId", sharedTo);
                        intent.putExtra("friendName", sharedTo);
                        intent.putExtra("contentId", contentId);
                        intent.putExtra("contentType", contentType);
                        intent.putExtra("title", contentTitle);
                        intent.putExtra("contentUrl", contentUrl);
                        intent.putExtra("message", message.getContent());
                        intent.putExtra("deepLink", shareUrl);  // 새로운 공유 정보가 포함된 URL
                        startActivity(intent);
                        Toast.makeText(ContentDetailActivity.this, sharedTo + "에게 공유되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ContentDetailActivity", "공유 실패 - 코드: " + response.code());
                        Toast.makeText(ContentDetailActivity.this, "공유에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ContentDetailActivity", "공유 네트워크 오류", t);
                    Toast.makeText(ContentDetailActivity.this, "공유에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ContentDetailActivity", "Error sharing content", e);
            Toast.makeText(this, "공유 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}