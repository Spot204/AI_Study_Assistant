package com.example.aistudyassistant.fragments.quiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudyassistant.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

// Thư viện ML Kit xử lý đọc chữ từ ảnh (Bản Bundled đóng gói sẵn)
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QuizFragment extends Fragment {

    private RecyclerView rvQuizzes;
    private TextView tvEmptyQuizzes;
    private MaterialButton btnCreateAi;

    // Bộ công cụ đời mới quản lý mở Camera, Thư viện và Chọn File hệ thống
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Void> takePhotoLauncher;
    private ActivityResultLauncher<String[]> openFileLauncher; // Launcher chuyên đi săn File

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. LUỒNG LẤY ẢNH TỪ THƯ VIỆN
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) runTextRecognitionFromUri(uri);
                }
        );

        // 2. LUỒNG CHỤP ẢNH TỪ CAMERA
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) runTextRecognitionFromBitmap(bitmap);
                }
        );

        // 3. LUỒNG CHỌN FILE HỆ THỐNG (PDF hoặc TXT)
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) handleImportedFile(uri);
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_quiz_list, container, false);

        rvQuizzes = view.findViewById(R.id.rv_quizzes);
        tvEmptyQuizzes = view.findViewById(R.id.tv_empty_quizzes);
        btnCreateAi = view.findViewById(R.id.btnCreateAi);

        // Chọc nút Tạo mới gọi ra cái Bottom Sheet xịn của bro
        btnCreateAi.setOnClickListener(v -> showCreateOptionsBottomSheet());

        tvEmptyQuizzes.setVisibility(View.VISIBLE);
        rvQuizzes.setVisibility(View.GONE);

        return view;
    }

    private void showCreateOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_import_options_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        View btnCamera = sheetView.findViewById(R.id.layout_option_camera);
        View btnGallery = sheetView.findViewById(R.id.layout_option_gallery);
        View btnFile = sheetView.findViewById(R.id.layout_option_file); // Khớp chuẩn 100% ID ô Tải tài liệu của bro

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                takePhotoLauncher.launch(null);
            });
        }

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                pickImageLauncher.launch("image/*");
            });
        }

        if (btnFile != null) {
            btnFile.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                // Kích hoạt bộ chọn tệp, chỉ cho hiển thị PDF và TXT
                openFileLauncher.launch(new String[]{"application/pdf", "text/plain"});
            });
        }

        bottomSheetDialog.show();
    }

    // Phân luồng tệp tin dựa vào định dạng MIME Type
    private void handleImportedFile(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType != null && mimeType.equals("application/pdf")) {
            readTextFromPdf(uri);
        } else {
            readTextFromTxt(uri);
        }
    }

    // 📄 LUỒNG XỬ LÝ 1: Đọc tệp tin .TXT thô
    private void readTextFromTxt(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            // 🛠️ ĐÃ SỬA: Thay != -1 bằng != null để khớp chuẩn kiểu dữ liệu String
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();

            String txtContent = sb.toString().trim();
            if (!txtContent.isEmpty()) {
                openOcrSummaryActivity(txtContent);
            } else {
                Toast.makeText(getContext(), "Tệp tin văn bản trống rỗng!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file TXT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    // 📕 LUỒNG XỬ LÝ 2: Đọc tệp tin .PDF bằng cơ chế rã trang thành ảnh rồi đẩy qua ML Kit
    private void readTextFromPdf(Uri uri) {
        try {
            // Bản chất Uri từ bộ chọn là luồng Stream, PdfRenderer bắt buộc phải đọc file vật lý (seekable).
            // Do đó ta tạo 1 file tạm thời trong bộ nhớ Cache để copy luồng dữ liệu sang.
            File tempFile = new File(requireContext().getCacheDir(), "temp_import.pdf");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            // Khởi tạo PdfRenderer chính chủ Android đọc file tạm
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(pfd);
            int totalPages = renderer.getPageCount();

            StringBuilder fullPdfText = new StringBuilder();
            // Kích hoạt luồng đệ quy bất đồng bộ an toàn để quét từng trang một sequentially
            processPdfPage(renderer, 0, totalPages, fullPdfText);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi giải mã PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm đệ quy bất đồng bộ xử lý tuần tự từng trang PDF, chống xung đột luồng và tràn RAM máy ảo
    private void processPdfPage(PdfRenderer renderer, int currentPage, int totalPages, StringBuilder accumulatedText) {
        if (currentPage >= totalPages) {
            // Khi quét tới trang cuối cùng, đóng bộ nhớ và bắn cục chữ sang đầu não AI
            renderer.close();
            String finalDocText = accumulatedText.toString().trim();
            if (!finalDocText.isEmpty()) {
                openOcrSummaryActivity(finalDocText);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy nội dung chữ viết trong file PDF này!", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // Mở trang hiện tại
        PdfRenderer.Page page = renderer.openPage(currentPage);

        // Render trang PDF thành ảnh Bitmap chất lượng cao (Scale x2 resolution để ML Kit đọc chữ nét nhất)
        int width = page.getWidth() * 2;
        int height = page.getHeight() * 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(android.graphics.Color.WHITE); // Đổ nền trắng cho trang
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close(); // Đóng trang ngay sau khi chụp được ảnh để giải phóng RAM

        // Ném ảnh vừa chụp của trang PDF vào ML Kit để bóc chữ
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    accumulatedText.append(visionText.getText()).append("\n");
                    // Chạy tiếp sang trang kế cận
                    processPdfPage(renderer, currentPage + 1, totalPages, accumulatedText);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỡ một trang bị lỗi (ví dụ trang trắng), bỏ qua và cày tiếp trang sau
                    processPdfPage(renderer, currentPage + 1, totalPages, accumulatedText);
                });
    }

    // --- CÁC HÀM PHỤ TRỢ QUÉT ẢNH CAMERA/GALLERY (GIỮ NGUYÊN HOÀN HẢO) ---
    private void runTextRecognitionFromUri(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(requireContext(), uri);
            recognizeText(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTextRecognitionFromBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizeText(image);
    }

    private void recognizeText(InputImage image) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String resultText = visionText.getText().trim();
                    if (!resultText.isEmpty()) {
                        openOcrSummaryActivity(resultText);
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy ký tự chữ viết nào!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi ML Kit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Đầu ra tối ưu: Đóng gói chữ bóc được truyền thẳng sang phễu lọc Gemini của OCRSummaryActivity
    private void openOcrSummaryActivity(String cleanText) {
        try {
            Intent intent = new Intent(getActivity(), Class.forName("com.example.aistudyassistant.features.ocr_summary.OCRSummaryActivity"));
            intent.putExtra("TARGET_MODE", "QUIZ");
            intent.putExtra("EXTRACTED_TEXT", cleanText); // Găm cục chữ sạch sẽ vào đây
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}