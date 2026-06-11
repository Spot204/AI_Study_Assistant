package com.example.aistudyassistant.fragments.flashcard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.fragments.flashcard.FlashcardDeckAdapter;
import com.example.aistudyassistant.features.ocr_summary.OCRSummaryActivity;
import com.example.aistudyassistant.services.ocr_summary.OCRService;
import com.example.aistudyassistant.services.profile.ProfileService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

// Thư viện bổ sung phục vụ cấu trúc rã tệp tin và ML Kit cục bộ cho PDF
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FlashcardFragment extends Fragment {

    private RecyclerView rvDecks;
    private FlashcardDeckAdapter adapter;
    private ProfileService profileService;
    private AppDatabase db;
    private ImageButton btnAdd;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String[]> openFileLauncher; // ◄ 1. THÊM LAUNCHER CHỌN FILE

    private Uri currentPhotoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        db = AppDatabase.getDatabase(requireContext());
        profileService = new ProfileService(requireContext());

        rvDecks = view.findViewById(R.id.rv_fc_decks);
        btnAdd = view.findViewById(R.id.btn_fc_add);

        rvDecks.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FlashcardDeckAdapter();
        adapter.setOnDeckClickListener(studySet -> {
            FlashcardStudyFragment studyFragment = FlashcardStudyFragment.newInstance(
                    studySet.getSetId(),
                    studySet.getTitle()
            );
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, studyFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvDecks.setAdapter(adapter);

        profileService.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) loadStudySets(user.getUserId());
        });

        setupLaunchers();

        view.findViewById(R.id.btn_fc_ai_create).setOnClickListener(v -> showImportOptionsBottomSheet());
        btnAdd.setOnClickListener(v -> showImportOptionsBottomSheet());

        return view;
    }

    private void loadStudySets(String userId) {
        db.studySetDao().getAllSetsByUser(userId).observe(getViewLifecycleOwner(), sets -> {
            if (sets != null) adapter.setStudySets(sets);
        });
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) processImageWithOCR(uri); }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoUri != null) {
                        processImageWithOCR(currentPhotoUri);
                    } else {
                        Toast.makeText(getContext(), "Đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(getContext(), "Bạn cần cấp quyền Camera để chụp ảnh!", Toast.LENGTH_SHORT).show();
                }
        );

        // ◄ 2. ĐĂNG KÝ BỘ HỨNG TỆP TIN PDF / TXT TỪ HỆ THỐNG
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> { if (uri != null) handleImportedFile(uri); }
        );
    }

    private void openCamera() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "ocr_image_" + System.currentTimeMillis() + ".jpg");
            currentPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(currentPhotoUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khởi tạo Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void processImageWithOCR(Uri imageUri) {
        Toast.makeText(getContext(), "Đang quét chữ, vui lòng đợi...", Toast.LENGTH_LONG).show();

        OCRService ocrService = new OCRService();
        ocrService.extractTextFromImage(requireContext(), imageUri, new OCRService.OCRCallback() {
            @Override
            public void onSuccess(String extractedText) {
                if (getActivity() == null) return;
                openOcrSummaryActivity(extractedText);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void showImportOptionsBottomSheet() {
        if (getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_import_options_bottom_sheet, null);

        ConstraintLayout layoutOptionFile = bottomSheetView.findViewById(R.id.layout_option_file);
        ConstraintLayout layoutOptionCamera = bottomSheetView.findViewById(R.id.layout_option_camera);
        ConstraintLayout layoutOptionGallery = bottomSheetView.findViewById(R.id.layout_option_gallery);

        // 🛠️ 3. LUỒNG ĐÃ SỬA: Thay đổi Toast "Đang phát triển" bằng lệnh kích hoạt File Picker thật
        layoutOptionFile.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openFileLauncher.launch(new String[]{"application/pdf", "text/plain"});
        });

        layoutOptionCamera.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        layoutOptionGallery.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            galleryLauncher.launch("image/*");
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // =========================================================================
    // 🔮 BỘ CORE XỬ LÝ ĐỌC FILE VÀ RÃ NHỮNG TRANG PDF THÀNH CHỮ CHUẨN CHỈ
    // =========================================================================

    private void handleImportedFile(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType != null && mimeType.equals("application/pdf")) {
            readTextFromPdf(uri);
        } else {
            readTextFromTxt(uri);
        }
    }

    private void readTextFromTxt(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            // Áp dụng chuẩn logic kết thúc chuỗi bằng null tránh lỗi toán tử int lúc nãy
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

    private void readTextFromPdf(Uri uri) {
        try {
            File tempFile = new File(requireContext().getCacheDir(), "temp_fc_import.pdf");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(pfd);
            int totalPages = renderer.getPageCount();

            StringBuilder fullPdfText = new StringBuilder();
            processPdfPage(renderer, 0, totalPages, fullPdfText);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi giải mã PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void processPdfPage(PdfRenderer renderer, int currentPage, int totalPages, StringBuilder accumulatedText) {
        if (currentPage >= totalPages) {
            renderer.close();
            String finalDocText = accumulatedText.toString().trim();
            if (!finalDocText.isEmpty()) {
                openOcrSummaryActivity(finalDocText);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu chữ viết trong PDF!", Toast.LENGTH_LONG).show();
            }
            return;
        }

        PdfRenderer.Page page = renderer.openPage(currentPage);
        int width = page.getWidth() * 2;
        int height = page.getHeight() * 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(android.graphics.Color.WHITE);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    accumulatedText.append(visionText.getText()).append("\n");
                    processPdfPage(renderer, currentPage + 1, totalPages, accumulatedText);
                })
                .addOnFailureListener(e -> processPdfPage(renderer, currentPage + 1, totalPages, accumulatedText));
    }

    // Hàm phụ trợ đóng gói tập trung, bắn luồng dữ liệu kèm cờ hiệu FLASHCARD sang đầu não AI
    private void openOcrSummaryActivity(String cleanText) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            Intent intent = new Intent(requireContext(), OCRSummaryActivity.class);
            intent.putExtra("EXTRACTED_TEXT", cleanText);
            intent.putExtra("TARGET_MODE", "FLASHCARD"); // Ép chặt chế độ chạy Flashcard
            startActivity(intent);
        });
    }
}