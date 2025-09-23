package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

public class DeleteReasonActivity extends AppCompatActivity {

    private Spinner spinnerReasons;
    private Button btnDelete, btnCancel;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_reason);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("회원 탈퇴 사유");

        spinnerReasons = findViewById(R.id.spinnerReasons);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);

        String[] reasons = {
                "앱이 마음에 들지 않음",
                "기능 부족",
                "사용 빈도 낮음",
                "개인정보 우려",
                "UI/UX 불편",
                "버그나 오류가 많음",
                "광고가 많음",
                "고객지원 불만족",
                "다른 서비스 이용 예정",
                "기타"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons);
        spinnerReasons.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();
        getJwtToken();

        btnDelete.setOnClickListener(v -> confirmFinalDeletion());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void getJwtToken() {
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

            jwtToken = securePrefs.getString("jwt_token", "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            jwtToken = "";
        }
    }

    private void confirmFinalDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("정말 탈퇴하시겠습니까?")
                .setMessage("탈퇴 후에는 계정을 복구할 수 없습니다.")
                .setPositiveButton("그래도 탈퇴", (dialog, which) -> executeDeleteAccount())
                .setNegativeButton("취소", null)
                .show();
    }

    private void executeDeleteAccount() {
        String selectedReason = spinnerReasons.getSelectedItem().toString();

        Map<String, String> body = new HashMap<>();
        body.put("reason", selectedReason);

        Call<Map<String, Object>> call = apiService.deleteAccount("Bearer " + jwtToken, body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && (Boolean) response.body().get("success")) {
                    clearPrefsAndLogout();
                } else {
                    Toast.makeText(DeleteReasonActivity.this, "탈퇴 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DeleteReasonActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearPrefsAndLogout() {
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

            securePrefs.edit().clear().apply();
            Toast.makeText(this, "정상적으로 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(DeleteReasonActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
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
