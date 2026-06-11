package com.example.aistudyassistant.fragments.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;

public class NotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_main);
        
        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }
    }

    public static class LearningGoalsActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile_activity_main);
            
            if (findViewById(R.id.btn_back) != null) {
                findViewById(R.id.btn_back).setOnClickListener(v -> finish());
            }
        }
    }
}
