package com.example.aistudyassistant.fragments.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.QuizEntity;
import com.example.aistudyassistant.fragments.quiz.QuizPlayActivity;
import java.util.List;

public class QuizFragment extends Fragment {

    private ConstraintLayout quizCard1;
    private TextView quizTitle1, quizInfo1;

    public QuizFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.activity_quiz_list,
                container,
                false
        );

        quizCard1 = view.findViewById(R.id.quizCard1);
        quizTitle1 = view.findViewById(R.id.quizTitle1);
        quizInfo1 = view.findViewById(R.id.quizInfo1);

        loadQuizzes();

        return view;
    }

    private void loadQuizzes() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        // Lấy User đầu tiên để demo dữ liệu động
        db.userDao().getAnyUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                new Thread(() -> {
                    List<QuizEntity> quizzes = db.quizDao().getQuizzesByUser(user.getUserId());
                    if (!quizzes.isEmpty()) {
                        QuizEntity firstQuiz = quizzes.get(0);
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                quizTitle1.setText(firstQuiz.getTitle());
                                quizInfo1.setText(firstQuiz.getTimeLimitMinutes() + " phút");
                                quizCard1.setOnClickListener(v -> {
                                    Intent intent = new Intent(getActivity(), QuizPlayActivity.class);
                                    intent.putExtra("QUIZ_ID", firstQuiz.getQuizId());
                                    startActivity(intent);
                                });
                            });
                        }
                    } else {
                        // Nếu chưa có quiz nào trong DB, dùng mặc định
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                quizCard1.setOnClickListener(v -> {
                                    Intent intent = new Intent(getActivity(), QuizPlayActivity.class);
                                    startActivity(intent);
                                });
                            });
                        }
                    }
                }).start();
            }
        });
    }
}
