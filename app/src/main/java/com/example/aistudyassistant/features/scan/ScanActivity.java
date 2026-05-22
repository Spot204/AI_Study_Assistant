package com.example.aistudyassistant.features.scan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.network.ApiClient;
import com.example.aistudyassistant.network.models.ScanRequest;
import com.example.aistudyassistant.network.models.ScanResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PIC_REQUEST = 1337;
    private ImageView imgPreview;
    private EditText edtHandWrittenNotes;
    private Button btnCapture, btnUploadScan;
    private ProgressBar loadingBar;
    private TextView tvOcrResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        imgPreview = findViewById(R.id.imgScanPreview);
        edtHandWrittenNotes = findViewById(R.id.edtScanManually);
        btnCapture = findViewById(R.id.btnCapturePhoto);
        btnUploadScan = findViewById(R.id.btnProcessOcrServer);
        loadingBar = findViewById(R.id.scanLoadingBar);
        tvOcrResult = findViewById(R.id.tvScanMetricsOutput);

        btnCapture.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            } else {
                Toast.makeText(this, "Không nhận diện được Camera phần cứng!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUploadScan.setOnClickListener(v -> {
            String documentContent = edtHandWrittenNotes.getText().toString().trim();
            if (documentContent.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tay văn bản hoặc chụp ảnh tài liệu trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingBar.setVisibility(View.VISIBLE);
            tvOcrResult.setText("Đang phân tích và xử lý qua Server Spring Boot & Trí tuệ nhận tạo Gemini...");

            ApiClient.getService().scanNotes(new ScanRequest(documentContent, "Môn Học Tổng Hợp"))
                    .enqueue(new Callback<ScanResponse>() {
                        @Override
                        public void onResponse(Call<ScanResponse> call, Response<ScanResponse> response) {
                            loadingBar.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                ScanResponse dataResponse = response.body();
                                tvOcrResult.setText("Phân tích hoàn tất!\n" +
                                        "Tóm tắt: " + dataResponse.getData().getSummary() + "\n\n" +
                                        "Số Flashcards trích xuất: " + dataResponse.getData().getFlashcards().size() + "\n" +
                                        "Số Trắc nghiệm thiết lập: " + dataResponse.getData().getQuiz().size());
                            } else {
                                tvOcrResult.setText("Yêu cầu thất bại hoặc dịch vụ Gemini quá tải. Vui lòng thử lại!");
                            }
                        }

                        @Override
                        public void onFailure(Call<ScanResponse> call, Throwable t) {
                            loadingBar.setVisibility(View.GONE);
                            tvOcrResult.setText("Lỗi kết nối máy chủ: " + t.getLocalizedMessage());
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imgPreview.setVisibility(View.VISIBLE);
            imgPreview.setImageBitmap(image);
            edtHandWrittenNotes.setText("Đã nạp gói hình ảnh từ Camera. Nhấp 'Phân tích thông qua Spring Boot' để bóc tách tri thức.");
        }
    }
}