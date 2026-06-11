package com.example.aistudyassistant.fragments.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.dao.StudySetDao;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FlashcardStudyFragment extends Fragment {

    private static final String ARG_SET_ID = "set_id";
    private static final String ARG_SET_TITLE = "set_title";

    private String setId;
    private String setTitle;
    private List<FlashcardEntity> flashcards = new ArrayList<>();
    private Set<String> masteredCards = new HashSet<>();
    private int currentIndex = 0;
    private boolean isShowingFront = true;

    private TextView tvTitle, tvProgressCount, tvCardLabel, tvCardContent;
    private ProgressBar pbProgress;
    private CardView cvFlashcard;
    private ImageButton btnBack, btnReset, btnPrev, btnNext;
    private Button btnWrong, btnRight;

    public static FlashcardStudyFragment newInstance(String setId, String setTitle) {
        FlashcardStudyFragment fragment = new FlashcardStudyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SET_ID, setId);
        args.putString(ARG_SET_TITLE, setTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setId = getArguments().getString(ARG_SET_ID);
            setTitle = getArguments().getString(ARG_SET_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_study, container, false);
        initViews(view);
        loadFlashcards();
        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_study_title);
        tvProgressCount = view.findViewById(R.id.tv_study_progress_count);
        tvCardLabel = view.findViewById(R.id.tv_card_label);
        tvCardContent = view.findViewById(R.id.tv_card_content);
        pbProgress = view.findViewById(R.id.pb_study_progress);
        cvFlashcard = view.findViewById(R.id.cv_flashcard);
        btnBack = view.findViewById(R.id.btn_study_back);
        btnReset = view.findViewById(R.id.btn_study_reset);
        btnPrev = view.findViewById(R.id.btn_study_prev);
        btnNext = view.findViewById(R.id.btn_study_next);
        btnWrong = view.findViewById(R.id.btn_study_wrong);
        btnRight = view.findViewById(R.id.btn_study_right);

        tvTitle.setText(setTitle);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        cvFlashcard.setOnClickListener(v -> {
            isShowingFront = !isShowingFront;
            updateCardDisplay();
        });

        btnNext.setOnClickListener(v -> nextCard());
        btnPrev.setOnClickListener(v -> prevCard());
        
        btnRight.setOnClickListener(v -> {
            if (!flashcards.isEmpty() && currentIndex < flashcards.size()) {
                masteredCards.add(flashcards.get(currentIndex).getFlashcardId());
            }
            nextCard();
        });

        btnWrong.setOnClickListener(v -> {
            if (!flashcards.isEmpty() && currentIndex < flashcards.size()) {
                masteredCards.remove(flashcards.get(currentIndex).getFlashcardId());
            }
            nextCard();
        });

        btnReset.setOnClickListener(v -> {
            currentIndex = 0;
            isShowingFront = true;
            masteredCards.clear();
            updateCardDisplay();
        });
    }

    private void loadFlashcards() {
        AppDatabase.getDatabase(requireContext()).flashcardDao()
                .getFlashcardsBySetLive(setId)
                .observe(getViewLifecycleOwner(), cards -> {
                    if (cards != null && !cards.isEmpty()) {
                        flashcards = cards;
                        updateCardDisplay();
                    } else {
                        Toast.makeText(getContext(), "Không có thẻ nào trong bộ này!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                });
    }

    private void updateCardDisplay() {
        if (flashcards.isEmpty()) return;

        FlashcardEntity currentCard = flashcards.get(currentIndex);
        if (isShowingFront) {
            tvCardLabel.setText("MẶT TRƯỚC");
            tvCardContent.setText(currentCard.getFront());
        } else {
            tvCardLabel.setText("MẶT SAU");
            tvCardContent.setText(currentCard.getBack());
        }

        tvProgressCount.setText((currentIndex + 1) + " / " + flashcards.size());
        pbProgress.setProgress((int) (((float) (currentIndex + 1) / flashcards.size()) * 100));
    }

    private void nextCard() {
        if (currentIndex < flashcards.size() - 1) {
            currentIndex++;
            isShowingFront = true;
            updateCardDisplay();
        } else {
            finishStudy();
        }
    }

    private void finishStudy() {
        if (flashcards.isEmpty()) return;

        int total = flashcards.size();
        int mastered = masteredCards.size();
        float percentage = ((float) mastered / total) * 100;

        android.content.Context context = getContext();
        if (context == null) return;

        // Use Repository to handle DB update and cloud sync
        AppDatabase db = AppDatabase.getDatabase(context);
        StudySetRepository repository = new StudySetRepository(db.studySetDao());
        
        new Thread(() -> {
            StudySetEntity set = db.studySetDao().getSetById(setId);
            if (set != null) {
                set.setMasteryPercentage(percentage);
                repository.updateSet(set);
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Hoàn thành bài học!")
                            .setMessage("Bạn đã thuộc " + mastered + " / " + total + " thẻ.\n" +
                                    "Tỷ lệ thuộc: " + String.format(Locale.getDefault(), "%.1f", percentage) + "%")
                            .setPositiveButton("Kết thúc", (dialog, which) -> {
                                if (isAdded()) {
                                    getParentFragmentManager().popBackStack();
                                }
                            })
                            .setCancelable(false)
                            .show();
                });
            }
        }).start();
    }

    private void prevCard() {
        if (currentIndex > 0) {
            currentIndex--;
            isShowingFront = true;
            updateCardDisplay();
        }
    }
}
