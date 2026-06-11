package com.example.aistudyassistant.features.ocr_summary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.services.core.GeminiService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class OCRSummaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etTitle;
    private RecyclerView rvReview;
    private Button btnAction;
    private ProgressBar progressBar;
    private TextView tvLabel;

    private GeminiService geminiService;
    private StudySetRepository studySetRepository;
    private FlashcardRepository flashcardRepository;

    // Khai báo cả 2 bộ Adapter phục vụ cho 2 chế độ riêng biệt
    private FlashcardReviewAdapter flashcardAdapter;
    private QuizReviewAdapter quizAdapter;

    private String targetMode = "FLASHCARD"; // Cờ hiệu chế độ: "FLASHCARD" hoặc "QUIZ"
    private String generatedSetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_summary);

        // 1. Khởi tạo dịch vụ lõi và Database
        // LƯU Ý: Thay "YOUR_API_KEY" bằng key thực tế lấy từ Google AI Studio
        geminiService = new GeminiService("AQ.Ab8RN6JG9FwkX-aNKlGAK9mXT591vlbE5z1s7ExwOu345hYxuA");
        AppDatabase db = AppDatabase.getDatabase(this);
        studySetRepository = new StudySetRepository(db.studySetDao());
// Khởi tạo thêm UserStatsRepository vì FlashcardRepository bây giờ bắt buộc cần nó để đếm thẻ
        com.example.aistudyassistant.data.repository.UserStatsRepository statsRepo =
                new com.example.aistudyassistant.data.repository.UserStatsRepository(
                        db.userStatsDao(),
                        db.achievementDao(),
                        db.userAchievementDao()
                );

// Truyền đủ 2 tham số vào là hết báo lỗi ngay
        flashcardRepository = new FlashcardRepository(db.flashcardDao(), statsRepo);

        generatedSetId = UUID.randomUUID().toString();

        // 2. Ánh xạ các thành phần giao diện nền sáng
        btnBack = findViewById(R.id.btn_ocr_back);
        etTitle = findViewById(R.id.et_ocr_title);
        rvReview = findViewById(R.id.rv_flashcard_review);
        btnAction = findViewById(R.id.btn_generate_flashcards);
        progressBar = findViewById(R.id.pb_ocr_loading);
        tvLabel = findViewById(R.id.tv_ocr_result_label);

        // 3. Cấu hình RecyclerView cuộn dọc danh sách review
        rvReview.setLayoutManager(new LinearLayoutManager(this));

        // 4. Kiểm tra xem Fragment gửi lệnh yêu cầu tạo Flashcard hay tạo Quiz
        targetMode = getIntent().getStringExtra("TARGET_MODE");
        if (targetMode == null) targetMode = "FLASHCARD";

        // Kích hoạt Adapter tương ứng với từng chế độ hiển thị
        if (targetMode.equals("QUIZ")) {
            btnAction.setText("Lưu bộ câu hỏi Quiz vào máy");
            quizAdapter = new QuizReviewAdapter();
            rvReview.setAdapter(quizAdapter);
        } else {
            btnAction.setText("Lưu bộ thẻ Flashcards vào máy");
            flashcardAdapter = new FlashcardReviewAdapter();
            rvReview.setAdapter(flashcardAdapter);
        }

        btnBack.setOnClickListener(v -> finish());
        btnAction.setOnClickListener(v -> saveToDatabase());

        // 5. Đọc chuỗi ký tự thô trích từ ảnh và đẩy sang cho Gemini phân tích
        String extractedText = getIntent().getStringExtra("EXTRACTED_TEXT");
        if (extractedText != null && !extractedText.isEmpty()) {
            generateContentWithAI(extractedText);
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu văn bản!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void generateContentWithAI(String text) {
        progressBar.setVisibility(View.VISIBLE);
        btnAction.setVisibility(View.GONE);
        tvLabel.setText("AI đang đọc hiểu tài liệu và thiết kế câu hỏi...");

        String userEnteredTitle = etTitle.getText().toString().trim();
        boolean isUserNamed = !userEnteredTitle.isEmpty();

        String prompt;
        if (targetMode.equals("QUIZ")) {
            prompt = "Bạn là một robot TRÍCH XUẤT VÀ ĐỊNH DẠNG dữ liệu cực kỳ nghiêm ngặt. " +
                    "Văn bản đầu vào dưới đây ĐÃ CÓ SẴN các câu hỏi trắc nghiệm, các lựa chọn và đáp án đúng. " +
                    "Nhiệm vụ của bạn là đọc, nhặt chính xác dữ liệu đó ra và điền vào định dạng quy định. " +
                    "TUYỆT ĐỐI KHÔNG tự sáng tác câu hỏi mới, KHÔNG viết lại câu, KHÔNG đổi từ ngữ cấu trúc của người ta.\n\n" +
                    "⚠️ QUY TẮC BẮT BUỘC:\n" +
                    "1. Giữ nguyên 100% nội dung chữ của câu hỏi và các đáp án từ văn bản gốc được cung cấp.\n" +
                    "2. Định dạng chính xác dữ liệu bóc được thành từng dòng, mỗi dòng 1 câu theo cấu trúc sau:\n" +
                    "Question: [Nội dung câu hỏi gốc] | A: [Đáp án A gốc] | B: [Đáp án B gốc] | C: [Đáp án C gốc] | D: [Đáp án D gốc] | Correct: [Số từ 1 đến 4 tương ứng đáp án đúng có sẵn]\n" +
                    (isUserNamed ? "" : "3. Dòng đầu tiên trích xuất tiêu đề của tài liệu: 'Title: [Tiêu đề gốc trong văn bản]'\n") +
                    "4. Chỉ trả về dữ liệu thô theo cấu trúc trên. TUYỆT ĐỐI KHÔNG viết lời chào mở đầu, không lời kết, không bọc dấu sao bôi đậm (**).\n\n" +
                    "VĂN BẢN GỐC CÓ SẴN ĐỂ LỌC:\n" + text;
        } else {
            prompt = "Bạn là một robot TRÍCH XUẤT VÀ ĐỊNH DẠNG dữ liệu cực kỳ nghiêm ngặt. " +
                    "Văn bản đầu vào dưới đây ĐÃ CÓ SẴN các cặp thông tin bài học (hoặc từ vựng và giải nghĩa). " +
                    "Nhiệm vụ của bạn là nhặt chính xác vế thông tin đó ra và đưa vào cấu trúc quy định. " +
                    "TUYỆT ĐỐI KHÔNG tự sáng tác câu hỏi mới, KHÔNG giải thích rộng ra ngoài phạm vi văn bản gốc.\n\n" +
                    "⚠️ QUY TẮC BẮT BUỘC:\n" +
                    "1. Nhặt đúng thuật ngữ/vế đầu làm Front, nội dung giải nghĩa đi kèm ngay sau làm Back.\n" +
                    "2. Giữ nguyên chữ gốc, không tự ý viết lại ý nghĩa theo ý bạn.\n" +
                    "3. Định dạng chính xác từng dòng theo cấu trúc sau:\n" +
                    "Front: [Nội dung mặt trước gốc] | Back: [Nội dung mặt sau gốc]\n" +
                    (isUserNamed ? "" : "4. Dòng đầu tiên trích xuất tiêu đề của tài liệu: 'Title: [Tiêu đề gốc trong văn bản]'\n") +
                    "5. Chỉ trả về dữ liệu thô theo cấu trúc trên. TUYỆT ĐỐI KHÔNG thêm ký tự đặc biệt, không viết lời dẫn mở/kết bài.\n\n" +
                    "VĂN BẢN GỐC CÓ SẴN ĐỂ LỌC:\n" + text;
        }

        geminiService.generateResponseAsync(prompt, new GeminiService.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setVisibility(View.VISIBLE);
                    tvLabel.setText("Bạn có thể chạm vào chữ trên từng ô để chỉnh sửa lại:");

                    try {
                        String[] lines = response.split("\n");
                        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "mock_user";

                        List<FlashcardEntity> tempFlashcards = new ArrayList<>();
                        List<QuizReviewAdapter.QuizItem> tempQuizzes = new ArrayList<>();

                        for (String line : lines) {
                            // Chuẩn hóa xóa bỏ các dấu sao nhiễu của Markdown gạch chân/bôi đậm
                            String cleanLine = line.replace("**", "").replace("[", "").replace("]", "").trim();
                            String lowerLine = cleanLine.toLowerCase();

                            // 1. TÁCH TIÊU ĐỀ
                            if (!isUserNamed && lowerLine.contains("title:")) {
                                int titleIdx = lowerLine.indexOf("title:");
                                String aiTitle = cleanLine.substring(titleIdx + 6).trim();
                                etTitle.setText(aiTitle);
                            }
                            // 2. TÁCH FLASHCARD (Bất chấp chữ hoa chữ thường)
                            else if (targetMode.equals("FLASHCARD") && lowerLine.contains("front:") && lowerLine.contains("| back:")) {
                                int frontIdx = lowerLine.indexOf("front:");
                                int backIdx = lowerLine.indexOf("| back:");

                                String front = cleanLine.substring(frontIdx + 6, backIdx).trim();
                                String back = cleanLine.substring(backIdx + 7).trim();

                                tempFlashcards.add(new FlashcardEntity(UUID.randomUUID().toString(), generatedSetId, userId, front, back));
                            }
                            // 3. TÁCH QUIZ (Tìm vị trí thông minh bằng lowerLine, cắt bằng cleanLine)
                            else if (targetMode.equals("QUIZ") && lowerLine.contains("question:") && lowerLine.contains("| a:")) {
                                try {
                                    int qIdx = lowerLine.indexOf("question:");
                                    int aIdx = lowerLine.indexOf("| a:");
                                    int bIdx = lowerLine.indexOf("| b:");
                                    int cIdx = lowerLine.indexOf("| c:");
                                    int dIdx = lowerLine.indexOf("| d:");
                                    int correctIdx = lowerLine.indexOf("| correct:");

                                    String q = cleanLine.substring(qIdx + 9, aIdx).trim();
                                    String a = cleanLine.substring(aIdx + 4, bIdx).trim();
                                    String b = cleanLine.substring(bIdx + 4, cIdx).trim();
                                    String c = cleanLine.substring(cIdx + 4, dIdx).trim();
                                    String d = cleanLine.substring(dIdx + 4, correctIdx).trim();

                                    int correct = 1;
                                    try {
                                        correct = Integer.parseInt(cleanLine.substring(correctIdx + 10).trim());
                                    } catch (Exception e) { /* Mặc định đáp án 1 nếu AI trả về chuỗi ký tự lỗi */ }

                                    tempQuizzes.add(new QuizReviewAdapter.QuizItem(q, Arrays.asList(a, b, c, d), correct));
                                } catch (Exception e) {
                                    Log.e("OCRSummary", "Lỗi bóc tách dòng Quiz: " + cleanLine, e);
                                }
                            }
                        }

                        // Đổ dữ liệu vào giao diện sau khi đã bóc tách an toàn
                        if (targetMode.equals("QUIZ")) {
                            quizAdapter.setData(tempQuizzes);
                            if (tempQuizzes.isEmpty()) {
                                Toast.makeText(OCRSummaryActivity.this, "Không bóc tách được câu hỏi Quiz nào. Hãy thử lại!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            flashcardAdapter.setData(tempFlashcards);
                            if (tempFlashcards.isEmpty()) {
                                Toast.makeText(OCRSummaryActivity.this, "Không bóc tách được thẻ Flashcard nào. Hãy thử lại!", Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (Exception e) {
                        Log.e("OCRSummary", "Lỗi xử lý phản hồi tổng thể của AI", e);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OCRSummaryActivity.this, "Lỗi kết nối AI: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    private void saveToDatabase() {
        // 1. Tạo một biến thường để lấy tên do người dùng nhập vào
        String enteredTitle = etTitle.getText().toString().trim();

        // 2. Nếu trống thì xử lý gán tên mặc định bình thường
        if (enteredTitle.isEmpty()) {
            enteredTitle = targetMode.equals("QUIZ") ? "Bộ bài tập Quiz AI" : "Bộ thẻ học tập AI";
        }

        // 3. ĐÓNG BĂNG giá trị cuối cùng vào một biến FINAL để dùng an toàn trong Lambda
        final String finalTitle = enteredTitle;

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "mock_user";

        if (targetMode.equals("QUIZ")) {
            // Chế độ QUIZ
            List<QuizReviewAdapter.QuizItem> finalQuizzes = quizAdapter.getList();
            if (finalQuizzes.isEmpty()) {
                Toast.makeText(this, "Danh sách câu hỏi đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            // 🔮 NƠI GHI DATABASE CHO QUIZ:
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Đã tạo bộ câu hỏi '" + finalTitle + "' gồm " + finalQuizzes.size() + " câu thành công!", Toast.LENGTH_LONG).show();
                finish();
            });

        } else {
            // Chế độ FLASHCARD
            List<FlashcardEntity> finalCards = flashcardAdapter.getList();
            if (finalCards.isEmpty()) {
                Toast.makeText(this, "Danh sách thẻ đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            StudySetEntity studySet = new StudySetEntity(generatedSetId, userId, finalTitle);
            studySet.setTotalCards(finalCards.size());

            // Thực hiện lưu đồng bộ mối quan hệ cha-con vào SQLite Room
            studySetRepository.insertSet(studySet, () -> {
                flashcardRepository.insertFlashcards(finalCards);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã lưu bộ thẻ '" + finalTitle + "' vào máy thành công!", Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        }
    }}