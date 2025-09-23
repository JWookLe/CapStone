package com.example.EmotionSync.websocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.EmotionSync.database.AppDatabase;
import com.example.EmotionSync.database.ChatMessageEntity;
import com.example.EmotionSync.model.ChatMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";

    private WebSocket webSocket;
    private final OkHttpClient client;
    private final ExecutorService executorService;
    private final AppDatabase database;
    private String currentUserId;
    private final Gson gson;
    private WebSocketListener listener;
    private final Context context;

    public WebSocketManager(AppDatabase database, Context context) {
        this.database = database;
        this.client = new OkHttpClient();
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
        this.context = context;
    }

    private String normalizeUserId(String rawId) {
        if (rawId == null) return null;

        // 카카오 계정이라면 숫자만 있는 경우에만 정규화
        if (rawId.matches("^\\d{5,}$")) {
            return "kakao_" + rawId + "@kakao.local";
        }

        // 이미 이메일 형식이거나 로컬 사용자명일 경우 그대로 반환
        return rawId;
    }

    public void connect(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.e(TAG, "userId가 null이거나 비어있습니다.");
            return;
        }

        this.currentUserId = userId;
        try {
            this.currentUserId = normalizeUserId(userId);
            if (this.currentUserId == null) {
                Log.e(TAG, "userId 정규화 실패");
                return;
            }
            
            String WS_URL = "ws://10.0.2.2:8080/ws/chat?userId=" + URLEncoder.encode(this.currentUserId,"UTF-8");
            Log.d(TAG, "📡 Connecting to WebSocket URL: " + WS_URL);

            // JWT 토큰 가져오기
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String token = prefs.getString("jwt_token", null);
            if (token == null) {
                Log.e(TAG, "JWT 토큰을 찾을 수 없습니다.");
                return;
            }

            Request request = new Request.Builder()
                    .url(WS_URL)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    Log.d(TAG, "WebSocket 연결됨");
                    if (listener != null) {
                        listener.onOpen(webSocket, response);
                    }
                }

                //@Override
                /*public void onMessage(WebSocket webSocket, String text) {
                    Log.d(TAG, "메시지 수신: " + text);
                    if (listener != null) {
                        listener.onMessage(webSocket, text);
                    }
                }*/
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.d(TAG, "📩 WebSocket 메시지 수신: " + text);

                    try {
                        ChatMessage receivedMessage = gson.fromJson(text, ChatMessage.class);

                        String normalizedSender = normalizeUserId(receivedMessage.getSenderId());
                        String normalizedMe = normalizeUserId(currentUserId);

                        Log.d(TAG, "✅ senderId = " + receivedMessage.getSenderId());
                        Log.d(TAG, "✅ currentUserId = " + currentUserId);
                        Log.d(TAG, "✅ 비교 결과: " + normalizedSender.equals(normalizedMe));

                        // ✅ 내가 보낸 메시지라면 무시
                        if (normalizedSender.equals(normalizedMe)) {
                            Log.d(TAG, "🔁 내가 보낸 메시지 → UI/DB에 반영 안 함");
                            return;
                        }

                        // ✅ DB 저장
                        saveMessageLocally(receivedMessage);

                        // ✅ UI 반영 (listener가 연결돼 있으면 넘겨줌)
                        if (listener != null) {
                            listener.onMessage(webSocket, text);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ WebSocket 메시지 파싱 실패", e);
                    }
                }
                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    Log.d(TAG, "바이너리 메시지 수신");
                    if (listener != null) {
                        listener.onMessage(webSocket, bytes);
                    }
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    Log.d(TAG, "WebSocket 연결 종료 중: " + reason);
                    if (listener != null) {
                        listener.onClosing(webSocket, code, reason);
                    }
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.d(TAG, "WebSocket 연결 종료됨: " + reason);
                    if (listener != null) {
                        listener.onClosed(webSocket, code, reason);
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e(TAG, "WebSocket 오류: " + t.getMessage());
                    if (listener != null) {
                        listener.onFailure(webSocket, t, response);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "WebSocket connection error", e);
        }
    }

    private void saveMessageLocally(ChatMessage message) {
        executorService.execute(() -> {
            Log.d(TAG, "✅ DB 저장 시도 - messageId: " + message.getMessageId());
            ChatMessageEntity entity = new ChatMessageEntity(
                    message.getMessageId(),
                    normalizeUserId(message.getSenderId()),
                    normalizeUserId(message.getReceiverId()),
                    message.getContent(),
                    message.getTimestamp(),
                    message.isRead()
            );
            try {
                database.chatMessageDao().insert(entity);
            } catch (Exception e) {
                Log.e(TAG, "❌ DB insert 실패 - messageId: " + message.getMessageId(), e);
            }
        });
    }

    public void sendMessage(ChatMessage message) {
        if (webSocket == null) {
            Log.e(TAG, "WebSocket is not connected");
            return;
        }

        try {
            String senderId = normalizeUserId(message.getSenderId());
            String receiverId = normalizeUserId(message.getReceiverId());

            JSONObject json = new JSONObject();
            json.put("messageId", message.getMessageId());
            json.put("senderId", senderId);
            json.put("receiverId", receiverId);
            json.put("content", message.getContent());
            json.put("timestamp", message.getTimestamp());
            json.put("isRead", message.isRead());

            webSocket.send(json.toString());

            saveMessageLocally(new ChatMessage(
                    message.getMessageId(),
                    senderId,
                    receiverId,
                    message.getContent(),
                    message.getTimestamp(),
                    true,
                    message.isRead()
            ));

        } catch (JSONException e) {
            Log.e(TAG, "Error creating message JSON", e);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "정상 종료");
        }
        executorService.shutdown();
    }
}
