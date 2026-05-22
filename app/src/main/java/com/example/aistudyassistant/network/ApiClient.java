package com.example.aistudyassistant.network;

import com.example.aistudyassistant.network.models.LoginRequest;
import com.example.aistudyassistant.network.models.RegisterRequest;
import com.example.aistudyassistant.network.models.AuthResponse;
import com.example.aistudyassistant.network.models.ScanRequest;
import com.example.aistudyassistant.network.models.ScanResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiClient {

    private static final String BASE_URL = "https://example-springboot-api.com/api/"; // URL API Gateway deploy thực tế
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public interface ApiService {
        @POST("auth/login")
        Call<AuthResponse> login(@Body LoginRequest request);

        @POST("auth/register")
        Call<AuthResponse> register(@Body RegisterRequest request);

        @POST("scan")
        Call<ScanResponse> scanNotes(@Body ScanRequest request);
    }

    public static synchronized ApiService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

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