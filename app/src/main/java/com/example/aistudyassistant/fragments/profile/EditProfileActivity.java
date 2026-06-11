package com.example.aistudyassistant.fragments.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.services.profile.ProfileService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtBio, edtSchool;
    private ImageView ivAvatar;
    private FrameLayout btnSelectImage;
    private ProgressBar progressBar;
    private ProfileService profileService;
    private User currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_edit);

        profileService = new ProfileService(this);
        initCloudinary();

        initViews();
        loadProfileData();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfileData());
        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edt_full_name);
        edtEmail = findViewById(R.id.edt_email);
        edtBio = findViewById(R.id.edt_bio);
        edtSchool = findViewById(R.id.edt_school);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnSelectImage = findViewById(R.id.selectimage);
        
        // Thêm ProgressBar nếu chưa có trong layout thì có thể findViewById null, 
        // nhưng tôi sẽ giả định bạn muốn có nó để theo dõi tiến trình
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadProfileData() {
        profileService.getCurrentUser().observe(this, user -> {
            if (user != null) {
                this.currentUser = user;
                edtFullName.setText(user.getFullName());
                edtEmail.setText(user.getEmail());
                edtBio.setText(user.getBio());
                edtSchool.setText(user.getSchool());

                if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatarPath())
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivAvatar);
                }
            }
        });
    }

    private void saveProfileData() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        if (selectedImageUri != null) {
            uploadImageAndSave(selectedImageUri);
        } else {
            performSave(currentUser != null ? currentUser.getAvatarPath() : null);
        }
    }

    private void initCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "deb7bnxp6"); // Thay bằng cloud_name của bạn
        config.put("api_key", "498144459286259");       // Thay bằng api_key của bạn
        config.put("api_secret", "uzpgI6uJPyTcK7RToUqaJmsyEWc"); // Thay bằng api_secret của bạn
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Đã khởi tạo trước đó
        }
    }

    private void uploadImageAndSave(Uri uri) {
        String userId = (currentUser != null && currentUser.getUserId() != null)
                ? currentUser.getUserId() : UUID.randomUUID().toString();

        MediaManager.get().upload(uri)
                .option("folder", "avatars")
                .option("public_id", userId)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Bắt đầu upload");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d("Cloudinary", "Upload thành công: " + imageUrl);
                        performSave(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        showLoading(false);
                        Log.e("Cloudinary", "Lỗi: " + error.getDescription());
                        Toast.makeText(EditProfileActivity.this, "Lỗi upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }

    private void performSave(String avatarUrl) {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String bio = edtBio.getText().toString().trim();
        String school = edtSchool.getText().toString().trim();

        if (currentUser == null) {
            currentUser = new User(UUID.randomUUID().toString(), fullName, email, bio, school);
        } else {
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setBio(bio);
            currentUser.setSchool(school);
        }

        if (avatarUrl != null) currentUser.setAvatarPath(avatarUrl);

        profileService.saveUser(currentUser, () -> runOnUiThread(() -> {
            showLoading(false);
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }));
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
