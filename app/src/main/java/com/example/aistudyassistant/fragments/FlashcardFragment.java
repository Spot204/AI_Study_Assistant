package com.example.aistudyassistant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.List;
import java.util.concurrent.Executors;

public class FlashcardFragment extends Fragment {

    private RecyclerView rvDecks;
    private FlashcardDeckAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        db = AppDatabase.getDatabase(requireContext());
        rvDecks = view.findViewById(R.id.rv_fc_decks);
        rvDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new FlashcardDeckAdapter();
        adapter.setOnItemClickListener(studySet -> {
            Intent intent = new Intent(getActivity(), FlashcardStudyActivity.class);
            intent.putExtra("SET_ID", studySet.getSetId());
            intent.putExtra("SET_TITLE", studySet.getTitle());
            startActivity(intent);
        });
        rvDecks.setAdapter(adapter);

        loadDecks();

        return view;
    }

    private void loadDecks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // "test_user_id" is a placeholder until Auth is fully integrated
            List<StudySetEntity> decks = db.studySetDao().getAllSetsByUser("test_user_id");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setStudySets(decks));
            }
        });
    }
}
