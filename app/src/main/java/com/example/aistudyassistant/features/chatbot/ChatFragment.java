package com.example.aistudyassistant.features.chatbot;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.ChatMessage;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
// [ĐÃ THÊM] Sử dụng Repository chuẩn
import com.example.aistudyassistant.data.repository.StudySessionRepository;
import com.example.aistudyassistant.services.core.LLMService;

import java.util.ArrayList;
import java.util.UUID;

public class ChatFragment extends Fragment {
    private EditText edtChat;
    private ImageView btnSend;
    private LinearLayout chatContainer;

    private LLMService llmService;
    private StudySessionRepository sessionRepo; // Thay thế cho FirestoreService và AppDatabase
    private StudySessionEntity currentSession;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isAiReady = false; // Biến kiểm soát trạng thái nạp Model

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtChat = view.findViewById(R.id.edtChat);
        btnSend = view.findViewById(R.id.btnSend);
        chatContainer = view.findViewById(R.id.chatContainer);

        Context appContext = requireContext().getApplicationContext();

        // 1. KHỞI TẠO REPOSITORY
        sessionRepo = new StudySessionRepository(AppDatabase.getDatabase(appContext).studySessionDao());

        // 2. KHỞI TẠO PHIÊN CHAT VÀ LƯU VÀO REPOSITORY (Tự động lo Local + Cloud)
        currentSession = new StudySessionEntity(UUID.randomUUID().toString(), null, "chat");
        currentSession.setMessages(new ArrayList<>());
        currentSession.setStartedAt(System.currentTimeMillis());
        sessionRepo.insertSession(currentSession);

        // 3. HIỂN THỊ TRẠNG THÁI NẠP MODEL
        TextView loadingView = addMessage("Hệ thống đang nạp model AI vào RAM. Vui lòng đợi trong giây lát...", false);
        btnSend.setEnabled(false); // Khóa nút gửi khi chưa load xong

        // 4. GỌI AI KHỞI ĐỘNG CHẠY NGẦM
        llmService = new LLMService();
        llmService.initializeModelAsync(appContext, new LLMService.InitializationCallback() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> {
                    isAiReady = true;
                    btnSend.setEnabled(true); // Mở khóa nút gửi
                    chatContainer.removeView(loadingView);
                    addMessage("Model AI đã sẵn sàng! Bạn cần tôi giúp gì?", false);
                });
            }

            @Override
            public void onError(String errorMsg) {
                mainHandler.post(() -> {
                    chatContainer.removeView(loadingView);
                    addMessage("Lỗi khởi tạo AI: " + errorMsg, false);
                });
            }
        });

        // Tự động bật bàn phím
        if (edtChat != null) {
            // Yêu cầu tập trung vào ô nhập liệu
            edtChat.requestFocus();

            // Hiển thị bàn phím ảo tự động sau một khoảng trễ ngắn
            edtChat.postDelayed(() -> {
                if (getContext() != null) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(edtChat, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 200);
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        if (!isAiReady) {
            Toast.makeText(getContext(), "AI đang nạp dữ liệu, vui lòng đợi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = edtChat.getText().toString().trim();
        if (message.isEmpty()) return;

        // Hiển thị tin nhắn User
        addMessage(message, true);
        edtChat.setText("");

        // Hiển thị trạng thái đang xử lý và khóa spam
        TextView typingView = addMessage("AI đang suy nghĩ...", false);
        btnSend.setEnabled(false);

        // Gọi AI xử lý ngầm qua luồng Async
        llmService.generateResponseAsync(message, response -> {
            mainHandler.post(() -> {
                chatContainer.removeView(typingView);
                addMessage(response, false);
                btnSend.setEnabled(true); // Mở lại nút gửi
            });
        });
    }

    private TextView addMessage(String text, boolean isUser) {
        if (!isAdded() || getContext() == null) return null;

        // Chỉ lưu tin nhắn thật vào DB (Bỏ qua các thông báo hệ thống như "đang nạp", "đang suy nghĩ")
        if (!text.contains("Hệ thống đang nạp") && !text.equals("AI đang suy nghĩ...")) {
            currentSession.getMessages().add(new ChatMessage(text, isUser, System.currentTimeMillis()));
            // Đẩy phần cập nhật lưu trữ cho Repository xử lý ngầm
            sessionRepo.updateSession(currentSession);
        }

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(16);
        textView.setPadding(32, 24, 32, 24);
        textView.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);

        if (isUser) {
            params.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.bg_chat_bubble_user);
        } else {
            params.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_chat_bubble_ai);
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);

        // Cuộn xuống dòng cuối cùng
        if (getView() != null) {
            NestedScrollView chatScrollView = getView().findViewById(R.id.chatScrollView);
            if (chatScrollView != null) {
                chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
            }
        }
        return textView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentSession.setEndedAt(System.currentTimeMillis());

        // Bàn giao toàn bộ việc lưu phiên cuối cùng cho Repository
        if (sessionRepo != null) {
            sessionRepo.updateSession(currentSession);
        }

        // Đóng Model để giải phóng vài GB RAM cho điện thoại
        if (llmService != null) {
            llmService.close();
        }
    }
}