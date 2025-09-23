package com.example.EmotionSync;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvProvider;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        TextView tvInviteCode = findViewById(R.id.tvInviteCode);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("내 정보 조회");

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvProvider = findViewById(R.id.tvProvider);

        Button btnGenerateInviteCode = findViewById(R.id.btnGenerateInviteCode);
        btnGenerateInviteCode.setOnClickListener(v -> {
            Call<String> call = apiService.createInviteCode("Bearer " + jwtToken);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    if (response.isSuccessful() && response.body() != null) {


                        String inviteCode = response.body();
                        Log.d("InviteCode", "setText() 실행됨: 초대코드: " + inviteCode);
                        tvInviteCode.setText("초대코드: " + inviteCode);

                        new androidx.appcompat.app.AlertDialog.Builder(UserDetailsActivity.this)
                                .setTitle("초대코드")
                                .setMessage("친구에게 공유할 코드: " + inviteCode)
                                .setPositiveButton("확인", null)
                                .show();
                    } else {
                        Toast.makeText(UserDetailsActivity.this, "초대코드 생성 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("InviteCode", "네트워크 실패 이유: " + t.getMessage());
                    Toast.makeText(UserDetailsActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });

        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            jwtToken = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString("jwt_token", "");

            Log.d("InviteCode", "JWT 토큰: " + jwtToken);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            jwtToken = "";
        }

        apiService = RetrofitClient.getApiService();
        fetchUserInfo();
    }

    private void fetchUserInfo() {
        Call<Map<String, Object>> call = apiService.getUserInfo("Bearer " + jwtToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    tvName.setText("이름: " + getOrDefault(data.get("name")));
                    tvEmail.setText("이메일: " + getOrDefault(data.get("email")));
                    tvPhone.setText("전화번호: " + getOrDefault(data.get("phone")));
                    tvProvider.setText("계정 유형: " + getOrDefault(data.get("provider")));
                } else {
                    Toast.makeText(UserDetailsActivity.this, "정보 조회 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(UserDetailsActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getOrDefault(Object value) {
        if (value == null || String.valueOf(value).equalsIgnoreCase("null")) {
            return "없음";
        } else {
            return String.valueOf(value);
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
