package com.example.aistudyassistant.fragments.auth;

import android.os.Bundle;
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
import com.example.aistudyassistant.services.auth.AuthService;
import com.example.aistudyassistant.features.auth.RegisterController;
import com.example.aistudyassistant.features.auth.RegisterView;


// Nhớ thêm implements RegisterView
public class RegisterFragment extends Fragment implements RegisterView {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    // Khai báo Controller
    private RegisterController registerController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        edtName = view.findViewById(R.id.edtRegisterName);
        edtEmail = view.findViewById(R.id.edtRegisterEmail);
        edtPassword = view.findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = view.findViewById(R.id.edtRegisterConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegisterSubmit);
        tvLoginLink = view.findViewById(R.id.tvLoginLink);
        progressBar = view.findViewById(R.id.registerProgressBar);

        // Khởi tạo Controller với các công cụ cần thiết
        AuthService authService = new AuthService();
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        UserRepository userRepository = new UserRepository(db.userDao());

        registerController = new RegisterController(this, authService, userRepository);

        // ============ CHỈ BẮT SỰ KIỆN CLICK ============

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // Giao toàn bộ dữ liệu cho Controller xử lý
            registerController.performRegister(name, email, password, confirmPassword);
        });

        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    // ============ CÁC HÀM THỰC THI GIAO DIỆN TỪ REGISTER_VIEW ============

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
    public void navigateToLogin() {
        if (getActivity() instanceof MainActivity) {
            getActivity().runOnUiThread(() -> {
                ((MainActivity) getActivity()).loadFragment(new LoginFragment());
            });
        }
    }
}