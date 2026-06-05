package com.example.aistudyassistant.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private AuthService authService;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new AuthService();
        AppDatabase db = AppDatabase.getDatabase(this);
        userRepository = new UserRepository(db.userDao());

        edtName = findViewById(R.id.edtRegisterName);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.registerProgressBar);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            authService.Register(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String uid) {
                    User newUser = new User(uid, name, email);
                    userRepository.saveUser(newUser);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        // Quay lại màn hình đăng nhập
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        tvLoginLink.setOnClickListener(v -> {
            finish(); // Quay lại LoginActivity
        });
    }
}
