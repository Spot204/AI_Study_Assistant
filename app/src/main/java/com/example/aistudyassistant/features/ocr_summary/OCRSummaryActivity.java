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

import com.example.aistudyassistant.BuildConfig;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.services.core.GeminiService;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private com.example.aistudyassistant.data.repository.QuizRepository quizRepository;

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
        geminiService = new GeminiService(BuildConfig.GEMINI_API_KEY);
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
        quizRepository = new com.example.aistudyassistant.data.repository.QuizRepository(db.quizDao(), db.studySetDao());

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
        if (targetMode.equals("QUIZ") || targetMode.equals("GENERATE_QUIZ")) {
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
            prompt = "Bạn là một hệ thống trích xuất dữ liệu cực kỳ nghiêm ngặt. Nhiệm vụ của bạn là đọc đoạn văn bản nằm trong thẻ <source_text> dưới đây và trích xuất chính xác các câu hỏi trắc nghiệm đã có sẵn.\n" +
                    "⚠️ QUY TẮC BẮT BUỘC:\n" +
                    "1. Giữ nguyên 100% nội dung chữ của câu hỏi và các đáp án từ văn bản gốc. TUYỆT ĐỐI KHÔNG tự sáng tác, KHÔNG viết lại, KHÔNG đổi cấu trúc từ ngữ.\n" +
                    "2. Trả về ĐÚNG định dạng JSON dưới đây. CHỈ xuất ra JSON thô, không bọc trong markdown (như ```json), không viết lời chào, không giải thích thêm:\n" +
                    "{\n" +
                    "  \"title\": \"Tiêu đề gốc trong văn bản (nếu có, nếu không để null)\",\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"question\": \"Nội dung câu hỏi gốc\",\n" +
                    "      \"options\": {\"1\": \"Nội dung đáp án A\", \"2\": \"Nội dung đáp án B\", \"3\": \"Nội dung đáp án C\", \"4\": \"Nội dung đáp án D\"},\n" +
                    "      \"correct\": 1\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "3. 'correct' phải là số nguyên từ 1 đến 4.\n\n" +
                    "<source_text>\n" + text + "\n</source_text>";
        } else if (targetMode.equals("GENERATE_QUIZ")) {
            prompt = "Bạn là một chuyên gia giáo dục. Nhiệm vụ của bạn là tạo ra các câu hỏi trắc nghiệm từ nội dung học phần (flashcards) được cung cấp dưới đây.\n" +
                    "⚠️ QUY TẮC BẮT BUỘC:\n" +
                    "1. Tạo ra các câu hỏi trắc nghiệm hay, bám sát nội dung. Mỗi câu hỏi phải có 4 lựa chọn.\n" +
                    "2. Trả về ĐÚNG định dạng JSON dưới đây. CHỈ xuất ra JSON thô, không bọc trong markdown, không viết lời chào:\n" +
                    "{\n" +
                    "  \"title\": \"Tiêu đề bộ Quiz (dựa trên nội dung hoặc giữ nguyên tiêu đề gốc)\",\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"question\": \"Nội dung câu hỏi\",\n" +
                    "      \"options\": {\"1\": \"Đáp án A\", \"2\": \"Đáp án B\", \"3\": \"Đáp án C\", \"4\": \"Đáp án D\"},\n" +
                    "      \"correct\": 1\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "3. 'correct' là số từ 1-4 ứng với đáp án đúng.\n\n" +
                    "NỘI DUNG HỌC PHẦN:\n" + text;
        } else {
            prompt = "Bạn là một robot TRÍCH XUẤT VÀ ĐỊNH DẠNG dữ liệu cực kỳ nghiêm ngặt. " +
                    "Văn bản đầu vào dưới đây ĐÃ CÓ SẴN các cặp thông tin bài học (hoặc từ vựng và giải nghĩa). " +
                    "Nhiệm vụ của bạn là nhặt chính xác vế thông tin đó ra và đưa vào cấu trúc quy định.\n\n" +
                    "⚠️ QUY TẮC BẮT BUỘC:\n" +
                    "1. Nhặt đúng thuật ngữ/vế đầu làm Front, nội dung giải nghĩa đi kèm ngay sau làm Back.\n" +
                    "2. Giữ nguyên chữ gốc, không tự ý viết lại ý nghĩa theo ý bạn.\n" +
                    "3. Định dạng chính xác từng dòng theo cấu trúc sau:\n" +
                    "Front: [Nội dung mặt trước gốc] | Back: [Nội dung mặt sau gốc]\n" +
                    (isUserNamed ? "" : "4. Dòng đầu tiên trích xuất tiêu đề của tài liệu: 'Title: [Tiêu đề gốc trong văn bản]'\n") +
                    "5. Chỉ trả về dữ liệu thô theo cấu trúc trên. TUYỆT ĐỐI KHÔNG thêm ký tự đặc biệt, không viết lời dẫn mở/kết bài.\n\n" +
                    "VĂN BẢN GỐC:\n" + text;
        }

        geminiService.generateResponseAsync(prompt, new GeminiService.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setVisibility(View.VISIBLE);
                    tvLabel.setText("Bạn có thể chạm vào chữ trên từng ô để chỉnh sửa lại:");

                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "mock_user";

                    try {
                        if (targetMode.equals("QUIZ") || targetMode.equals("GENERATE_QUIZ")) {
                            // Xử lý bằng JSON cho Quiz
                            String jsonStr = response.trim();
                            
                            // Trích xuất JSON từ khối code markdown nếu có
                            if (jsonStr.contains("```json")) {
                                int start = jsonStr.indexOf("```json") + 7;
                                int end = jsonStr.lastIndexOf("```");
                                if (end > start) {
                                    jsonStr = jsonStr.substring(start, end).trim();
                                }
                            } else if (jsonStr.contains("```")) {
                                int start = jsonStr.indexOf("```") + 3;
                                int end = jsonStr.lastIndexOf("```");
                                if (end > start) {
                                    jsonStr = jsonStr.substring(start, end).trim();
                                }
                            }

                            // Tìm kiếm ngoặc nhọn đầu tiên và cuối cùng nếu vẫn chưa sạch
                            if (!jsonStr.startsWith("{")) {
                                int firstBrace = jsonStr.indexOf("{");
                                int lastBrace = jsonStr.lastIndexOf("}");
                                if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                                    jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
                                }
                            }

                            JSONObject root = new JSONObject(jsonStr);
                            if (!isUserNamed && root.has("title") && !root.isNull("title")) {
                                etTitle.setText(root.getString("title"));
                            }

                            JSONArray questions = root.getJSONArray("questions");
                            List<QuizReviewAdapter.QuizItem> tempQuizzes = new ArrayList<>();
                            for (int i = 0; i < questions.length(); i++) {
                                JSONObject qObj = questions.getJSONObject(i);
                                JSONObject opts = qObj.getJSONObject("options");
                                List<String> options = Arrays.asList(
                                        opts.optString("1", opts.optString("A", "")),
                                        opts.optString("2", opts.optString("B", "")),
                                        opts.optString("3", opts.optString("C", "")),
                                        opts.optString("4", opts.optString("D", ""))
                                );
                                tempQuizzes.add(new QuizReviewAdapter.QuizItem(qObj.getString("question"), options, qObj.optInt("correct", 1)));
                            }
                            quizAdapter.setData(tempQuizzes);

                        } else {
                            // Xử lý bằng Split Line cho Flashcard (để nguyên như cũ)
                            String[] lines = response.split("\n");
                            List<FlashcardEntity> tempFlashcards = new ArrayList<>();

                            for (String line : lines) {
                                String cleanLine = line.replace("**", "").replace("[", "").replace("]", "").trim();
                                String lowerLine = cleanLine.toLowerCase();

                                if (!isUserNamed && lowerLine.contains("title:")) {
                                    etTitle.setText(cleanLine.substring(lowerLine.indexOf("title:") + 6).trim());
                                } else if (lowerLine.contains("front:") && lowerLine.contains("| back:")) {
                                    int fIdx = lowerLine.indexOf("front:");
                                    int bIdx = lowerLine.indexOf("| back:");
                                    String f = cleanLine.substring(fIdx + 6, bIdx).trim();
                                    String b = cleanLine.substring(bIdx + 7).trim();
                                    tempFlashcards.add(new FlashcardEntity(UUID.randomUUID().toString(), generatedSetId, userId, f, b));
                                }
                            }
                            flashcardAdapter.setData(tempFlashcards);
                        }
                    } catch (Exception e) {
                        Log.e("OCRSummary", "Lỗi xử lý phản hồi AI", e);
                        Toast.makeText(OCRSummaryActivity.this, "Lỗi phân tích dữ liệu. Hãy thử lại!", Toast.LENGTH_SHORT).show();
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

        if (targetMode.equals("QUIZ") || targetMode.equals("GENERATE_QUIZ")) {
            // Chế độ QUIZ
            List<QuizReviewAdapter.QuizItem> finalQuizzes = quizAdapter.getList();
            if (finalQuizzes.isEmpty()) {
                Toast.makeText(this, "Danh sách câu hỏi đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            
            // Chuyển đổi danh sách QuizItem sang JSON String để lưu vào database
            try {
                JSONArray qArray = new JSONArray();
                for (QuizReviewAdapter.QuizItem item : finalQuizzes) {
                    JSONObject obj = new JSONObject();
                    obj.put("question", item.question);
                    obj.put("options", new JSONArray(item.options));
                    obj.put("correct", item.correctAnswerIndex);
                    qArray.put(obj);
                }

                com.example.aistudyassistant.database.entities.QuizEntity quizEntity = 
                    new com.example.aistudyassistant.database.entities.QuizEntity(
                        UUID.randomUUID().toString(),
                        userId,
                        finalTitle,
                        null, // sourceSetId
                        null, // sourceDocumentId
                        15,   // Thời gian mặc định 15 phút
                        0,    // Điểm cao nhất ban đầu
                        qArray.toString()
                    );

                quizRepository.insertQuiz(quizEntity);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã lưu bộ câu hỏi '" + finalTitle + "' thành công!", Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e("OCRSummary", "Lỗi khi đóng gói JSON Quiz", e);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi khi lưu dữ liệu Quiz!", Toast.LENGTH_SHORT).show();
            }

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