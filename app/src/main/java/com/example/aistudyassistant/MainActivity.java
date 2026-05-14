package com.example.aistudyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.features.profile.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
