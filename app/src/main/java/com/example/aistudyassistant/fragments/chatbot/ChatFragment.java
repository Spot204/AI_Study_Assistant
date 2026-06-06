package com.example.aistudyassistant.fragments.chatbot;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.aistudyassistant.data.repository.StudySessionRepository;
import com.example.aistudyassistant.services.core.LLMService;

import java.util.ArrayList;
import java.util.UUID;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private EditText edtChat;
    private ImageView btnSend;
    private LinearLayout chatContainer;

    private LLMService llmService;
    private StudySessionRepository sessionRepo;
    private StudySessionEntity currentSession;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isAiReady = false;

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

        sessionRepo = new StudySessionRepository(AppDatabase.getDatabase(appContext).studySessionDao());

        currentSession = new StudySessionEntity(UUID.randomUUID().toString(), null, "chat");
        currentSession.setMessages(new ArrayList<>());
        currentSession.setStartedAt(System.currentTimeMillis());
        sessionRepo.insertSession(currentSession);
        Log.d(TAG, "Created new chat session: " + currentSession.getSessionId());

        TextView connectingView = addMessage("Đang kết nối với trí tuệ nhân tạo...", false);
        btnSend.setEnabled(false);

        Log.i(TAG, "Initializing LLM Service...");
        llmService = new LLMService();
        llmService.initializeModelAsync(appContext, new LLMService.InitializationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "LLM Service initialized successfully");
                mainHandler.post(() -> {
                    isAiReady = true;
                    btnSend.setEnabled(true);
                    chatContainer.removeView(connectingView);
                    addMessage("Trợ lý AI đã sẵn sàng hỗ trợ học tập! Bạn cần tóm tắt hay giải đáp nội dung gì?", false);
                });
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "LLM Service initialization failed: " + errorMsg);
                mainHandler.post(() -> {
                    chatContainer.removeView(connectingView);
                    addMessage("Không thể kết nối AI: " + errorMsg, false);
                });
            }
        });

        if (edtChat != null) {
            edtChat.requestFocus();
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

        Log.d(TAG, "User sent message: " + message);

        // Lưu tin nhắn của User vào danh sách VÀ in ra màn hình
        addMessage(message, true);
        edtChat.setText("");

        TextView typingView = addMessage("AI đang suy nghĩ...", false);
        btnSend.setEnabled(false);

        // ĐÃ SỬA: Gọi đúng hàm đa lượt và truyền TOÀN BỘ danh sách tin nhắn vào
        llmService.generateChatResponseAsync(currentSession.getMessages(), response -> {
            Log.d(TAG, "AI response received: " + response);
            mainHandler.post(() -> {
                chatContainer.removeView(typingView);
                // Lưu tin nhắn của AI vào danh sách VÀ in ra màn hình
                addMessage(response, false);
                btnSend.setEnabled(true);
            });
        });
    }

    private TextView addMessage(String text, boolean isUser) {
        if (!isAdded() || getContext() == null) return null;

        // Chỉ lưu những tin nhắn thật (Bỏ qua mấy câu thông báo hệ thống)
        if (!text.contains("Đang kết nối") && !text.equals("AI đang suy nghĩ...")) {
            currentSession.getMessages().add(new ChatMessage(text, isUser, System.currentTimeMillis()));
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
        Log.i(TAG, "onDestroy: Saving session and closing LLM Service");
        currentSession.setEndedAt(System.currentTimeMillis());

        if (sessionRepo != null) {
            sessionRepo.updateSession(currentSession);
        }

        if (llmService != null) {
            llmService.close();
        }
    }
}