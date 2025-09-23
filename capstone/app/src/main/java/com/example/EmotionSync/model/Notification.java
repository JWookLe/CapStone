package com.example.EmotionSync.model;

public class Notification {
    private String id;
    private String type;       // 예: "친구 요청", "메시지", "공유"

    private String content;    // 예: "민경빈님이 친구 요청을 보냈습니다"
    private String senderId;     // 요청을 보낸 사람의 ID

    // 기본 생성자
    public Notification() {}

    // 모든 필드를 포함하는 생성자
    public Notification(String id, String type, String content, String senderId) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.senderId = senderId;
    }

    // Getter
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return content;
    }

    public String getSenderId() {
        return senderId;
    }

    // Setter
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String content) {
        this.content = content;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
