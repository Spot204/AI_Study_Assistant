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
import com.example.aistudyassistant.MainActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    private AuthService authService;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();
        AppDatabase db = AppDatabase.getDatabase(this);
        userRepository = new UserRepository(db.userDao());

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản/mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Admin login check
            if (email.equals("admin@gmail.com") && password.equals("123456")) {
                User defaultUser = new User("default_id", "Admin User", "admin@gmail.com");
                userRepository.saveUser(defaultUser);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Đăng nhập mặc định thành công!", Toast.LENGTH_SHORT).show();
                navigateToMain("Admin User", "admin@gmail.com");
                return;
            }

            authService.Login(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String uid) {
                    String name = email.split("@")[0];
                    User user = new User(uid, name, email);
                    userRepository.saveUser(user);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToMain(name, email);
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
