package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MyInfoActivity extends AppCompatActivity {

    private Button btnChangePassword;
    private Button btnViewInfo;
    private Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("내 정보");

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnViewInfo = findViewById(R.id.btnViewInfo);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

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

            String provider = securePrefs.getString("provider", "LOCAL"); // 기본값은 LOCAL로

            if (!"LOCAL".equals(provider)) {
                // 소셜 로그인 사용자는 비밀번호 변경 버튼 숨김
                btnChangePassword.setEnabled(false);
                btnChangePassword.setAlpha(0.5f);  // 살짝 투명하게
                Toast.makeText(this, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 로컬 사용자만 비밀번호 변경 가능
                btnChangePassword.setOnClickListener(v -> {
                    Intent intent = new Intent(MyInfoActivity.this, VerifyPasswordActivity.class);
                    intent.putExtra("purpose", "change_password");
                    startActivity(intent);
                });
            }

            // 내 정보 조회
            btnViewInfo.setOnClickListener(v -> {
                if (!"LOCAL".equals(provider)) {
                    // 소셜 로그인은 비밀번호 확인 없이 바로 정보 조회
                    Intent intent = new Intent(MyInfoActivity.this, UserDetailsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MyInfoActivity.this, VerifyPasswordActivity.class);
                    intent.putExtra("purpose", "view_info");
                    startActivity(intent);
                }
            });

            // 회원 탈퇴 버튼 클릭 시
            btnDeleteAccount.setOnClickListener(v -> {
                if (!"LOCAL".equals(provider)) {
                    // 소셜 로그인은 비밀번호 확인 없이 바로 탈퇴 이유 화면으로
                    Intent intent = new Intent(MyInfoActivity.this, DeleteReasonActivity.class);
                    startActivity(intent);
                } else {
                    // 로컬 사용자는 비밀번호 확인 먼저
                    Intent intent = new Intent(MyInfoActivity.this, VerifyPasswordActivity.class);
                    intent.putExtra("purpose", "delete_account");
                    startActivity(intent);
                }
            });



        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
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
