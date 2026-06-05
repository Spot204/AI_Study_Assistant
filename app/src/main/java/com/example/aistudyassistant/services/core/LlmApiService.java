package com.example.aistudyassistant.services.core;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LlmApiService {
    // Ví dụ cấu hình cho Gemini API hoặc một API chuẩn OpenAI
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    Call<JsonObject> getChatResponse(
        @Query("key") String apiKey,
        @Body JsonObject body
    );
}
