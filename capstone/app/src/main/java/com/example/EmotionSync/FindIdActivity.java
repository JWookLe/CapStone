package com.example.EmotionSync;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindIdActivity extends AppCompatActivity {

    private static final String TAG = "FindIdActivity";
    private ApiService apiService;

    private EditText editName, editVerificationCode;
    private EditText editPhone1, editPhone2, editPhone3;
    private Button buttonSendVerification, buttonVerify;
    private TextView textTimer;

    private String verificationCode;
    private CountDownTimer countDownTimer;
    private final long VERIFICATION_DURATION = 5 * 60 * 1000; // 5분

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        apiService = RetrofitClient.getApiService();

        editName = findViewById(R.id.editName);
        editPhone1 = findViewById(R.id.editPhone1);
        editPhone2 = findViewById(R.id.editPhone2);
        editPhone3 = findViewById(R.id.editPhone3);
        editVerificationCode = findViewById(R.id.editVerificationCode);
        buttonSendVerification = findViewById(R.id.buttonSendVerification);
        buttonVerify = findViewById(R.id.buttonVerify);
        textTimer = findViewById(R.id.textTimer);

        buttonSendVerification.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone1 = editPhone1.getText().toString().trim();
            String phone2 = editPhone2.getText().toString().trim();
            String phone3 = editPhone3.getText().toString().trim();

            if (name.isEmpty() || phone1.isEmpty() || phone2.isEmpty() || phone3.isEmpty()) {
                Toast.makeText(this, "이름과 전화번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPhone = phone1 + "-" + phone2 + "-" + phone3;

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", name);
            userInfo.put("phone", formattedPhone);

            Call<Map<String, Object>> call = apiService.sendVerificationForFindId(userInfo);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> result = response.body();
                        if (result != null && (Boolean) result.get("success")) {
                            Toast.makeText(FindIdActivity.this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                            buttonSendVerification.setText("인증번호 재전송");
                            startVerificationTimer();
                        } else {
                            Toast.makeText(FindIdActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FindIdActivity.this, "인증번호 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(FindIdActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonVerify.setOnClickListener(v -> {
            String code = editVerificationCode.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String phone1 = editPhone1.getText().toString().trim();
            String phone2 = editPhone2.getText().toString().trim();
            String phone3 = editPhone3.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPhone = phone1 + "-" + phone2 + "-" + phone3;

            Map<String, String> verificationInfo = new HashMap<>();
            verificationInfo.put("name", name);
            verificationInfo.put("phone", formattedPhone);
            verificationInfo.put("code", code);

            Call<Map<String, Object>> call = apiService.verifyAndFindId(verificationInfo);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> result = response.body();
                        if (result != null && (Boolean) result.get("success")) {
                            String userId = (String) result.get("userId");
                            showFoundIdDialog(userId);
                        } else {
                            Toast.makeText(FindIdActivity.this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FindIdActivity.this, "아이디 찾기 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(FindIdActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void startVerificationTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        textTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(VERIFICATION_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                textTimer.setText(String.format(Locale.getDefault(), "인증번호 유효시간: %02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                textTimer.setText("인증번호 유효시간이 만료되었습니다.");
            }
        }.start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showFoundIdDialog(String userId) {
        new AlertDialog.Builder(this)
                .setTitle("아이디 찾기 성공")
                .setMessage("회원님의 아이디는 " + userId + " 입니다.")
                .setPositiveButton("로그인 화면으로", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
