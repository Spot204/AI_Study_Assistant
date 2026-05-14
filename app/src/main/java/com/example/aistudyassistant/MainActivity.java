package com.example.aistudyassistant;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.features.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.bottomNavigation);

        navigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                return true;

            } else if (id == R.id.nav_scan) {

                // mở ScanActivity
                // startActivity(new Intent(this, ScanActivity.class));
                return true;

            } else if (id == R.id.nav_study) {

                // mở StudyActivity
                return true;

            } else if (id == R.id.nav_practice) {

                // mở PracticeActivity
                return true;

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });

        // Điều hướng sang Profile
        findViewById(R.id.card_profile).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Các tính năng khác (Hiện tại chỉ thông báo chưa có Activity)
        findViewById(R.id.card_chatbot).setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng Chat AI đang phát triển", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.card_ocr).setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng Quét tài liệu đang phát triển", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.card_study).setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng Học tập đang phát triển", Toast.LENGTH_SHORT).show());
    }
}
