package com.example.aistudyassistant.features.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.example.aistudyassistant.firebase.FirestoreService;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class QuizPlayActivity extends AppCompatActivity {

    private TextView tvQuestion, tvQuestionNum, tvProgress, tvTimer;
    private TextView tvOptionA, tvOptionB, tvOptionC, tvOptionD;
    private MaterialCardView cardOptionA, cardOptionB, cardOptionC, cardOptionD;
    private TextView tvOptionALetter, tvOptionBLetter, tvOptionCLetter, tvOptionDLetter;
    private LinearProgressIndicator progressBar;
    private ImageView btnBack;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean isAnswered = false;
    private String sessionId;

    private long startTime;
    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_play);

        initViews();
        loadQuestions();
        displayQuestion();

        sessionId = UUID.randomUUID().toString();
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        btnBack.setOnClickListener(v -> showExitConfirmation());

        // Sử dụng OnBackPressedDispatcher thay cho onBackPressed() đã bị deprecated
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }

    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thoát Quiz")
                .setMessage("Bạn có chắc chắn muốn thoát? Tiến trình sẽ không được lưu.")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục", null)
                .show();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionNum = findViewById(R.id.tvQuestionNum);
        tvProgress = findViewById(R.id.tvProgress);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        tvOptionA = findViewById(R.id.tvOptionA);
        tvOptionB = findViewById(R.id.tvOptionB);
        tvOptionC = findViewById(R.id.tvOptionC);
        tvOptionD = findViewById(R.id.tvOptionD);

        cardOptionA = findViewById(R.id.cardOptionA);
        cardOptionB = findViewById(R.id.cardOptionB);
        cardOptionC = findViewById(R.id.cardOptionC);
        cardOptionD = findViewById(R.id.cardOptionD);

        tvOptionALetter = findViewById(R.id.tvOptionALetter);
        tvOptionBLetter = findViewById(R.id.tvOptionBLetter);
        tvOptionCLetter = findViewById(R.id.tvOptionCLetter);
        tvOptionDLetter = findViewById(R.id.tvOptionDLetter);

        cardOptionA.setOnClickListener(v -> checkAnswer(0, cardOptionA, tvOptionALetter));
        cardOptionB.setOnClickListener(v -> checkAnswer(1, cardOptionB, tvOptionBLetter));
        cardOptionC.setOnClickListener(v -> checkAnswer(2, cardOptionC, tvOptionCLetter));
        cardOptionD.setOnClickListener(v -> checkAnswer(3, cardOptionD, tvOptionDLetter));
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();
        questionList.add(new Question(
                "Đạo hàm của hàm số y = sin(x) là gì?",
                Arrays.asList("y' = cos(x)", "y' = -sin(x)", "y' = -cos(x)", "y' = tan(x)"),
                0
        ));
        questionList.add(new Question(
                "Đạo hàm của hàm số y = cos(x) là gì?",
                Arrays.asList("y' = sin(x)", "y' = -sin(x)", "y' = cos(x)", "y' = -cos(x)"),
                1
        ));
        questionList.add(new Question(
                "Đạo hàm của hàm số y = x^2 là gì?",
                Arrays.asList("y' = x", "y' = 2x", "y' = x^2", "y' = 2"),
                1
        ));
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            isAnswered = false;
            Question currentQuestion = questionList.get(currentQuestionIndex);

            String questionNumText = "Câu " + (currentQuestionIndex + 1);
            tvQuestionNum.setText(questionNumText);
            
            tvQuestion.setText(currentQuestion.getQuestionText());
            
            String progressText = (currentQuestionIndex + 1) + "/" + questionList.size();
            tvProgress.setText(progressText);
            
            progressBar.setProgress((int) (((float) (currentQuestionIndex + 1) / questionList.size()) * 100));

            tvOptionA.setText(currentQuestion.getOptions().get(0));
            tvOptionB.setText(currentQuestion.getOptions().get(1));
            tvOptionC.setText(currentQuestion.getOptions().get(2));
            tvOptionD.setText(currentQuestion.getOptions().get(3));

            resetOptionStyles();
        } else {
            showResults();
        }
    }

    private void resetOptionStyles() {
        List<MaterialCardView> cards = Arrays.asList(cardOptionA, cardOptionB, cardOptionC, cardOptionD);
        List<TextView> letters = Arrays.asList(tvOptionALetter, tvOptionBLetter, tvOptionCLetter, tvOptionDLetter);

        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            letters.get(i).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_background));
            letters.get(i).setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void checkAnswer(int selectedIndex, MaterialCardView selectedCard, TextView selectedLetter) {
        if (isAnswered) return;
        isAnswered = true;

        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (selectedIndex == currentQuestion.getCorrectAnswerIndex()) {
            score++;
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.quiz_correct_light));
            selectedLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quiz_correct_dark));
            selectedLetter.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.quiz_wrong_light));
            selectedLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quiz_wrong_dark));
            selectedLetter.setTextColor(ContextCompat.getColor(this, R.color.white));
            
            // Show correct answer
            showCorrectAnswer(currentQuestion.getCorrectAnswerIndex());
        }

        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1500);
    }

    private void showCorrectAnswer(int correctIndex) {
        MaterialCardView correctCard;
        TextView correctLetter;

        switch (correctIndex) {
            case 0: correctCard = cardOptionA; correctLetter = tvOptionALetter; break;
            case 1: correctCard = cardOptionB; correctLetter = tvOptionBLetter; break;
            case 2: correctCard = cardOptionC; correctLetter = tvOptionCLetter; break;
            case 3: correctCard = cardOptionD; correctLetter = tvOptionDLetter; break;
            default: return;
        }

        correctCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.quiz_correct_light));
        correctLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quiz_correct_dark));
        correctLetter.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void showResults() {
        long endTime = System.currentTimeMillis();
        int durationMillis = (int) (endTime - startTime);
        int durationMinutes = durationMillis / 60000;

        StudySessionEntity session = new StudySessionEntity(sessionId, null, "quiz");
        session.setScore(score);
        session.setDurationMinutes(durationMinutes);
        session.setEndedAt(endTime);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            db.studySessionDao().insertSession(session);
            new FirestoreService(db.studySessionDao()).syncStudySession(session);
        });

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", questionList.size());
        startActivity(intent);
        finish();
    }
}
