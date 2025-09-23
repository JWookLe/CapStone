package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePassword";
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnChangePassword;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("비밀번호 변경");

        // 뷰 초기화
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // API 서비스 초기화
        apiService = RetrofitClient.getApiService();

        // 토큰 가져오기
        getJwtToken();

        // 버튼 클릭 이벤트 설정
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void getJwtToken() {
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

            jwtToken = securePrefs.getString("jwt_token", "");
            Log.d(TAG, "JWT 토큰 로드됨: " + (TextUtils.isEmpty(jwtToken) ? "없음" : "있음"));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "토큰 가져오기 실패: " + e.getMessage());
            Toast.makeText(this, "토큰 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            jwtToken = "";
        }
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // 유효성 검사 로그
        Log.d(TAG, "새 비밀번호: " + newPassword);
        Log.d(TAG, "새 비밀번호 확인: " + confirmNewPassword);

        // 유효성 검사
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("새 비밀번호를 입력해주세요");
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("비밀번호는 최소 6자리 이상이어야 합니다");
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            etConfirmNewPassword.setError("비밀번호 확인을 입력해주세요");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 토큰 확인
        Log.d(TAG, "JWT 토큰: " + (jwtToken != null ? "있음" : "없음"));
        if (TextUtils.isEmpty(jwtToken)) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 변경 요청
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("newPassword", newPassword);

        Log.d(TAG, "비밀번호 변경 요청 시작: " + passwordData.toString());

        Call<Map<String, Object>> call = apiService.changePassword(
                "Bearer " + jwtToken,
                passwordData
        );

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "onResponse 호출됨");
                Log.d(TAG, "응답 코드: " + response.code());
                Log.d(TAG, "응답 성공 여부: " + response.isSuccessful());

                if (response.body() != null) {
                    Log.d(TAG, "응답 본문: " + response.body().toString());
                }

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    boolean success = false;

                    // success 값이 있는지 확인하고 가져오기
                    if (data.containsKey("success")) {
                        success = (boolean) data.get("success");
                    }

                    Log.d(TAG, "응답 success 값: " + success);

                    if (success) {
                        Toast.makeText(
                                ChangePasswordActivity.this,
                                "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.",
                                Toast.LENGTH_LONG
                        ).show();

                        // 잠시 대기 후 로그아웃 처리 (토스트 메시지가 보이도록)
                        new Handler().postDelayed(() -> logoutAndRedirectToLogin(), 1000);
                    } else {
                        String message = (String) data.getOrDefault("message", "비밀번호 변경에 실패했습니다");
                        Log.d(TAG, "실패 메시지: " + message);
                        Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "에러 본문: " + errorBody);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "서버 오류: " + response.code());
                    Toast.makeText(
                            ChangePasswordActivity.this,
                            "서버 오류: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "API 호출 실패", t);
                Toast.makeText(
                        ChangePasswordActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // 로그아웃 및 로그인 화면으로 이동 메서드 분리
    private void logoutAndRedirectToLogin() {
        Log.d(TAG, "logoutAndRedirectToLogin 메서드 시작");
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

            // 토큰 제거
            securePrefs.edit().remove("jwt_token").commit(); // apply()가 아닌 commit() 사용하여 즉시 적용
            Log.d(TAG, "JWT 토큰 삭제 완료");

            // 로그인 화면으로 이동
            Log.d(TAG, "로그인 화면으로 이동 시작");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Log.d(TAG, "startActivity 호출 완료");

            // 현재 액티비티 종료
            finish();
            Log.d(TAG, "finish() 호출 완료");
        } catch (Exception e) {
            Log.e(TAG, "로그아웃 처리 중 예외 발생", e);
            e.printStackTrace();
            Toast.makeText(this, "로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // 예외가 발생해도 메인 화면으로 이동 시도
            try {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "메인 화면 이동 중 2차 예외 발생", ex);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}