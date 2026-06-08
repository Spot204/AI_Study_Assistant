package com.example.aistudyassistant.services.ocr_summary;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class OCRService {

    // 1. Khai báo Interface để trả kết quả về cho màn hình (Fragment)
    public interface OCRCallback {
        void onSuccess(String extractedText);
        void onFailure(String errorMessage);
    }

    /**
     * 2. Hàm chính: Đọc chữ từ hình ảnh
     * @param context: Môi trường của ứng dụng
     * @param imageUri: Đường dẫn tới bức ảnh cần quét
     * @param callback: Cổng trả kết quả
     */
    public void extractTextFromImage(Context context, Uri imageUri, OCRCallback callback) {
        try {
            // Bước A: Chuyển đổi đường dẫn ảnh thành định dạng mà Google ML Kit hiểu được
            InputImage image = InputImage.fromFilePath(context, imageUri);

            // Bước B: Khởi tạo bộ nhận diện chữ viết (dùng bộ ngôn ngữ Latin mặc định bao gồm Tiếng Việt/Anh)
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Bước C: Bắt đầu cho AI quét ảnh
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        // Thành công! Lấy toàn bộ đoạn text ra
                        String resultText = visionText.getText();

                        // Nếu ảnh trống không có chữ nào
                        if (resultText.trim().isEmpty()) {
                            callback.onFailure("Không tìm thấy chữ nào trong ảnh này.");
                        } else {
                            // Trả chữ về cho màn hình hiển thị
                            callback.onSuccess(resultText);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Lỗi trong quá trình AI quét (ví dụ: ảnh quá mờ)
                        callback.onFailure("Lỗi nhận diện chữ: " + e.getMessage());
                    });

        } catch (IOException e) {
            // Lỗi không tìm thấy file ảnh trong máy
            callback.onFailure("Không thể đọc được file ảnh: " + e.getMessage());
        }
    }
}