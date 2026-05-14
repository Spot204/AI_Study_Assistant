package com.example.aistudyassistant.features.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_main);

        // Header actions
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_settings).setOnClickListener(v -> 
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show());

        setupMenuItems();
        
        // Logout action
        findViewById(R.id.btn_logout).setOnClickListener(v -> 
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show());
    }

    private void setupMenuItems() {
        findViewById(R.id.menu_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.menu_notifications).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.menu_goals).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LearningGoalsActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.menu_help).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpSupportActivity.class);
            startActivity(intent);
        });
    }
}
