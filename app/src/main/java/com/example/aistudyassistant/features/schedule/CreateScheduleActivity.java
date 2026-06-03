package com.example.aistudyassistant.features.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.Calendar;
import java.util.Locale;

import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import java.util.concurrent.Executors;

public class CreateScheduleActivity extends AppCompatActivity {

    private EditText edtTitle;
    private MaterialButton btnSave;
    private MaterialCardView cardDate, cardStartTime, cardEndTime;
    private MaterialCardView typeReview, typeQuiz, typeFlashcard, typeReading;
    private TextView tvDate, tvStartTime, tvEndTime;
    private Calendar calendar;
    private String selectedType = "Review";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_schedule);

        calendar = Calendar.getInstance();
        initViews();
        setupListeners();
        updateDateText();
        updateTimeTexts();
        updateTypeSelection();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        btnSave = findViewById(R.id.btnSave);
        cardDate = findViewById(R.id.cardDate);
        cardStartTime = findViewById(R.id.cardStartTime);
        cardEndTime = findViewById(R.id.cardEndTime);
        tvDate = findViewById(R.id.tvDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        
        typeReview = findViewById(R.id.typeReview);
        typeQuiz = findViewById(R.id.typeQuiz);
        typeFlashcard = findViewById(R.id.typeFlashcard);
        typeReading = findViewById(R.id.typeReading);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        cardDate.setOnClickListener(v -> showDatePicker());
        cardStartTime.setOnClickListener(v -> showTimePicker(true));
        cardEndTime.setOnClickListener(v -> showTimePicker(false));

        typeReview.setOnClickListener(v -> { selectedType = "Review"; updateTypeSelection(); });
        typeQuiz.setOnClickListener(v -> { selectedType = "Quiz"; updateTypeSelection(); });
        typeFlashcard.setOnClickListener(v -> { selectedType = "Flashcard"; updateTypeSelection(); });
        typeReading.setOnClickListener(v -> { selectedType = "Reading"; updateTypeSelection(); });

        btnSave.setOnClickListener(v -> {
            saveSchedule();
        });
    }

    private void updateTypeSelection() {
        resetTypeBorders();
        MaterialCardView selected = null;
        switch (selectedType) {
            case "Review": selected = typeReview; break;
            case "Quiz": selected = typeQuiz; break;
            case "Flashcard": selected = typeFlashcard; break;
            case "Reading": selected = typeReading; break;
        }
        if (selected != null) {
            selected.setStrokeColor(getResources().getColor(R.color.black, getTheme()));
            selected.setStrokeWidth(6);
        }
    }

    private void resetTypeBorders() {
        int grayColor = 0xFFEEEEEE;
        
        typeReview.setStrokeColor(grayColor);
        typeReview.setStrokeWidth(2);
        typeQuiz.setStrokeColor(grayColor);
        typeQuiz.setStrokeWidth(2);
        typeFlashcard.setStrokeColor(grayColor);
        typeFlashcard.setStrokeWidth(2);
        typeReading.setStrokeColor(grayColor);
        typeReading.setStrokeWidth(2);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isStartTime) tvStartTime.setText(time);
                    else tvEndTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void updateDateText() {
        String myFormat = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, Locale.getDefault());
        tvDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeTexts() {
        tvStartTime.setText("09:00");
        tvEndTime.setText("10:00");
    }

    private void saveSchedule() {
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        String date = tvDate.getText().toString();
        String startTime = tvStartTime.getText().toString();
        String endTime = tvEndTime.getText().toString();

        ScheduleTask task = new ScheduleTask(title, startTime, endTime, selectedType, date);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getDatabase(this).scheduleDao().insertTask(task);
            runOnUiThread(this::finish);
        });
    }
}
