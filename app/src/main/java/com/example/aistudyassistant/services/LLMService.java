package com.example.aistudyassistant.services;

import android.content.Context;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LLMService {

    private LlmInference llmInference;

    public LLMService(Context context) {
        try {
            // Tên file model .task bạn đã để trong thư mục assets
            String modelName = "qwen_model.task";

            File modelFile = new File(context.getFilesDir(), modelName);
            String modelPath = modelFile.getAbsolutePath();

            // Tự động copy file từ assets vào bộ nhớ trong của máy ở lần đầu chạy app
            if (!modelFile.exists()) {
                try (InputStream in = context.getAssets().open(modelName);
                     OutputStream out = new FileOutputStream(modelFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }

            // Cấu hình và khởi tạo MediaPipe Engine
            LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                    .setModelFilePath(modelPath)
                    .build();

            llmInference = LlmInference.createFromOptions(context, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm gọi AI xử lý (Hàm này chạy tốn thời gian nên BẮT BUỘC phải gọi ở Luồng ngầm - Background Thread)
     */
    public String generateResponse(String prompt) {
        if (llmInference == null) {
            return "Model AI chưa được khởi tạo thành công!";
        }
        try {
            return llmInference.generateResponse(prompt);
        } catch (Exception e) {
            return "Lỗi vận hành AI: " + e.getLocalizedMessage();
        }
    }

    public void close() {
        try {
            if (llmInference != null) {
                llmInference.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
