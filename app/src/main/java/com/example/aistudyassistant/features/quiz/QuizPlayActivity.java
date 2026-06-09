package com.example.aistudyassistant.features.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aistudyassistant.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private long startTime;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
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

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        btnBack.setOnClickListener(v -> showExitConfirmation());
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
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

            tvQuestionNum.setText("Câu " + (currentQuestionIndex + 1));
            tvQuestion.setText(currentQuestion.getQuestionText());
            tvProgress.setText((currentQuestionIndex + 1) + "/" + questionList.size());
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
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            selectedLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            selectedLetter.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            selectedLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
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

        correctCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        correctLetter.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        correctLetter.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void showResults() {
        long endTime = System.currentTimeMillis();
        int durationMinutes = (int) ((endTime - startTime) / 60000);
        if (durationMinutes < 1) durationMinutes = 1; // Tối thiểu 1 phút để ghi nhận

        String quizId = getIntent().getStringExtra("QUIZ_ID");
        if (quizId == null) quizId = "default_quiz"; // Fallback

        int finalScore = score;
        int totalQuestions = questionList.size();
        int duration = durationMinutes;
        String finalQuizId = quizId;

        com.example.aistudyassistant.database.AppDatabase db = com.example.aistudyassistant.database.AppDatabase.getDatabase(this);
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.example.aistudyassistant.database.entities.StudySessionEntity session = 
                new com.example.aistudyassistant.database.entities.StudySessionEntity(
                    java.util.UUID.randomUUID().toString(),
                    "test_user_id", // Replace with real userId
                    "quiz",
                    finalQuizId,
                    System.currentTimeMillis(),
                    duration
                );
            session.setScore(finalScore + "/" + totalQuestions);
            db.studySessionDao().insertSession(session);

            // Cập nhật điểm cao nhất
            com.example.aistudyassistant.database.entities.QuizEntity quiz = db.quizDao().getQuizById(finalQuizId);
            if (quiz != null) {
                int scorePercent = (finalScore * 100 / totalQuestions);
                if (scorePercent > quiz.getBestScore()) {
                    quiz.setBestScore(scorePercent);
                    db.quizDao().updateQuiz(quiz);
                }
            }

            // Cập nhật mục tiêu học tập (ví dụ: cộng thêm thời gian học)
            List<com.example.aistudyassistant.database.entities.LearningGoalEntity> goals = db.learningGoalDao().getGoalsByUser("test_user_id");
            if (goals != null && !goals.isEmpty()) {
                com.example.aistudyassistant.database.entities.LearningGoalEntity dailyGoal = goals.get(0);
                dailyGoal.setCurrentValue(dailyGoal.getCurrentValue() + duration);
                db.learningGoalDao().updateGoal(dailyGoal);
            }
        });

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", questionList.size());
        intent.putExtra("TIME", tvTimer.getText().toString());
        startActivity(intent);
        finish();
    }
}

