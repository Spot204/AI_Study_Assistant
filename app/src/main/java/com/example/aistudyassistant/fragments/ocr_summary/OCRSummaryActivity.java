package com.example.aistudyassistant.fragments.ocr_summary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.features.ocr_summary.DocumentController;
import com.google.firebase.auth.FirebaseAuth;

public class OCRSummaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View cvCapture;
    private TextView tvSummary;
    private Button btnGenerateFlashcards;
    private ProgressBar progressBar;
    private DocumentController documentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_summary);

        documentController = new DocumentController(this);

        btnBack = findViewById(R.id.btn_ocr_back);
       // cvCapture = findViewById(R.id.cv_ocr_capture);
       // tvSummary = findViewById(R.id.tv_ocr_summary_text);
        btnGenerateFlashcards = findViewById(R.id.btn_generate_flashcards);
        progressBar = findViewById(R.id.pb_ocr_loading);

        btnBack.setOnClickListener(v -> finish());

        cvCapture.setOnClickListener(v -> {
            // Giả lập nội dung quét được từ tài liệu
            String detectedText = "Spaced Repetition (Lặp lại ngắt quãng) là phương pháp học tập dựa trên việc ôn tập thông tin vào các khoảng thời gian tăng dần để tối ưu hóa trí nhớ dài hạn.";
            summarizeContent(detectedText);
        });

        btnGenerateFlashcards.setOnClickListener(v -> generateFlashcards());
    }

    private void summarizeContent(String text) {
        progressBar.setVisibility(View.VISIBLE);
        tvSummary.setText("AI đang phân tích tài liệu...");
        
        documentController.summarizeDocument(text, new DocumentController.AIProcessCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvSummary.setTextColor(getResources().getColor(android.R.color.white));
                    tvSummary.setText(response);
                    btnGenerateFlashcards.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OCRSummaryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void generateFlashcards() {
        progressBar.setVisibility(View.VISIBLE);
        String summary = tvSummary.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "mock_user";
        
        documentController.generateFlashcardsFromSummary(summary, userId, new DocumentController.FlashcardGenerationCallback() {
            @Override
            public void onSuccess(int count) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OCRSummaryActivity.this, "Đã tạo " + count + " Flashcards thành công!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OCRSummaryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
