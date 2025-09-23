package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public boolean registerUser(Map<String, String> userInfo) {
        String id = userInfo.get("id");
        String password = userInfo.get("password");
        String name = userInfo.get("name");
        String phone = userInfo.get("phone");

        // 필수 정보 확인
        if (id == null || password == null || name == null || phone == null) {
            return false;
        }

        // 이미 존재하는 사용자인지 확인
        if (userRepository.findById(id).isPresent()) {
            return false;
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(password);

        // 새 사용자 생성
        User user = new User(id, null, hashedPassword, name, phone, "LOCAL");
        userRepository.save(user);

        return true;
    }

    public boolean authenticateUser(String id, String password) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("입력된 비밀번호: " + password);
            System.out.println("DB 비밀번호: " + user.getPassword());
            System.out.println("비밀번호 매칭 결과: " + passwordEncoder.matches(password, user.getPassword()));
            return passwordEncoder.matches(password, user.getPassword());
        }

        return false;
    }

    /**
     * 이름과 전화번호로 사용자 존재 여부 확인
     */
    public boolean isUserExists(String name, String phone) {
        return userRepository.findByNameAndPhone(name, phone).isPresent();
    }

    /**
     * 아이디, 이름, 전화번호로 사용자 존재 여부 확인
     */
    public boolean isUserExists(String id, String name, String phone) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getName().equals(name) && user.getPhone().equals(phone);
        }

        return false;
    }

    /**
     * 이름과 전화번호로 사용자 아이디 찾기
     */
    public String findUserId(String name, String phone) {
        Optional<User> userOptional = userRepository.findByNameAndPhone(name, phone);

        return userOptional.map(User::getId).orElse(null);
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }


    /**
     * 비밀번호 재설정
     */
    @Transactional
    public boolean resetPassword(String id, String newPassword) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!"LOCAL".equals(user.getProvider())) {
                throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 재설정할 수 없습니다.");
            }
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * 사용자 비밀번호 확인
     * @param id 사용자 ID
     * @param password 확인할 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean verifyUserPassword(String id, String password) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!"LOCAL".equals(user.getProvider())) {
                throw new IllegalStateException("소셜 로그인 사용자는 비밀번호 확인이 불가능합니다.");
            }

            return passwordEncoder.matches(password, user.getPassword());
        }

        return false;
    }


    /**
     * 로그인한 사용자의 비밀번호 변경
     * @param id 사용자 ID
     * @param newPassword 새 비밀번호
     * @return 변경 성공 여부
     */
    @Transactional
    public boolean changePassword(String id, String newPassword) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!"LOCAL".equals(user.getProvider())) {
                throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            }
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            return true;
        }

        return false;
    }
}
