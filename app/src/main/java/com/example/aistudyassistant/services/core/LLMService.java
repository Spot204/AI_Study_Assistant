package com.example.aistudyassistant.services.core;

import android.content.Context;
import android.util.Log;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LLMService {

    private LlmInference llmInference;
    private boolean isModelReady = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Cổng giao tiếp báo cáo trạng thái nạp Model (Khắc phục lỗi dòng 78 của ChatFragment)
    public interface InitializationCallback {
        void onSuccess();
        void onError(String errorMsg);
    }

    // Cổng giao tiếp trả kết quả sinh chữ AI (Khắc phục lỗi dòng 132 của ChatFragment)
    public interface ResponseCallback {
        void onResult(String response);
    }

    // Constructor rỗng (Khắc phục lỗi dòng 77 của ChatFragment: Expected 1 argument but found 0)
    public LLMService() {
    }

    /**
     * Khởi tạo AI chạy ngầm. Tránh đơ màn hình khi copy file > 2GB.
     */
    public void initializeModelAsync(Context context, InitializationCallback callback) {
        executorService.execute(() -> {
            try {
                // Đã sửa lại đúng chính tả tên file dựa trên bức ảnh chụp thư mục assets của bạn
                String modelName = "qwen3_thinking_4b_q4_block128_ekv2048.task";

                File modelFile = new File(context.getFilesDir(), modelName);
                String modelPath = modelFile.getAbsolutePath();

                // Tự động copy file từ assets vào bộ nhớ trong của máy ở lần đầu chạy app
                if (!modelFile.exists()) {
                    Log.d("LLMService", "Đang copy model >2GB... Vui lòng đợi vài phút!");
                    try (InputStream in = context.getAssets().open(modelName);
                         OutputStream out = new FileOutputStream(modelFile)) {
                        // Tăng bộ đệm (buffer) lên 8KB để copy nhanh hơn
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    Log.d("LLMService", "Copy model hoàn tất!");
                }

                // Cấu hình và khởi tạo MediaPipe Engine
                LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelPath)
                        .setMaxTokens(1024) // Giới hạn từ AI sinh ra
                        .build();

                llmInference = LlmInference.createFromOptions(context, options);
                isModelReady = true;

                // Báo thành công về cho giao diện (UI)
                callback.onSuccess();

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Hàm gọi AI xử lý ngầm (Background Thread)
     */
    public void generateResponseAsync(String prompt, ResponseCallback callback) {
        if (!isModelReady || llmInference == null) {
            callback.onResult("Model AI chưa được khởi tạo thành công!");
            return;
        }

        executorService.execute(() -> {
            try {
                String response = llmInference.generateResponse(prompt);
                callback.onResult(response);
            } catch (Exception e) {
                callback.onResult("Lỗi vận hành AI: " + e.getLocalizedMessage());
            }
        });
    }

    public void close() {
        try {
            if (llmInference != null) {
                llmInference.close();
            }
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}