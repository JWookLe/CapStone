package com.example.EmotionSync;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    //BASE_URL은 API 서버의 주소
    //10.0.2.2는 에뮬레이터에서 로컬호스트(localhost)를 의미
    //실제 기기에서는 "http://192.168.X.X:8080/" 같은 IP 주소를 사용해야 함
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // 로깅 인터셉터 추가
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 재시도 인터셉터 - 네트워크 오류 시 최대 3번 재시도
            Interceptor retryInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Response response = null;
                    IOException exception = null;

                    int maxRetries = 3;
                    int retryCount = 0;

                    while (retryCount < maxRetries && (response == null || !response.isSuccessful())) {
                        if (response != null) {
                            response.close(); // 이전 응답 닫기
                        }

                        try {
                            if (retryCount > 0) {
                                System.out.println("재시도 #" + retryCount);
                                // 재시도 전 대기 시간 추가
                                Thread.sleep(2000); // 2초 대기
                            }
                            response = chain.proceed(request);
                        } catch (IOException e) {
                            exception = e;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("재시도 중 인터럽트 발생", e);
                        } finally {
                            retryCount++;
                        }
                    }

                    if (response == null && exception != null) {
                        throw exception;
                    }

                    return response;
                }
            };

            // OkHttpClient 생성 및 타임아웃 설정
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(retryInterceptor)
                    .connectTimeout(60, TimeUnit.SECONDS)  // 연결 타임아웃 증가
                    .readTimeout(60, TimeUnit.SECONDS)     // 읽기 타임아웃 증가
                    .writeTimeout(60, TimeUnit.SECONDS)    // 쓰기 타임아웃 증가
                    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}