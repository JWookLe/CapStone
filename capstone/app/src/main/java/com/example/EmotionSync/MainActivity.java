package com.example.EmotionSync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;  // 디버그 모드 활성화
    private ApiService apiService;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private String currentUserId;
    private String currentProvider;
    private String currentProviderKey;
    EditText editTextId, editTextPassword;
    Button buttonLogin, buttonGoogleLogin, buttonKakaoLogin;
    TextView textForgotPassword, buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate 시작");
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "setContentView 완료");

        try {
            // UI 요소 초기화
            Log.d(TAG, "UI 초기화 시작");
            initializeViews();
            Log.d(TAG, "UI 초기화 완료");

            checkKeyHash();

            // Kakao SDK 초기화
            try {
                Log.d(TAG, "Kakao SDK 초기화 시작");
                KakaoSdk.init(this, "d998147f83d262022abfc49f600d81bc");
                Log.d(TAG, "Kakao SDK 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "Kakao SDK 초기화 실패", e);
            }

            // Retrofit 초기화
            try {
                Log.d(TAG, "Retrofit 초기화 시작");
                apiService = RetrofitClient.getApiService();
                Log.d(TAG, "Retrofit 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "Retrofit 초기화 실패", e);
            }

            // Google 로그인 설정
            try {
                Log.d(TAG, "Google 로그인 설정 시작");
                setupGoogleSignIn();
                Log.d(TAG, "Google 로그인 설정 완료");
            } catch (Exception e) {
                Log.e(TAG, "Google 로그인 설정 실패", e);
            }

            // 이벤트 리스너 설정
            Log.d(TAG, "이벤트 리스너 설정 시작");
            setupEventListeners();
            Log.d(TAG, "이벤트 리스너 설정 완료");

            // 자동 로그인 체크
            Log.d(TAG, "자동 로그인 체크 시작");
            checkAutoLogin();
            Log.d(TAG, "자동 로그인 체크 완료");

        } catch (Exception e) {
            Log.e(TAG, "앱 초기화 중 오류 발생", e);
            if (DEBUG) {
                e.printStackTrace();
            }
            // 치명적인 오류가 아닌 경우에는 토스트 메시지를 표시하지 않음
            if (e instanceof IllegalStateException) {
                Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다. 앱을 다시 시작해주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("키해시확인", "KeyHash: " + keyHash);
                Log.d("키해시확인", "KeyHash (trim): " + keyHash.trim()); // 공백 제거된 버전도 출력
            }
        } catch (Exception e) {
            Log.e("키해시확인", "키 해시 생성 실패", e);
        }
    }

    private void initializeViews() {
        try {
            Log.d(TAG, "UI 요소 찾기 시작");
            editTextId = findViewById(R.id.editTextId);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            buttonJoin = findViewById(R.id.buttonJoin);
            textForgotPassword = findViewById(R.id.textForgotPassword);
            buttonGoogleLogin = findViewById(R.id.buttonGoogleLogin);
            buttonKakaoLogin = findViewById(R.id.buttonKakaoLogin);

            if (editTextId == null || editTextPassword == null || buttonLogin == null || 
                buttonJoin == null || textForgotPassword == null || buttonGoogleLogin == null || 
                buttonKakaoLogin == null) {
                Log.e(TAG, "필수 UI 요소를 찾을 수 없습니다.");
                throw new IllegalStateException("필수 UI 요소를 찾을 수 없습니다.");
            }
            Log.d(TAG, "모든 UI 요소를 성공적으로 찾았습니다.");

            editTextId.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            editTextPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        } catch (Exception e) {
            Log.e(TAG, "UI 초기화 실패", e);
            if (DEBUG) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    private void setupGoogleSignIn() {
        try {
            Log.d(TAG, "Google 로그인 옵션 설정 시작");
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("807266536718-1eq9a689dvr3pudegd0itiktfgvj883q.apps.googleusercontent.com")
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google 로그인 클라이언트 생성 완료");

            googleSignInLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "Google 로그인 결과 수신");
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                if (account != null) {
                                    String idToken = account.getIdToken();
                                    sendIdTokenToServer(idToken);
                                }
                            } catch (ApiException e) {
                                Log.e(TAG, "구글 로그인 실패", e);
                                Toast.makeText(this, "구글 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "구글 로그인 취소됨");
                            Toast.makeText(this, "구글 로그인 취소됨", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            Log.d(TAG, "Google 로그인 설정 완료");
        } catch (Exception e) {
            Log.e(TAG, "Google 로그인 설정 실패", e);
            if (DEBUG) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    private void setupEventListeners() {
        buttonJoin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, JoinActivity.class)));
        textForgotPassword.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FindAccountActivity.class)));

        buttonLogin.setOnClickListener(v -> handleLocalLogin());
        buttonGoogleLogin.setOnClickListener(v -> handleGoogleLogin());
        buttonKakaoLogin.setOnClickListener(v -> handleKakaoLogin());
    }

    private void checkAutoLogin() {
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

            String token = securePrefs.getString("jwt_token", null);
            if (token != null && !token.isEmpty() && isTokenValid(token)) {
                // 서버에 토큰 검증 요청
                apiService.verifyToken("Bearer " + token).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().get("success"))) {
                            // 서버에서 OK 받으면 HomeActivity로 이동
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            // 서버에서 401/403 등 받으면 토큰 삭제
                            securePrefs.edit().remove("jwt_token").apply();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                        // 네트워크 오류 등
                        Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                securePrefs.edit().remove("jwt_token").apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JWT 토큰 유효성 검사 함수 추가
    private boolean isTokenValid(String token) {
        try {
            // 서버와 동일한 시크릿 키 사용
            String secretKey = "my-super-secure-jwt-secret-key-2025-very-long-and-random";
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();

            // 만료 시간 확인 (exp)
            if (claims.getExpiration() != null) {
                return claims.getExpiration().getTime() > System.currentTimeMillis();
            }
            return true; // 만료 정보 없으면 true
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleLocalLogin() {
        String id = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        Log.d("로그인요청", "id: " + id + ", pw: " + password);

        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> loginInfo = new HashMap<>();
        loginInfo.put("id", id);
        loginInfo.put("password", password);

        Call<Map<String, Object>> call = apiService.login(loginInfo);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d("로그인응답", "isSuccessful: " + response.isSuccessful());
                Log.d("로그인응답", "code: " + response.code());
                Log.d("로그인응답", "body: " + response.body());
                Log.d("로그인응답", "errorBody: " + response.errorBody());

                if (response.isSuccessful()) {
                    Map<String, Object> result = response.body();
                    if (result != null && (Boolean) result.get("success")) {
                        saveTokenAndMoveHome((String) result.get("token"));
                    } else {
                        Toast.makeText(MainActivity.this, "로그인 실패! 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "로그인 실패! 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(MainActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGoogleLogin() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleKakaoLogin() {
        Log.d("카카오버튼", "카카오 로그인 버튼 클릭됨");
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, kakaoCallback);
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this, kakaoCallback);
        }
    }

    private final Function2<OAuthToken, Throwable, Unit> kakaoCallback = (token, error) -> {
        Log.d("카카오콜백", "콜백 도착, token: " + token + ", error: " + error);
        if (error != null) {
            Toast.makeText(this, "카카오 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        } else if (token != null) {
            sendKakaoTokenToServer(token.getAccessToken());
        }
        return null;
    };

    private void sendKakaoTokenToServer(String kakaoToken) {
        Map<String, String> data = new HashMap<>();
        data.put("kakaoToken", kakaoToken);

        Log.d("카카오디버그", "토큰 서버로 보냄: " + kakaoToken); // 로그 추가

        try {
            Call<Map<String, Object>> call = apiService.kakaoLogin(data);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    Log.d("카카오응답", "isSuccessful: " + response.isSuccessful());
                    Log.d("카카오응답", "code: " + response.code());
                    Log.d("카카오응답", "body: " + response.body());
                    Log.d("카카오응답", "errorBody: " + response.errorBody());

                    if (response.isSuccessful() && response.body() != null) {
                        boolean success = Boolean.TRUE.equals(response.body().get("success"));
                        Log.d("카카오응답", "success: " + success);

                        if (success) {
                            String token = (String) response.body().get("token");
                            saveKakaoTokenAndMoveHome(token);
                            Log.d("카카오응답", "token: " + token);




                        } else {
                            Toast.makeText(MainActivity.this, "카카오 로그인 실패 (응답값)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "카카오 로그인 실패 (응답 오류)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("카카오응답", "API 호출 실패", t);
                    Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("카카오예외", "Retrofit 예외 발생", e);
            Toast.makeText(MainActivity.this, "예외 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendIdTokenToServer(String idToken) {
        Map<String, String> data = new HashMap<>();
        data.put("idToken", idToken);

        Call<Map<String, Object>> call = apiService.socialLogin(data);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = (boolean) response.body().get("success");
                    if (success) {
                        String token = (String) response.body().get("token");
                        saveGoogleTokenAndMoveHome(token);
                    } else {
                        Toast.makeText(MainActivity.this, "구글 로그인 실패!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "구글 로그인 실패!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTokenAndMoveHome(String token) {
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

            securePrefs.edit()
                    .putString("jwt_token", token)
                    .putString("provider", "LOCAL")
                    .putString("user_id", editTextId.getText().toString().trim())
                    .commit();

            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "토큰 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGoogleTokenAndMoveHome(String token) {
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

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            String email = account.getEmail();
            String userId = account.getId();
            
            Log.d(TAG, "구글 계정 정보 - Email: " + email + ", ID: " + userId);

            securePrefs.edit()
                    .putString("jwt_token", token)
                    .putString("provider", "GOOGLE")
                    .putString("user_id", email)  // 이메일을 user_id로 저장
                    .putString("email", email)    // 이메일도 별도로 저장
                    .putString("google_id", userId) // 원본 ID도 저장
                    .commit();

            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "토큰 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveKakaoTokenAndMoveHome(String token) {
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

            UserApiClient.getInstance().me((user, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오 사용자 정보 요청 실패", error);
                    Toast.makeText(MainActivity.this, "사용자 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    return null;
                }

                if (user != null) {
                    securePrefs.edit()
                            .putString("jwt_token", token)
                            .putString("provider", "KAKAO")
                            .putString("user_id", String.valueOf(user.getId()))
                            .commit();

                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "토큰 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
