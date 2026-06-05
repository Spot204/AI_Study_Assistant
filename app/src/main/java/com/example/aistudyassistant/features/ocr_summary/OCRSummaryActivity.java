package com.example.aistudyassistant.features.ocr_summary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OCRSummaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View cvCapture;
    private TextView tvSummary;
    private Button btnGenerateFlashcards;
    private ProgressBar progressBar;
    private com.example.aistudyassistant.services.core.LLMService llmService;
    private StudySetRepository studySetRepository;
    private FlashcardRepository flashcardRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_summary);

        llmService = new com.example.aistudyassistant.services.core.LLMService();
        AppDatabase db = AppDatabase.getDatabase(this);
        studySetRepository = new StudySetRepository(db.studySetDao());
        flashcardRepository = new FlashcardRepository(db.flashcardDao());

        btnBack = findViewById(R.id.btn_ocr_back);
        cvCapture = findViewById(R.id.cv_ocr_capture);
        tvSummary = findViewById(R.id.tv_ocr_summary_text);
        btnGenerateFlashcards = findViewById(R.id.btn_generate_flashcards);
        progressBar = findViewById(R.id.pb_ocr_loading);

        btnBack.setOnClickListener(v -> finish());

        cvCapture.setOnClickListener(v -> {
            // Giả lập nội dung quét được từ tài liệu
            String detectedText = "Spaced Repetition (Lặp lại ngắt quãng) là phương pháp học tập dựa trên việc ôn tập thông tin vào các khoảng thời gian tăng dần để tối ưu hóa trí nhớ dài hạn.";
            processAIText(detectedText);
        });

        btnGenerateFlashcards.setOnClickListener(v -> {
            generateFlashcardsFromSummary();
        });
    }

    private void processAIText(String text) {
        progressBar.setVisibility(View.VISIBLE);
        tvSummary.setText("AI đang phân tích tài liệu...");
        
        llmService.generateResponseAsync("Hãy tóm tắt ngắn gọn và phân tích nội dung này: " + text, response -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvSummary.setTextColor(getResources().getColor(android.R.color.white));
                tvSummary.setText(response);
                btnGenerateFlashcards.setVisibility(View.VISIBLE);
            });
        });
    }

    private void generateFlashcardsFromSummary() {
        progressBar.setVisibility(View.VISIBLE);
        String summary = tvSummary.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "mock_user";
        
        llmService.generateResponseAsync("Từ nội dung sau, hãy tạo 3 flashcards. Mỗi flashcard gồm Front (câu hỏi) và Back (câu trả lời). Định dạng: 'Front: [câu hỏi] | Back: [câu trả lời]'. Nội dung: " + summary, response -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                try {
                    String setId = UUID.randomUUID().toString();
                    StudySetEntity studySet = new StudySetEntity(setId, userId, "AI Generated Set");
                    
                    List<FlashcardEntity> flashcards = new ArrayList<>();
                    String[] lines = response.split("\n");
                    for (String line : lines) {
                        if (line.contains("Front:") && line.contains("| Back:")) {
                            String front = line.substring(line.indexOf("Front:") + 6, line.indexOf("| Back:")).trim();
                            String back = line.substring(line.indexOf("| Back:") + 7).trim();
                            flashcards.add(new FlashcardEntity(UUID.randomUUID().toString(), setId, userId, front, back));
                        }
                    }

                    if (!flashcards.isEmpty()) {
                        studySet.setTotalCards(flashcards.size());
                        studySetRepository.insertSet(studySet, () -> {
                            flashcardRepository.insertFlashcards(flashcards);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Đã tạo " + flashcards.size() + " Flashcards thành công!", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        });
                    } else {
                        Toast.makeText(this, "Không thể trích xuất Flashcards từ AI", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("OCRSummary", "Error saving flashcards", e);
                    Toast.makeText(this, "Lỗi khi lưu Flashcards", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

