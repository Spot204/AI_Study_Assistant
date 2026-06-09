package com.example.aistudyassistant.fragments.flashcard;

import android.os.Bundle;
import com.example.aistudyassistant.features.ocr_summary.OCRSummaryActivity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.features.flashcard.FlashcardDeckAdapter;
import com.example.aistudyassistant.services.profile.ProfileService;

public class FlashcardFragment extends Fragment {

    private RecyclerView rvDecks;
    private FlashcardDeckAdapter adapter;
    private ProfileService profileService;
    private AppDatabase db;
    private ImageButton btnBack, btnAdd;

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
        rvDecks.setAdapter(adapter);

        profileService.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                loadStudySets(user.getUserId());
            }
        });

        view.findViewById(R.id.btn_fc_ai_create).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), OCRSummaryActivity.class));
        });

        return view;
    }

    private void loadStudySets(String userId) {
        db.studySetDao().getAllSetsByUser(userId).observe(getViewLifecycleOwner(), sets -> {
            if (sets != null) {
                adapter.setStudySets(sets);
            }
        });
    }
}
