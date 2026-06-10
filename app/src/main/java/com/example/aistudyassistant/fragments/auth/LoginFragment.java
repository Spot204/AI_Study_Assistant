package com.example.aistudyassistant.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.MainActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.fragments.home.HomeFragment;
import com.example.aistudyassistant.services.auth.AuthService;
import com.example.aistudyassistant.features.auth.LoginController;
import com.example.aistudyassistant.features.auth.LoginView;

// Chú ý: Thêm "implements LoginView"
public class LoginFragment extends Fragment implements LoginView {

    private static final String TAG = "LoginFragment";
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;

    // Khai báo Controller
    private LoginController loginController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegisterLink = view.findViewById(R.id.tvRegisterLink);
        progressBar = view.findViewById(R.id.progressBar);

        // Khởi tạo Controller (Cung cấp đồ nghề cho nó)
        AuthService authService = new AuthService();
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        UserRepository userRepository = new UserRepository(db.userDao());

        loginController = new LoginController(this, authService, userRepository);

        // ============ CHỈ BẮT SỰ KIỆN CLICK ============

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            Log.d(TAG, "Login button clicked for email: " + email);

            // Đẩy hết việc khó cho Controller lo!
            loginController.performLogin(email, password);
        });

        tvRegisterLink.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterFragment());
            }
        });
    }

    // ============ CÁC HÀM THỰC THI GIAO DIỆN TỪ LOGIN_VIEW ============

    @Override
    public void showLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        }
    }

    @Override
    public void hideLoading() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        }
    }

    @Override
    public void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void showSuccess(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void navigateToMain(String name, String email) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // KIỂM TRA: Đảm bảo Activity chứa Fragment này đúng là MainActivity
                if (getActivity() instanceof MainActivity) {

                    // Gọi cái hàm thần thánh bên MainActivity để nó tự động đổi sang HomeFragment
                    // và làm hiện thanh Bottom Navigation lên!
                    ((MainActivity) getActivity()).navigateToHomeAfterAuth();

                }
            });
        }
    }
}