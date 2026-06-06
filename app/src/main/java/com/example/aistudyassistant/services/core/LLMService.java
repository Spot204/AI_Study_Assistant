package com.example.aistudyassistant.services.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 2 DÒNG IMPORT BẮT BUỘC ĐỂ KHÔNG BỊ LỖI CHỮ ĐỎ
import com.example.aistudyassistant.database.entities.ChatMessage;
import com.example.aistudyassistant.BuildConfig;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LLMService {
    private static final String TAG = "LLMService";
    private LlmApiService apiService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // THÊM: Handler để xử lý lỗi văng app khi cập nhật Giao diện
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // NHỚ ĐIỀN LẠI API KEY CỦA BẠN VÀO ĐÂY
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    public interface InitializationCallback {
        void onSuccess();
        void onError(String errorMsg);
    }

    public interface ResponseCallback {
        void onResult(String response);
    }

    public LLMService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(LlmApiService.class);
    }

    public void initializeModelAsync(Context context, InitializationCallback callback) {
        Log.i(TAG, "initializeModelAsync: Checking API service...");
        executorService.execute(() -> {
            if (apiService != null) {
                Log.i(TAG, "API Service is ready");
                mainHandler.post(callback::onSuccess); // Đẩy về Main Thread
            } else {
                Log.e(TAG, "API Service initialization failed");
                mainHandler.post(() -> callback.onError("Không thể khởi tạo API Service"));
            }
        });
    }

    // =================================================================
    // HÀM 1: Dùng cho Tạo Flashcard (Hỏi 1 câu - Đáp 1 câu)
    // =================================================================
    public void generateResponseAsync(String prompt, ResponseCallback callback) {
        Log.d(TAG, "generateResponseAsync called with prompt: " + prompt);
        executorService.execute(() -> {
            try {
                JsonObject body = new JsonObject();
                JsonArray contents = new JsonArray();
                JsonObject contentObj = new JsonObject();
                JsonArray parts = new JsonArray();
                JsonObject partObj = new JsonObject();

                partObj.addProperty("text", prompt);
                parts.add(partObj);
                contentObj.add("parts", parts);
                contents.add(contentObj);
                body.add("contents", contents);

                Log.d(TAG, "Sending request to Gemini API...");
                Response<JsonObject> response = apiService.getChatResponse(API_KEY, body).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String aiText = response.body()
                            .getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

                    Log.d(TAG, "AI Response success: " + aiText);
                    mainHandler.post(() -> callback.onResult(aiText));
                } else {
                    String error = "Lỗi API: " + response.code() + " - " + response.message();
                    Log.e(TAG, error);
                    mainHandler.post(() -> callback.onResult(error));
                }
            } catch (Exception e) {
                Log.e(TAG, "Network or Parsing Error: " + e.getLocalizedMessage(), e);
                mainHandler.post(() -> callback.onResult("Lỗi kết nối Server: " + e.getLocalizedMessage()));
            }
        });
    }

    // =================================================================
    // HÀM 2: Dùng cho CHATBOT (Gửi cả danh sách lịch sử + Sửa lỗi 400)
    // =================================================================
    public void generateChatResponseAsync(List<ChatMessage> chatHistory, ResponseCallback callback) {
        Log.d(TAG, "generateChatResponseAsync called with history size: " + chatHistory.size());
        executorService.execute(() -> {
            try {
                JsonObject body = new JsonObject();
                JsonArray contents = new JsonArray();

                // Duyệt qua toàn bộ tin nhắn
                for (int i = 0; i < chatHistory.size(); i++) {
                    ChatMessage msg = chatHistory.get(i);

                    // FIX LỖI 400 BAD REQUEST:
                    // Gemini cấm tin nhắn đầu tiên trong lịch sử là của Bot.
                    // Dòng này sẽ tự động loại bỏ câu chào mặc định của Bot trước khi gửi đi.
                    if (i == 0 && !msg.isUser()) {
                        continue;
                    }

                    JsonObject contentObj = new JsonObject();
                    contentObj.addProperty("role", msg.isUser() ? "user" : "model");

                    JsonArray parts = new JsonArray();
                    JsonObject partObj = new JsonObject();
                    partObj.addProperty("text", msg.getText());

                    parts.add(partObj);
                    contentObj.add("parts", parts);
                    contents.add(contentObj);
                }

                body.add("contents", contents);

                Log.d(TAG, "Sending CHAT request to Gemini API...");
                Response<JsonObject> response = apiService.getChatResponse(API_KEY, body).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String aiText = response.body()
                            .getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

                    Log.d(TAG, "AI Response success: " + aiText);
                    mainHandler.post(() -> callback.onResult(aiText)); // Bắn về Main Thread vẽ UI
                } else {
                    // Lấy chi tiết lỗi từ body để dễ fix (nếu có)
                    String errorDetail = response.errorBody() != null ? response.errorBody().string() : response.message();
                    String error = "Lỗi API: " + response.code() + " - " + errorDetail;
                    Log.e(TAG, error);
                    mainHandler.post(() -> callback.onResult(error));
                }
            } catch (Exception e) {
                Log.e(TAG, "Network or Parsing Error: " + e.getLocalizedMessage(), e);
                mainHandler.post(() -> callback.onResult("Lỗi kết nối Server: " + e.getLocalizedMessage()));
            }
        });
    }

    public void close() {
        Log.i(TAG, "Closing LLM Service executor");
        executorService.shutdown();
    }
}