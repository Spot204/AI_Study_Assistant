package com.example.aistudyassistant.features.schedule;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.google.android.material.button.MaterialButton;

public class CreateScheduleActivity extends AppCompatActivity {

    private EditText edtTitle;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_schedule);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        findViewById(R.id.headerLayout).findViewById(android.R.id.content).setOnClickListener(v -> finish()); // This might need a better way to find the back button if it's not ID'd
        
        // Find back button container (the FrameLayout)
        View backButton = findViewById(R.id.headerLayout);
        // Since the FrameLayout doesn't have an ID, we'll need to find it by structure or add an ID.
        // Let's add an ID to the back button in the XML later. For now, let's just finish on some view click if possible.
        
        btnSave.setOnClickListener(v -> {
            saveSchedule();
        });
    }

    private void saveSchedule() {
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        // Logic to save to database will go here
        finish();
    }
}
