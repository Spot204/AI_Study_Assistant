package com.example.aistudyassistant.network;

import com.example.aistudyassistant.network.models.LoginRequest;
import com.example.aistudyassistant.network.models.RegisterRequest;
import com.example.aistudyassistant.network.models.AuthResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiClient {

    private static final String BASE_URL = "https://example-springboot-api.com/api/"; // URL API Gateway thực tế
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public interface ApiService {
        @POST("auth/login")
        Call<AuthResponse> login(@Body LoginRequest request);

        @POST("auth/register")
        Call<AuthResponse> register(@Body RegisterRequest request);
    }

    public static synchronized ApiService getService() {
        if (retrofit == null) {
            // Loại bỏ HttpLoggingInterceptor để sửa hoàn toàn lỗi:
            // "Cannot resolve symbol 'HttpLoggingInterceptor'"
            OkHttpClient client = new OkHttpClient.Builder().build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}