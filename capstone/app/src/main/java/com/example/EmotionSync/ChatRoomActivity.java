package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.database.AppDatabase;
import com.example.EmotionSync.database.ChatMessageEntity;
import com.example.EmotionSync.model.ChatMessage;
import com.example.EmotionSync.websocket.WebSocketManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private static final String TAG = "ChatRoomActivity";
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private String currentUserId;
    private String currentProvider;
    private String friendId;
    private String friendName;
    private String contentId;
    private String contentType;
    private String contentTitle;
    private String contentUrl;
    private WebSocketManager webSocketManager;
    private AppDatabase database;
    private SharedPreferences securePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 데이터베이스 초기화
        database = AppDatabase.getInstance(this);

        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        friendId = intent.getStringExtra("friendId");
        friendName = intent.getStringExtra("friendName");
        contentId = intent.getStringExtra("contentId");
        contentType = intent.getStringExtra("contentType");
        contentTitle = intent.getStringExtra("title");
        contentUrl = intent.getStringExtra("contentUrl");
        String deepLink = intent.getStringExtra("deepLink");

        Log.d(TAG, "Received content info - ID: " + contentId + ", Type: " + contentType + ", Title: " + contentTitle);
        Log.d(TAG, "Received URL: " + contentUrl);
        Log.d(TAG, "Received deep link: " + deepLink);

        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePreferences = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            currentUserId = securePreferences.getString("user_id", null);
            currentProvider = securePreferences.getString("provider", "local");
            
            Log.d(TAG, "현재 사용자 ID: " + currentUserId);
            Log.d(TAG, "현재 프로바이더: " + currentProvider);
            
            if (currentUserId == null) {
                Log.e(TAG, "사용자 ID를 찾을 수 없습니다.");
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 사용자 ID 정규화
            String normalizedCurrentUserId = normalizeUserId(currentUserId);
            String normalizedFriendId = normalizeUserId(friendId);

            // RecyclerView 설정
            recyclerView = findViewById(R.id.chatRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            chatAdapter = new ChatAdapter(this, recyclerView);
            recyclerView.setAdapter(chatAdapter);

            // 메시지 입력 필드와 전송 버튼 설정
            messageInput = findViewById(R.id.messageInput);
            sendButton = findViewById(R.id.sendButton);
            sendButton.setOnClickListener(v -> sendMessage());

            // 웹소켓 연결
            connectWebSocket(normalizedCurrentUserId, normalizedFriendId);

            // 기존 메시지 로드
            loadMessages();

            // 컨텐츠 정보가 있다면 자동으로 메시지 전송
            if (contentId != null && contentType != null && contentTitle != null && contentUrl != null) {
                String contentMessage = intent.getStringExtra("message");
                if (contentMessage != null) {
                    Log.d(TAG, "컨텐츠 공유 메시지 전송 시도 - 제목: " + contentTitle + ", URL: " + contentUrl);
                    
                    // 메시지 전송 전에 모든 정보가 올바르게 설정되었는지 확인
                    if (currentUserId != null && friendId != null) {
                        // 약간의 지연 후 메시지 전송
                        new Handler().postDelayed(() -> {
                            sendContentShareMessage();
                            Log.d(TAG, "컨텐츠 공유 메시지 전송 완료");
                        }, 500); // 0.5초 지연
                    } else {
                        Log.e(TAG, "사용자 ID 또는 친구 ID가 null입니다 - currentUserId: " + currentUserId + ", friendId: " + friendId);
                    }
                }
            } else {
                Log.e(TAG, "컨텐츠 정보가 불완전합니다 - ID: " + contentId + 
                          ", Type: " + contentType + 
                          ", Title: " + contentTitle + 
                          ", URL: " + contentUrl);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadMessages();   // ✅ 실시간 복귀 시에도 안 읽은 메시지 반영
    }

    private String normalizeUserId(String rawId) {
        if (rawId != null && rawId.matches("^\\d{5,}$") && !rawId.contains("@")) {
            return "kakao_" + rawId + "@kakao.local";
        }
        return rawId;
    }

    private void connectWebSocket(String currentUserId, String friendId) {
        try {
            if (currentProvider != null && "kakao".equals(currentProvider)) {
                if (!currentUserId.contains("@")) {
                    currentUserId = "kakao_" + currentUserId + "@kakao.local";
                }
            }
            if (friendId != null && friendId.matches("^\\d{5,}$")) {
                friendId = "kakao_" + friendId + "@kakao.local";
            }

            webSocketManager = new WebSocketManager(database, this);
            webSocketManager.connect(currentUserId);
        } catch (Exception e) {
            Log.e(TAG, "WebSocket 연결 중 오류 발생", e);
            Toast.makeText(this, "채팅 서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                String normalizedCurrentUserId = normalizeUserId(currentUserId);
                String normalizedFriendId = normalizeUserId(friendId);

                List<ChatMessageEntity> entities = database.chatMessageDao().getMessagesBetweenUsers(normalizedCurrentUserId, normalizedFriendId);
                List<ChatMessage> messages = entities.stream()
                        .map(entity -> new ChatMessage(
                                entity.getMessageId(),
                                entity.getSenderId(),
                                entity.getReceiverId(),
                                entity.getContent(),
                                entity.getTimestamp(),
                                entity.getSenderId().equals(normalizedCurrentUserId),
                                entity.isRead()
                        ))
                        .collect(Collectors.toList());

                if (!isFinishing()) {
                    runOnUiThread(() -> {
                        try {
                            chatAdapter.setMessages(messages);
                            if (!messages.isEmpty()) {
                                recyclerView.post(() -> {
                                    try {
                                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                                    } catch (Exception e) {
                                        Log.e(TAG, "스크롤 중 오류 발생", e);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "UI 업데이트 중 오류 발생", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "메시지 로딩 중 오류 발생", e);
                if (!isFinishing()) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatRoomActivity.this, "메시지 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void loadUnreadMessages() {
        new Thread(() -> {
            try {
                String normalizedCurrentUserId = normalizeUserId(currentUserId);
                String normalizedFriendId = normalizeUserId(friendId);

                List<ChatMessageEntity> unreadEntities = database.chatMessageDao().getUnreadMessages(normalizedCurrentUserId, normalizedFriendId)
                        .stream()
                        .filter(entity -> !entity.getSenderId().equals(normalizedCurrentUserId))  // 내가 보낸 건 무시
                        .collect(Collectors.toList());

                List<ChatMessage> unreadMessages = unreadEntities.stream()
                        .map(entity -> new ChatMessage(
                                entity.getMessageId(),
                                entity.getSenderId(),
                                entity.getReceiverId(),
                                entity.getContent(),
                                entity.getTimestamp(),
                                entity.getSenderId().equals(normalizedCurrentUserId),
                                true  // 읽음 처리됨으로 간주
                        ))
                        .collect(Collectors.toList());

                // UI에 메시지 추가
                runOnUiThread(() -> {
                    chatAdapter.addMessages(unreadMessages);  // 너가 만든 addMessages()가 없다면 setMessages()로 바꿔
                    recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                });

                // DB에 읽음 처리 반영
                for (ChatMessageEntity entity : unreadEntities) {
                    entity.setRead(true);
                }
                database.chatMessageDao().updateMessages(unreadEntities);

            } catch (Exception e) {
                Log.e(TAG, "안 읽은 메시지 로딩 중 오류 발생", e);
            }
        }).start();
    }

    private void sendMessage() {
        try {
            String content = messageInput.getText().toString().trim();
            if (!content.isEmpty()) {
                ChatMessage message = new ChatMessage(
                        UUID.randomUUID().toString(),
                        normalizeUserId(currentUserId),
                        normalizeUserId(friendId),
                        content,
                        System.currentTimeMillis(),
                        true,
                        false
                );

                // 메시지 전송 전에 UI에 먼저 추가
                runOnUiThread(() -> {
                    chatAdapter.addMessage(message);
                    recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                });

                // WebSocket을 통해 메시지 전송
                webSocketManager.sendMessage(message);
                messageInput.setText("");
            }
        } catch (Exception e) {
            Log.e(TAG, "메시지 전송 중 오류 발생", e);
            Toast.makeText(this, "메시지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendContentShareMessage() {
        String currentUserId = securePreferences.getString("user_id", null);
        String provider = securePreferences.getString("provider", null);
        
        if (currentUserId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 카카오 계정인 경우 ID 형식 수정
        String formattedUserId = currentUserId;
        if ("KAKAO".equals(provider)) {
            formattedUserId = "kakao_" + currentUserId + "@kakao.local";
        }

        // emotion-sync://content/{type}/{id} 형식의 URL 생성
        String displayUrl = String.format("emotion-sync://content/%s/%s", contentType, contentId);
        String messageContent = String.format("%s 컨텐츠를 공유했습니다.\n%s", 
            contentTitle, displayUrl);
        
        Log.d(TAG, "메시지 전송 시작 - 수신자: " + friendId);
        
        String jwtToken = securePreferences.getString("jwt_token", null);
        if (jwtToken == null) {
            Log.e(TAG, "JWT 토큰이 없습니다.");
            return;
        }
        
        Log.d(TAG, "사용할 JWT 토큰: " + jwtToken);
        
        String messageId = UUID.randomUUID().toString();
        Log.d(TAG, "메시지 생성 - ID: " + messageId + ", 발신자: " + formattedUserId + ", 수신자: " + friendId);
        
        String authHeader = "Bearer " + jwtToken;
        Log.d(TAG, "사용할 인증 헤더: " + authHeader);
        
        Log.d(TAG, "전송할 메시지 내용: " + messageContent);
        
        ChatMessage message = new ChatMessage(
            messageId,
            formattedUserId,
            friendId,
            messageContent,
            System.currentTimeMillis(),
            true,
            false
        );
        
        Log.d(TAG, "전송할 메시지 객체: " + message);

        // 메시지를 먼저 UI에 추가
        runOnUiThread(() -> {
            try {
                chatAdapter.addMessage(message);
                recyclerView.post(() -> {
                    try {
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    } catch (Exception e) {
                        Log.e(TAG, "스크롤 중 오류 발생", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "UI 업데이트 중 오류 발생", e);
            }
        });
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.sendChatMessage(authHeader, message).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "서버 응답 수신 - 코드: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "컨텐츠 공유 메시지 전송 완료");
                    runOnUiThread(() -> {
                        // 메시지를 로컬 DB에 저장
                        new Thread(() -> {
                            try {
                                ChatMessageEntity entity = new ChatMessageEntity(
                                    message.getMessageId(),
                                    message.getSenderId(),
                                    message.getReceiverId(),
                                    message.getContent(),
                                    message.getTimestamp(),
                                    message.isRead()
                                );
                                database.chatMessageDao().insert(entity);
                            } catch (Exception e) {
                                Log.e(TAG, "DB 저장 중 오류 발생", e);
                            }
                        }).start();
                        
                        Toast.makeText(ChatRoomActivity.this, 
                            "컨텐츠가 공유되었습니다.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e(TAG, "메시지 전송 실패 - 코드: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(ChatRoomActivity.this, 
                            "메시지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "메시지 전송 실패", t);
                runOnUiThread(() -> {
                    Toast.makeText(ChatRoomActivity.this, 
                        "메시지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            if (webSocketManager != null) {
                webSocketManager.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "WebSocket 연결 해제 중 오류 발생", e);
        }
        super.onDestroy();
    }
}