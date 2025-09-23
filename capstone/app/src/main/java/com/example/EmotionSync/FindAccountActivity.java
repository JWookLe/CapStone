package com.example.EmotionSync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class FindAccountActivity extends AppCompatActivity {

    private Button buttonFindId, buttonFindPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로 가기 버튼 활성화
            getSupportActionBar().setDisplayShowTitleEnabled(false);  // 타이틀 비활성화
        }

        // UI 요소 연결
        buttonFindId = findViewById(R.id.buttonFindId);
        buttonFindPassword = findViewById(R.id.buttonFindPassword);

        // 아이디 찾기 버튼 클릭 이벤트
        buttonFindId.setOnClickListener(v -> {
            Intent intent = new Intent(FindAccountActivity.this, FindIdActivity.class);
            startActivity(intent);
        });

        // 비밀번호 찾기 버튼 클릭 이벤트
        buttonFindPassword.setOnClickListener(v -> {
            Intent intent = new Intent(FindAccountActivity.this, FindPasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // 뒤로 가기 버튼 클릭 시 이전 화면으로 이동
        return true;
    }
} 