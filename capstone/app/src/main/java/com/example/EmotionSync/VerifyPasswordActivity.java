package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyPasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword;
    private EditText etConfirmPassword;
    private Button btnVerify;
    private ApiService apiService;
    private String jwtToken;
    private String purpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_password);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("비밀번호 확인");

        // 뷰 초기화
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnVerify = findViewById(R.id.btnVerify);

        // API 서비스 초기화
        apiService = RetrofitClient.getApiService();

        // 토큰 가져오기
        getJwtToken();

        // 목적값 받기
        purpose = getIntent().getStringExtra("purpose");

        // 버튼 클릭 이벤트 설정
        btnVerify.setOnClickListener(v -> verifyPassword());
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
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "토큰 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            jwtToken = "";
        }
    }

    private void verifyPassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("현재 비밀번호를 입력해주세요");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("비밀번호 확인을 입력해주세요");
            return;
        }

        if (!currentPassword.equals(confirmPassword)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(jwtToken)) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("password", currentPassword);

        Call<Map<String, Object>> call = apiService.verifyPassword(
                "Bearer " + jwtToken,
                passwordData
        );

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = (boolean) response.body().getOrDefault("success", false);
                    if (success) {
                        if ("change_password".equals(purpose)) {
                            startActivity(new Intent(VerifyPasswordActivity.this, ChangePasswordActivity.class));
                        } else if ("view_info".equals(purpose)) {
                            startActivity(new Intent(VerifyPasswordActivity.this, UserDetailsActivity.class));
                        } else if ("delete_account".equals(purpose)) {
                            startActivity(new Intent(VerifyPasswordActivity.this, DeleteReasonActivity.class));
                        }
                        finish();
                    } else {
                        String message = (String) response.body().getOrDefault("message", "비밀번호가 일치하지 않습니다");
                        Toast.makeText(VerifyPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JSONObject errorObj = new JSONObject(errorJson);
                            String errorMessage = errorObj.optString("message", "서버 오류가 발생했습니다.");
                            Toast.makeText(VerifyPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyPasswordActivity.this, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(VerifyPasswordActivity.this, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(VerifyPasswordActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
