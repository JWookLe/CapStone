package com.example.EmotionSyncServer.dto;

public class UserDto {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String provider;
    private String inviteCode;

    public UserDto(String id, String email, String name, String phone, String provider, String inviteCode) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.provider = provider;
        this.inviteCode = inviteCode;
    }

    // Getter만 정의 (불변 객체처럼)
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getProvider() { return provider; }
    public String getInviteCode() { return inviteCode; }
}
