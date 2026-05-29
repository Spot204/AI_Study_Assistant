package com.example.aistudyassistant.features.profile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtBio, edtSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_edit);

        // Khởi tạo views
        initViews();

        // Nút quay lại
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Nút lưu
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfileData());
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edt_full_name);
        edtEmail = findViewById(R.id.edt_email);
        edtBio = findViewById(R.id.edt_bio);
        edtSchool = findViewById(R.id.edt_school);
    }

    private void saveProfileData() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String bio = edtBio.getText().toString().trim();
        String school = edtSchool.getText().toString().trim();

        // Kiểm tra dữ liệu trống (Validation)
        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Họ tên và Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Thực hiện lưu dữ liệu vào Firebase Firestore hoặc SQLite thông qua Repository
        // Tạm thời hiển thị Toast và đóng Activity
        
        Toast.makeText(this, "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
