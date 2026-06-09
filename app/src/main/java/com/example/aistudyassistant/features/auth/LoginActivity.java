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
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.features.profile.User;
import com.example.aistudyassistant.network.ApiClient;
import com.example.aistudyassistant.network.models.LoginRequest;
import com.example.aistudyassistant.network.models.AuthResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getDatabase(this);
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

            // BƯỚC 1: KIỂM TRA LOCAL TRƯỚC
            executorService.execute(() -> {
                User localUser = db.userDao().getUserByEmail(email);
                
                if (localUser != null) {
                    // Tìm thấy trong Local -> Đăng nhập ngay
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Đăng nhập nhanh (Offline): " + localUser.getFullName(), Toast.LENGTH_SHORT).show();
                        navigateToMain(localUser.getFullName(), localUser.getEmail());
                    });
                } else {
                    // BƯỚC 2: KHÔNG THẤY LOCAL -> KIỂM TRA REMOTE (FIREBASE/API)
                    runOnUiThread(() -> performRemoteLogin(email, password));
                }
            });
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performRemoteLogin(String email, String password) {
        ApiClient.getService().login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String name = response.body().getUser().getName();
                            String userId = String.valueOf(response.body().getUser().getId());

                            // BƯỚC 3: ĐĂNG NHẬP REMOTE THÀNH CÔNG -> LƯU VÀO LOCAL CHO LẦN SAU
                            saveUserToLocal(userId, name, email);

                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain(name, email);
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối máy chủ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToLocal(String userId, String name, String email) {
        executorService.execute(() -> {
            User newUser = new User(userId, name, email);
            db.userDao().insertUser(newUser);
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
