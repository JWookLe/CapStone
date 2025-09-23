package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class EmotionSurveyActivity extends AppCompatActivity {
    private static final String TAG = "EmotionSurveyActivity";
    private String selectedEmotion;
    private RadioGroup rgQuestion1, rgQuestion2, rgQuestion3, rgQuestion4;
    private EditText etQuestion5;
    private Button btnSubmit;
    private ApiService apiService;
    private String userId;
    private String jwtToken;
    private long recordId; // 감정 기록 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_survey);

        // Intent에서 감정 정보와 기록 ID 받아오기
        selectedEmotion = getIntent().getStringExtra("emotion");
        recordId = getIntent().getLongExtra("recordId", 0);

        Log.d(TAG, "받은 감정 유형: " + selectedEmotion);
        Log.d(TAG, "받은 감정 기록 ID: " + recordId);

        if (selectedEmotion == null) {
            selectedEmotion = "기쁨"; // 기본값
        }

        // recordId가 0이면 SharedPreferences에서 가져오기 시도
        if (recordId == 0) {
            SharedPreferences prefs = getSharedPreferences("emotion_sync_prefs", MODE_PRIVATE);
            recordId = prefs.getLong("latest_emotion_record_id", 1); // 기본값은 1
            Log.d(TAG, "SharedPreferences에서 가져온 감정 기록 ID: " + recordId);
        }

        // Retrofit 초기화
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // 에뮬레이터에서 로컬 서버 연결
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 토큰 가져오기
        getUserToken();

        // UI 초기화
        initializeViews();
        setupSurvey();
    }

    private void getUserToken() {
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

            jwtToken = securePrefs.getString("jwt_token", null);
            if (jwtToken != null) {
                userId = getUserIdFromToken(jwtToken);
                Log.d(TAG, "토큰에서 추출한 사용자 ID: " + userId);
            }

            if (jwtToken == null || userId == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "인증 정보 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private String getUserIdFromToken(String token) {
        try {
            // JWT 토큰의 시크릿 키 (서버와 동일한 키 사용해야 함)
            String secretKey = "my-super-secure-jwt-secret-key-2025-very-long-and-random";

            // JWT 토큰 파싱
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰의 subject에서 사용자 ID 가져오기
            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeViews() {
        // 감정 타입 텍스트뷰 설정
        TextView tvEmotionType = findViewById(R.id.tvEmotionType);
        tvEmotionType.setText(selectedEmotion + " 설문");

        // 라디오 그룹 초기화
        rgQuestion1 = findViewById(R.id.rgQuestion1);
        rgQuestion2 = findViewById(R.id.rgQuestion2);
        rgQuestion3 = findViewById(R.id.rgQuestion3);
        rgQuestion4 = findViewById(R.id.rgQuestion4);

        // 텍스트 입력 필드 초기화
        etQuestion5 = findViewById(R.id.etQuestion5);

        // 제출 버튼 초기화
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> submitSurvey());
    }

    private void setupSurvey() {
        // 질문 텍스트 설정
        TextView tvQuestion1 = findViewById(R.id.tvQuestion1);
        TextView tvQuestion2 = findViewById(R.id.tvQuestion2);
        TextView tvQuestion3 = findViewById(R.id.tvQuestion3);
        TextView tvQuestion4 = findViewById(R.id.tvQuestion4);
        TextView tvQuestion5 = findViewById(R.id.tvQuestion5);

        // 감정에 따른 질문 설정
        switch (selectedEmotion) {
            case "슬픔":
                tvQuestion1.setText("1. 오늘 얼마나 슬픈가요?");
                tvQuestion2.setText("2. 슬픔이 얼마나 오래 지속되었나요?");
                tvQuestion3.setText("3. 슬픔이 일상생활에 얼마나 영향을 미쳤나요?");
                tvQuestion4.setText("4. 슬픔을 해소하기 위한 노력을 했나요?");
                tvQuestion5.setText("5. 슬픔의 구체적인 원인이 무엇인가요?");
                etQuestion5.setHint("슬픔의 원인을 자유롭게 작성해 주세요");
                break;
            case "기쁨":
                tvQuestion1.setText("1. 오늘 얼마나 행복하신가요?");
                tvQuestion2.setText("2. 기쁨이 얼마나 오래 지속되었나요?");
                tvQuestion3.setText("3. 기쁨이 일상생활에 얼마나 영향을 미쳤나요?");
                tvQuestion4.setText("4. 기쁨을 나누기 위한 노력을 했나요?");
                tvQuestion5.setText("5. 기쁨의 구체적인 원인이 무엇인가요?");
                etQuestion5.setHint("기쁨의 원인을 자유롭게 작성해 주세요");
                break;
            case "화남":
                tvQuestion1.setText("1. 오늘 얼마나 화가 나나요?");
                tvQuestion2.setText("2. 화가 얼마나 오래 지속되었나요?");
                tvQuestion3.setText("3. 화가 일상생활에 얼마나 영향을 미쳤나요?");
                tvQuestion4.setText("4. 화를 해소하기 위한 노력을 했나요?");
                tvQuestion5.setText("5. 화의 구체적인 원인이 무엇인가요?");
                etQuestion5.setHint("화가 난 원인을 자유롭게 작성해 주세요");
                break;
            case "불안":
                tvQuestion1.setText("1. 오늘 얼마나 불안하신가요?");
                tvQuestion2.setText("2. 불안이 얼마나 오래 지속되었나요?");
                tvQuestion3.setText("3. 불안이 일상생활에 얼마나 영향을 미쳤나요?");
                tvQuestion4.setText("4. 불안을 해소하기 위한 노력을 했나요?");
                tvQuestion5.setText("5. 불안의 구체적인 원인이 무엇인가요?");
                etQuestion5.setHint("불안의 원인을 자유롭게 작성해 주세요");
                break;
        }
    }

    private void submitSurvey() {
        try {
            // 모든 질문에 답변했는지 확인
            if (rgQuestion1.getCheckedRadioButtonId() == -1 ||
                    rgQuestion2.getCheckedRadioButtonId() == -1 ||
                    rgQuestion3.getCheckedRadioButtonId() == -1 ||
                    rgQuestion4.getCheckedRadioButtonId() == -1 ||
                    etQuestion5.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "모든 질문에 답변해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 설문 결과 수집
            int responseQ1 = getResponseValue(rgQuestion1);
            int responseQ2 = getResponseValue(rgQuestion2);
            int responseQ3 = getResponseValue(rgQuestion3);
            int responseQ4 = getResponseValue(rgQuestion4);
            String responseQ5 = etQuestion5.getText().toString().trim();

            // 설문 결과 서버에 저장
            saveEmotionSurvey(responseQ1, responseQ2, responseQ3, responseQ4, responseQ5);

            // ContentRecommendationActivity로 이동
            Intent intent = new Intent(EmotionSurveyActivity.this, ContentRecommendationActivity.class);
            intent.putExtra("emotion", selectedEmotion);
            intent.putExtra("responseQ1", responseQ1);
            intent.putExtra("responseQ2", responseQ2);
            intent.putExtra("responseQ3", responseQ3);
            intent.putExtra("responseQ4", responseQ4);
            intent.putExtra("responseQ5Text", responseQ5);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "설문 제출 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveEmotionSurvey(int q1, int q2, int q3, int q4, String q5) {
        Log.d(TAG, "설문 저장 시작: 기록ID=" + recordId);

        // 설문 데이터 준비
        Map<String, Object> surveyData = new HashMap<>();
        surveyData.put("userId", userId);
        surveyData.put("recordId", recordId);
        surveyData.put("question1", q1);
        surveyData.put("question2", q2);
        surveyData.put("question3", q3);
        surveyData.put("question4", q4);
        surveyData.put("question5", q5);

        Log.d(TAG, "설문 데이터: " + surveyData.toString());

        // API 호출
        Call<Map<String, Object>> call = apiService.recordEmotionSurvey(
                "Bearer " + jwtToken,
                surveyData
        );

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    boolean success = (boolean) data.getOrDefault("success", false);

                    Log.d(TAG, "설문 저장 응답: " + data.toString());

                    if (success) {
                        Log.d(TAG, "설문 저장 성공");
                        Toast.makeText(EmotionSurveyActivity.this,
                                "설문이 성공적으로 기록되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "설문 저장 실패: " + data.getOrDefault("message", "알 수 없는 오류"));
                        Toast.makeText(EmotionSurveyActivity.this,
                                "설문 기록에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "서버 오류: " + response.code() + ", 내용: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }

                    Toast.makeText(EmotionSurveyActivity.this,
                            "서버 연결에 실패했습니다: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "네트워크 오류", t);
                Toast.makeText(EmotionSurveyActivity.this,
                        "서버 연결에 실패했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getResponseValue(RadioGroup radioGroup) {
        int checkedId = radioGroup.getCheckedRadioButtonId();

        if (checkedId == R.id.rbQ1Option1 || checkedId == R.id.rbQ2Option1 ||
                checkedId == R.id.rbQ3Option1 || checkedId == R.id.rbQ4Option1) {
            return 1;
        } else if (checkedId == R.id.rbQ1Option2 || checkedId == R.id.rbQ2Option2 ||
                checkedId == R.id.rbQ3Option2 || checkedId == R.id.rbQ4Option2) {
            return 2;
        } else if (checkedId == R.id.rbQ1Option3 || checkedId == R.id.rbQ2Option3 ||
                checkedId == R.id.rbQ3Option3 || checkedId == R.id.rbQ4Option3) {
            return 3;
        } else if (checkedId == R.id.rbQ1Option4 || checkedId == R.id.rbQ2Option4 ||
                checkedId == R.id.rbQ3Option4 || checkedId == R.id.rbQ4Option4) {
            return 4;
        } else if (checkedId == R.id.rbQ1Option5 || checkedId == R.id.rbQ2Option5 ||
                checkedId == R.id.rbQ3Option5 || checkedId == R.id.rbQ4Option5) {
            return 5;
        }

        return 3; // 기본값
    }
}