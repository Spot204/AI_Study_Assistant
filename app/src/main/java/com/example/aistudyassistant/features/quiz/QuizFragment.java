package com.example.aistudyassistant.features.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.R;

public class QuizFragment extends Fragment {

    ConstraintLayout quizCard1;

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

        quizCard1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuizPlayActivity.class);
            startActivity(intent);
        });

        return view;
    }
}