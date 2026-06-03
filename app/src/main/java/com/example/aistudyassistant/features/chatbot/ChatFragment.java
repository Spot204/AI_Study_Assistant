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
import com.example.aistudyassistant.firebase.FirestoreService;
import com.example.aistudyassistant.services.LLMService;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {
    private EditText edtChat;
    private ImageView btnSend;
    private LinearLayout chatContainer;
    private LLMService llmService;
    private AppDatabase db;
    private FirestoreService firestoreService;
    private StudySessionEntity currentSession;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        
        // Khởi tạo Database và Firebase Service
        db = AppDatabase.getDatabase(appContext);
        firestoreService = new FirestoreService(db.studySessionDao());
        
        // Tạo phiên làm việc mới cho lần chat này
        currentSession = new StudySessionEntity(UUID.randomUUID().toString(), null, "chat");
        currentSession.setMessages(new ArrayList<>());

        executorService.execute(() -> {
            llmService = new LLMService(appContext);
            // Lưu phiên bắt đầu vào SQLite
            db.studySessionDao().insertSession(currentSession);
        });

        if (edtChat != null) {
            edtChat.requestFocus();
            edtChat.postDelayed(() -> {
                if (isAdded() && getContext() != null) {
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
        String message = edtChat.getText().toString().trim();
        if (message.isEmpty()) return;

        // 1. Hiển thị tin nhắn của User và lưu vào session
        addMessage(message, true);
        edtChat.setText("");

        // Hiển thị trạng thái đang xử lý
        TextView typingView = addMessage("AI đang suy nghĩ...", false);

        executorService.execute(() -> {
            if (llmService == null) {
                mainHandler.post(() -> {
                    chatContainer.removeView(typingView);
                    Toast.makeText(getContext(), "AI đang khởi tạo, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            String response = llmService.generateResponse(message);

            // 3. Cập nhật kết quả AI lên giao diện và lưu vào session
            mainHandler.post(() -> {
                chatContainer.removeView(typingView);
                // Xóa tin nhắn "đang suy nghĩ" khỏi danh sách nếu lỡ add vào (ở đây addMessage trả về TextView)
                // Trong code này addMessage cho "đang suy nghĩ" cũng gọi addMessage(), 
                // nhưng ta sẽ sửa addMessage để chỉ lưu tin nhắn thật.
                addMessage(response, false);
            });
        });
    }

    private TextView addMessage(String text, boolean isUser) {
        if (!isAdded() || getContext() == null) return null;

        // Lưu vào danh sách tin nhắn của session (bỏ qua tin nhắn "đang suy nghĩ")
        if (!text.equals("AI đang suy nghĩ...")) {
            currentSession.getMessages().add(new ChatMessage(text, isUser, System.currentTimeMillis()));
            // Cập nhật SQLite ở luồng ngầm
            executorService.execute(() -> db.studySessionDao().updateSession(currentSession));
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
        currentSession.setEndedAt(System.currentTimeMillis());
        
        executorService.execute(() -> {
            // Lưu lần cuối vào SQLite
            db.studySessionDao().updateSession(currentSession);
            
            // Đồng bộ lên Firebase
            firestoreService.syncStudySession(currentSession);

            if (llmService != null) {
                llmService.close();
            }
        });
        executorService.shutdown();
    }
}
