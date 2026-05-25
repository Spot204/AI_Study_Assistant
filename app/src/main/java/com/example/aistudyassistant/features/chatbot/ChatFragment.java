package com.example.aistudyassistant.features.chatbot;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;

public class ChatFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtChat = view.findViewById(R.id.edtChat);
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
    }

}
