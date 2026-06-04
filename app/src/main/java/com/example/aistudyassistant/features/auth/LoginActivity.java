package com.example.aistudyassistant.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.MainActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;
import com.example.aistudyassistant.services.profile.ProfileService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    private AuthService authService;
    private ProfileService profileService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();
        profileService = new ProfileService(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập tài khoản/mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // TÀI KHOẢN MẶC ĐỊNH ĐỂ TEST NHANH
            if (email.equals("admin@gmail.com") && password.equals("123456")) {
                User defaultUser = new User("default_id", "Admin User", "admin@gmail.com");
                profileService.saveUser(defaultUser, () -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Đăng nhập mặc định thành công!", Toast.LENGTH_SHORT).show();
                        navigateToMain("Admin User", "admin@gmail.com");
                    });
                });
                return;
            }

            // BƯỚC 1: ĐĂNG NHẬP QUA FIREBASE
            authService.Login(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String uid) {
                    // Đăng nhập Firebase thành công, lưu thông tin vào Local Room
                    // Trong thực tế, bạn có thể lấy profile từ Firestore ở đây.
                    // Tạm thời lấy name từ email nếu không có thông tin profile.
                    String name = email.split("@")[0];
                    User user = new User(uid, name, email);
                    
                    profileService.saveUser(user, () -> {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain(name, email);
                        });
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }


    private void navigateToMain(String name, String email) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
