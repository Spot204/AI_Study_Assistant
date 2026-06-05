package com.example.aistudyassistant.services.core;

import android.content.Context;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LLMService {

    private LlmApiService apiService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    // THAY THẾ: Nhập API Key của bạn ở đây (Ví dụ Gemini API)
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE"; 
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

    /**
     * Giờ đây hàm này chỉ đơn giản là báo thành công ngay lập tức 
     * vì không cần nạp model vào RAM nữa.
     */
    public void initializeModelAsync(Context context, InitializationCallback callback) {
        executorService.execute(() -> {
            // Giả lập kiểm tra kết nối internet hoặc API service
            if (apiService != null) {
                callback.onSuccess();
            } else {
                callback.onError("Không thể khởi tạo API Service");
            }
        });
    }

    /**
     * Gửi câu hỏi lên Server và nhận kết quả
     */
    public void generateResponseAsync(String prompt, ResponseCallback callback) {
        executorService.execute(() -> {
            try {
                // Tạo cấu trúc Body cho Gemini API
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

                Response<JsonObject> response = apiService.getChatResponse(API_KEY, body).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Trích xuất text từ JSON trả về của Gemini
                    String aiText = response.body()
                            .getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    
                    callback.onResult(aiText);
                } else {
                    callback.onResult("Lỗi API: " + response.code() + " - " + response.message());
                }
            } catch (Exception e) {
                callback.onResult("Lỗi kết nối Server: " + e.getLocalizedMessage());
            }
        });
    }

    public void close() {
        executorService.shutdown();
    }
}
