package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;  // 이건 그대로 냅둬. 자동 생성 아님

    private String email;       // 추가
    private String password;
    private String name;
    private String phone;
    private String provider;    // 추가

    public User() {
    }

    public User(String id, String email, String password, String name, String phone, String provider) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.provider = provider;
    }

    // Getter / Setter
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getProvider() { return provider; }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProvider(String provider) { this.provider = provider; }
}
