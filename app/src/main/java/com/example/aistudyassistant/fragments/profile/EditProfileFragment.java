package com.example.aistudyassistant.fragments.profile; // Nhớ sửa lại package cho đúng với thư mục của bạn

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.R;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    // Khai báo các thành phần giao diện
    private FrameLayout btnBack, btnSelectImage;
    private TextView btnSave;
    private ImageView ivAvatar;
    private EditText edtFullName, edtEmail, edtBio, edtSchool;

    // Biến lưu trữ Uri của ảnh sau khi chọn từ thư viện
    private Uri selectedImageUri = null;

    // Bộ khởi chạy (Launcher) để mở thư viện ảnh
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Phóng to ảnh tràn viền để lấp đầy khung tròn
                    ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivAvatar.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên file layout khớp với tên file XML của bạn
        return inflater.inflate(R.layout.profile_activity_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupEvents();
    }

    // 1. Ánh xạ tất cả các View
    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);
        btnSelectImage = view.findViewById(R.id.selectimage);
        ivAvatar = view.findViewById(R.id.iv_avatar);

        edtFullName = view.findViewById(R.id.edt_full_name);
        edtEmail = view.findViewById(R.id.edt_email);
        edtBio = view.findViewById(R.id.edt_bio);
        edtSchool = view.findViewById(R.id.edt_school);
    }

    // 2. Cài đặt các sự kiện Click
    private void setupEvents() {
        // Sự kiện quay lại (Back)
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Sự kiện mở thư viện chọn ảnh
        btnSelectImage.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        // Sự kiện Lưu dữ liệu
        btnSave.setOnClickListener(v -> saveProfileData());
    }

    // 3. Hàm xử lý logic khi bấm Lưu
    private void saveProfileData() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String bio = edtBio.getText().toString().trim();
        String school = edtSchool.getText().toString().trim();

        // Kiểm tra tính hợp lệ cơ bản
        if (fullName.isEmpty()) {
            edtFullName.setError("Họ và tên không được để trống");
            edtFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        // In ra Logcat để kiểm tra xem dữ liệu đã được gom đúng chưa
        Log.d(TAG, "--- BẮT ĐẦU LƯU HỒ SƠ ---");
        Log.d(TAG, "Họ tên: " + fullName);
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Mô tả: " + bio);
        Log.d(TAG, "Trường: " + school);
        if (selectedImageUri != null) {
            Log.d(TAG, "Có ảnh đại diện mới: " + selectedImageUri.toString());
        }

        /* * TODO: Tích hợp Firebase tại đây
         * 1. Nếu có ảnh mới (selectedImageUri != null):
         * -> Upload ảnh lên Firebase Storage trước.
         * -> Lấy link ảnh (Download URL) trả về.
         * -> Lưu link ảnh cùng với các thông tin (Tên, Email...) lên Firestore.
         * * 2. Nếu KHÔNG có ảnh mới:
         * -> Chỉ cập nhật các thông tin văn bản (Tên, Email...) lên thẳng Firestore.
         */

        Toast.makeText(requireContext(), "Đang lưu thông tin...", Toast.LENGTH_SHORT).show();

        // Tạm thời đóng Fragment sau khi gom dữ liệu thành công
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}