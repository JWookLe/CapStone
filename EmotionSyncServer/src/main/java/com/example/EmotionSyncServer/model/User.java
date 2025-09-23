package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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



    private String inviteCode;
    public String getInviteCode() {
        return inviteCode;
    }
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
    public User() {
    }

    public User(String id, String email, String password, String name, String phone, String provider) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.provider = provider;
        this.inviteCode = generateInviteCode();  // 초대 코드 생성
    }

    private String generateInviteCode() {
        // 6자리 랜덤 문자열 생성
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public void addFriend(User friend) {
        if (friend == null) {
            throw new IllegalArgumentException("친구는 null일 수 없습니다.");
        }
        if (this.equals(friend)) {
            throw new IllegalArgumentException("자기 자신을 친구로 추가할 수 없습니다.");
        }
        if (!friends.contains(friend)) {
            friends.add(friend);
            friend.getFriends().add(this);
        }
    }

    public void removeFriend(User friend) {
        if (friend != null) {
            friends.remove(friend);
            friend.getFriends().remove(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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