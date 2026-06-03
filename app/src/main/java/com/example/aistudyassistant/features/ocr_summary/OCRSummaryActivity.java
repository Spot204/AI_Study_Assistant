package com.example.aistudyassistant.features.ocr_summary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;

public class OCRSummaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View cvCapture;
    private TextView tvSummary;
    private Button btnGenerateFlashcards;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_summary);

        btnBack = findViewById(R.id.btn_ocr_back);
        cvCapture = findViewById(R.id.cv_ocr_capture);
        tvSummary = findViewById(R.id.tv_ocr_summary_text);
        btnGenerateFlashcards = findViewById(R.id.btn_generate_flashcards);
        progressBar = findViewById(R.id.pb_ocr_loading);

        btnBack.setOnClickListener(v -> finish());

        cvCapture.setOnClickListener(v -> {
            // Mock OCR & Summary Process
            startMockOcrProcess();
        });

        btnGenerateFlashcards.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang được phát triển!", Toast.LENGTH_SHORT).show();
        });
    }

    private void startMockOcrProcess() {
        progressBar.setVisibility(View.VISIBLE);
        tvSummary.setText("Đang xử lý tài liệu...");
        
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            tvSummary.setTextColor(getResources().getColor(android.R.color.white));
            tvSummary.setText("Tóm tắt tài liệu:\n\n" +
                    "1. Spaced Repetition (Lặp lại ngắt quãng) là kỹ thuật học tập giúp tối ưu hóa khả năng ghi nhớ dài hạn.\n" +
                    "2. Cơ chế hoạt động dựa trên việc ôn tập thông tin vào các khoảng thời gian tăng dần.\n" +
                    "3. Giúp vượt qua \"Đường cong quên lãng\" của Ebbinghaus.\n" +
                    "4. AI có thể tự động hóa việc lên lịch ôn tập cho từng thẻ Flashcard cụ thể.");
        }, 2000);
    }
}
