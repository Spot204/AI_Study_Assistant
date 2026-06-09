package com.example.aistudyassistant.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.example.aistudyassistant.database.entities.LearningGoalEntity;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class FlashcardStudyActivity extends AppCompatActivity {

    private TextView tvTitle, tvProgress, tvCardLabel, tvCardContent;
    private ProgressBar pbProgress;
    private CardView cvFlashcard;
    private ImageButton btnBack, btnNext, btnPrev;
    private Button btnWrong, btnRight;

    private List<FlashcardEntity> flashcards;
    private int currentIndex = 0;
    private boolean isFront = true;
    private long startTime;
    private String setId, setTitle;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_flashcard_study);

        db = AppDatabase.getDatabase(this);
        startTime = System.currentTimeMillis();
        setId = getIntent().getStringExtra("SET_ID");
        setTitle = getIntent().getStringExtra("SET_TITLE");

        initViews();
        loadFlashcards();

        btnBack.setOnClickListener(v -> finishStudy());
        cvFlashcard.setOnClickListener(v -> flipCard());
        btnNext.setOnClickListener(v -> nextCard());
        btnPrev.setOnClickListener(v -> prevCard());
        btnRight.setOnClickListener(v -> markAsLearned());
        btnWrong.setOnClickListener(v -> nextCard());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_study_title);
        tvProgress = findViewById(R.id.tv_study_progress_count);
        tvCardLabel = findViewById(R.id.tv_card_label);
        tvCardContent = findViewById(R.id.tv_card_content);
        pbProgress = findViewById(R.id.pb_study_progress);
        cvFlashcard = findViewById(R.id.cv_flashcard);
        btnBack = findViewById(R.id.btn_study_back);
        btnNext = findViewById(R.id.btn_study_next);
        btnPrev = findViewById(R.id.btn_study_prev);
        btnRight = findViewById(R.id.btn_study_right);
        btnWrong = findViewById(R.id.btn_study_wrong);

        if (setTitle != null) tvTitle.setText(setTitle);
    }

    private void loadFlashcards() {
        Executors.newSingleThreadExecutor().execute(() -> {
            flashcards = db.flashcardDao().getFlashcardsBySet(setId);
            runOnUiThread(() -> {
                if (flashcards != null && !flashcards.isEmpty()) {
                    updateUI();
                } else {
                    Toast.makeText(this, "Không có thẻ nào trong bộ này", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void updateUI() {
        FlashcardEntity card = flashcards.get(currentIndex);
        tvCardContent.setText(isFront ? card.getFront() : card.getBack());
        tvCardLabel.setText(isFront ? "CÂU HỎI" : "ĐÁP ÁN");
        tvProgress.setText((currentIndex + 1) + " / " + flashcards.size());
        pbProgress.setProgress((currentIndex + 1) * 100 / flashcards.size());
    }

    private void flipCard() {
        isFront = !isFront;
        updateUI();
    }

    private void nextCard() {
        if (currentIndex < flashcards.size() - 1) {
            currentIndex++;
            isFront = true;
            updateUI();
        } else {
            finishStudy();
        }
    }

    private void prevCard() {
        if (currentIndex > 0) {
            currentIndex--;
            isFront = true;
            updateUI();
        }
    }

    private void markAsLearned() {
        // Logic đánh dấu đã thuộc có thể thêm ở đây
        nextCard();
    }

    private void finishStudy() {
        long endTime = System.currentTimeMillis();
        int durationMinutes = (int) ((endTime - startTime) / 60000);
        if (durationMinutes < 1) durationMinutes = 1;

        int finalDuration = durationMinutes;
        Executors.newSingleThreadExecutor().execute(() -> {
            // Lưu phiên học
            StudySessionEntity session = new StudySessionEntity(
                    UUID.randomUUID().toString(),
                    "test_user_id",
                    "flashcard",
                    setId,
                    System.currentTimeMillis(),
                    finalDuration
            );
            session.setScore((currentIndex + 1) + "/" + flashcards.size());
            db.studySessionDao().insertSession(session);

            // Cập nhật mục tiêu
            List<LearningGoalEntity> goals = db.learningGoalDao().getGoalsByUser("test_user_id");
            if (goals != null && !goals.isEmpty()) {
                LearningGoalEntity goal = goals.get(0);
                goal.setCurrentValue(goal.getCurrentValue() + finalDuration);
                db.learningGoalDao().updateGoal(goal);
            }
        });

        Toast.makeText(this, "Đã ghi nhận " + durationMinutes + " phút học tập!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
