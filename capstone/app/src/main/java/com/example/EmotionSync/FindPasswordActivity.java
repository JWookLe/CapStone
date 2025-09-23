package com.example.EmotionSync;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPasswordActivity extends AppCompatActivity {

    private static final String TAG = "FindPasswordActivity";
    private ApiService apiService;
    private EditText editId, editName, editVerificationCode;
    private EditText editPhone1, editPhone2, editPhone3;
    private Button buttonSendVerification, buttonVerify;
    private TextView textTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        apiService = RetrofitClient.getApiService();

        // UI 요소 연결
        editId = findViewById(R.id.editId);
        editName = findViewById(R.id.editName);
        editPhone1 = findViewById(R.id.editPhone1);
        editPhone2 = findViewById(R.id.editPhone2);
        editPhone3 = findViewById(R.id.editPhone3);
        editVerificationCode = findViewById(R.id.editVerificationCode);
        buttonSendVerification = findViewById(R.id.buttonSendVerification);
        buttonVerify = findViewById(R.id.buttonVerify);
        textTimer = findViewById(R.id.textTimer);

        // 인증번호 전송 버튼 클릭 이벤트
        buttonSendVerification.setOnClickListener(v -> {
            String id = editId.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String phone1 = editPhone1.getText().toString().trim();
            String phone2 = editPhone2.getText().toString().trim();
            String phone3 = editPhone3.getText().toString().trim();

            if (id.isEmpty() || name.isEmpty() || phone1.isEmpty() || phone2.isEmpty() || phone3.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPhone = phone1 + "-" + phone2 + "-" + phone3;

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("id", id);
            userInfo.put("name", name);
            userInfo.put("phone", formattedPhone);

            Call<Map<String, Object>> call = apiService.sendVerificationForFindPassword(userInfo);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> result = response.body();
                        if (result != null && (Boolean) result.get("success")) {
                            Toast.makeText(FindPasswordActivity.this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                            buttonSendVerification.setText("인증번호 재전송");

                            // 타이머 시작
                            new CountDownTimer(180000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    long minutes = millisUntilFinished / 60000;
                                    long seconds = (millisUntilFinished % 60000) / 1000;
                                    String time = String.format("유효시간: %d:%02d", minutes, seconds);
                                    textTimer.setText(time);
                                    textTimer.setVisibility(View.VISIBLE);
                                }

                                public void onFinish() {
                                    textTimer.setText("인증시간이 만료되었습니다.");
                                }
                            }.start();
                        } else {
                            Toast.makeText(FindPasswordActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FindPasswordActivity.this, "인증번호 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(FindPasswordActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 인증 및 비밀번호 재설정 버튼 클릭 이벤트
        buttonVerify.setOnClickListener(v -> {
            String id = editId.getText().toString().trim();
            String code = editVerificationCode.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> verificationInfo = new HashMap<>();
            verificationInfo.put("id", id);
            verificationInfo.put("code", code);

            Call<Map<String, Object>> call = apiService.verifyForFindPassword(verificationInfo);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> result = response.body();
                        if (result != null && (Boolean) result.get("success")) {
                            Intent intent = new Intent(FindPasswordActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("userId", id);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(FindPasswordActivity.this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FindPasswordActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(FindPasswordActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
