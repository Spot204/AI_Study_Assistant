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

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.features.auth.AuthActivity;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;

public class RegisterFragment extends Fragment {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private AuthService authService;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtName = view.findViewById(R.id.edtRegisterName);
        edtEmail = view.findViewById(R.id.edtRegisterEmail);
        edtPassword = view.findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = view.findViewById(R.id.edtRegisterConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegisterSubmit);
        tvLoginLink = view.findViewById(R.id.tvLoginLink);
        progressBar = view.findViewById(R.id.registerProgressBar);

        authService = new AuthService();
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        userRepository = new UserRepository(db.userDao());

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            authService.Register(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String uid) {
                    User newUser = new User(uid, name, email);
                    userRepository.saveUser(newUser);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            // Quay lại màn hình đăng nhập
                            if (getActivity() instanceof AuthActivity) {
                                ((AuthActivity) getActivity()).replaceFragment(new LoginFragment());
                            }
                        });
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Đăng ký thất bại: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        tvLoginLink.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).replaceFragment(new LoginFragment());
            }
        });
    }
}
