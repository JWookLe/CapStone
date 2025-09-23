package com.example.EmotionSync;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

// 회원가입 액티비티 정의
public class JoinActivity extends AppCompatActivity {

    private static final String TAG = "JoinActivity";  // 디버깅을 위한 태그 설정
    private ApiService apiService;  // Retrofit API 서비스 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // 부모 클래스(AppCompatActivity)의 onCreate 호출
        setContentView(R.layout.activity_join);  // XML 레이아웃 파일(activity_join.xml)을 UI로 설정

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로 가기 버튼 활성화
            getSupportActionBar().setDisplayShowTitleEnabled(false);  // 타이틀 비활성화
        }

        apiService = RetrofitClient.getApiService();  // Retrofit API 서비스 객체 초기화

        // UI 요소들을 Java 코드에서 사용하기 위해 ID를 이용해 연결
        EditText editName = findViewById(R.id.editName);  // 사용자 이름 입력 필드
        EditText editId = findViewById(R.id.editId);  // 사용자 아이디 입력 필드
        EditText editPassword = findViewById(R.id.editPassword);  // 비밀번호 입력 필드
        EditText editPasswordCheck = findViewById(R.id.editPasswordCheck);  // 비밀번호 확인 입력 필드
        EditText editPhone1 = findViewById(R.id.editPhone1);  // 전화번호 첫번째 입력 필드
        EditText editPhone2 = findViewById(R.id.editPhone2);  // 전화번호 두번째 입력 필드
        EditText editPhone3 = findViewById(R.id.editPhone3);  // 전화번호 세번째 입력 필드
        Button buttonRegister = findViewById(R.id.buttonRegister);  // 회원가입 버튼

        // 한글 입력을 지원하기 위한 설정
        editName.setImeOptions(EditorInfo.IME_ACTION_NEXT);  // 다음 입력 필드로 이동하도록 설정
        // 한글 입력을 위한 설정: TYPE_CLASS_TEXT만 사용하고 다른 플래그는 적용하지 않음
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        // 한글 자모 결합을 위한 추가 설정
        editName.setPrivateImeOptions("defaultInputmode=korean");

        editId.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editPassword.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editPasswordCheck.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editPhone1.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editPhone2.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editPhone3.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // 회원가입 버튼 클릭 이벤트 설정
        buttonRegister.setOnClickListener(v -> {
            String name = editName.getText().toString();  // 입력된 이름 가져오기
            String id = editId.getText().toString();  // 입력된 아이디 가져오기
            String password = editPassword.getText().toString();  // 입력된 비밀번호 가져오기
            String passwordCheck = editPasswordCheck.getText().toString();  // 입력된 비밀번호 확인 값 가져오기
            String phone1 = editPhone1.getText().toString();  // 입력된 전화번호 첫번째 가져오기
            String phone2 = editPhone2.getText().toString();  // 입력된 전화번호 두번째 가져오기
            String phone3 = editPhone3.getText().toString();  // 입력된 전화번호 세번째 가져오기

            // 입력값 검증
            if(name.isEmpty() || id.isEmpty() || password.isEmpty() || passwordCheck.isEmpty() ||
                    phone1.isEmpty() || phone2.isEmpty() || phone3.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();  // 모든 정보 입력 요청
                return;  // 회원가입 절차 중단
            }
            if (password.length() < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;  // 회원가입 절차 중단
            }
            if (!password.equals(passwordCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();  // 비밀번호 불일치 메시지 출력
                return;  // 회원가입 절차 중단
            }

            // 전화번호 형식 만들기: "010-1234-5678"
            String formattedPhone = phone1 + "-" + phone2 + "-" + phone3;

            // 사용자 정보를 Map에 저장하여 서버로 보낼 준비
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("id", id);
            userInfo.put("password", password);
            userInfo.put("name", name);
            userInfo.put("phone", formattedPhone);

            // Retrofit을 이용하여 서버에 회원가입 요청 전송
            Call<Map<String, Object>> call = apiService.registerUser(userInfo);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {  // HTTP 응답이 성공했을 경우
                        Map<String, Object> result = response.body();  // 서버에서 반환한 JSON 데이터를 Map으로 변환하여 가져옴
                        if (result != null && (Boolean) result.get("success")) {  // 회원가입 성공 여부 확인
                            Toast.makeText(JoinActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));  // 로그인 화면으로 이동
                            finish();  // 현재 액티비티 종료
                        } else {
                            Toast.makeText(JoinActivity.this, result != null ? (String) result.get("message") : "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(JoinActivity.this, "이미 등록되어 있는 아이디입니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {  // 네트워크 오류 또는 서버 다운 등 실패 상황
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(JoinActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}