package com.example.aistudyassistant.features.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.google.android.material.button.MaterialButton;

public class QuizResultActivity extends AppCompatActivity {

    private TextView tvScorePercent, tvCorrectNum, tvIncorrectNum, tvTimeElapsed;
    private MaterialButton btnBackToMenu, btnRetryQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initViews();
        displayResults();

        btnBackToMenu.setOnClickListener(v -> finish());
        btnRetryQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPlayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        tvScorePercent = findViewById(R.id.tvScorePercent);
        tvCorrectNum = findViewById(R.id.tvCorrectNum);
        tvIncorrectNum = findViewById(R.id.tvIncorrectNum);
        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnRetryQuiz = findViewById(R.id.btnRetryQuiz);
    }

    private void displayResults() {
        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);
        String time = getIntent().getStringExtra("TIME");

        int percent = (total > 0) ? (score * 100 / total) : 0;
        tvScorePercent.setText(percent + "%");
        tvCorrectNum.setText(String.valueOf(score));
        tvIncorrectNum.setText(String.valueOf(total - score));
        tvTimeElapsed.setText(time != null ? time : "0:00");
    }
}
