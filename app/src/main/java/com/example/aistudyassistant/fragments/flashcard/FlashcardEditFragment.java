package com.example.aistudyassistant.fragments.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.features.ocr_summary.FlashcardReviewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlashcardEditFragment extends Fragment {

    private static final String ARG_SET_ID = "set_id";

    private String setId;
    private StudySetEntity studySet;
    private AppDatabase db;
    private FlashcardReviewAdapter adapter;

    private EditText etTitle;
    private RecyclerView rvCards;
    private Button btnSave, btnAddCard;
    private ImageButton btnBack;

    public static FlashcardEditFragment newInstance(String setId) {
        FlashcardEditFragment fragment = new FlashcardEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SET_ID, setId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setId = getArguments().getString(ARG_SET_ID);
        }
        db = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_edit, container, false);

        etTitle = view.findViewById(R.id.et_edit_title);
        rvCards = view.findViewById(R.id.rv_edit_cards);
        btnSave = view.findViewById(R.id.btn_edit_save);
        btnAddCard = view.findViewById(R.id.btn_edit_add_card);
        btnBack = view.findViewById(R.id.btn_edit_back);

        rvCards.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FlashcardReviewAdapter();
        rvCards.setAdapter(adapter);

        loadData();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnAddCard.setOnClickListener(v -> {
            List<FlashcardEntity> currentList = adapter.getList();
            String userId = (studySet != null) ? studySet.getUserId() : "";
            FlashcardEntity newCard = new FlashcardEntity(
                    UUID.randomUUID().toString(),
                    setId,
                    userId,
                    "",
                    ""
            );
            currentList.add(newCard);
            adapter.notifyItemInserted(currentList.size() - 1);
            rvCards.scrollToPosition(currentList.size() - 1);
        });

        btnSave.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void loadData() {
        new Thread(() -> {
            studySet = db.studySetDao().getSetById(setId);
            List<FlashcardEntity> cards = db.flashcardDao().getFlashcardsBySetIdSync(setId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (studySet != null) {
                        etTitle.setText(studySet.getTitle());
                        adapter.setData(new ArrayList<>(cards));
                    }
                });
            }
        }).start();
    }

    private void saveChanges() {
        String newTitle = etTitle.getText().toString().trim();
        if (newTitle.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tiêu đề bộ thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FlashcardEntity> updatedCards = adapter.getList();
        if (updatedCards.isEmpty()) {
            Toast.makeText(getContext(), "Bộ thẻ phải có ít nhất 1 thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // 1. Cập nhật tiêu đề bộ thẻ
            studySet.setTitle(newTitle);
            studySet.setUpdatedAt(System.currentTimeMillis());
            db.studySetDao().update(studySet);

            // 2. Xử lý các thẻ:
            // Cách đơn giản nhất: Xóa hết thẻ cũ của bộ này và chèn lại bộ mới (nếu không cần giữ tiến độ học tập)
            // Cách chuẩn: So sánh ID để Update/Insert/Delete. Ở đây ta sẽ giữ tiến độ bằng cách dùng danh sách hiện tại.
            
            // Lấy danh sách ID hiện tại trong DB để biết cái nào bị xóa
            List<String> existingCardIds = db.flashcardDao().getCardIdsBySetId(setId);
            List<String> updatedCardIds = new ArrayList<>();
            for (FlashcardEntity card : updatedCards) {
                updatedCardIds.add(card.getFlashcardId());
            }

            // Xóa những thẻ không còn trong danh sách mới
            for (String oldId : existingCardIds) {
                if (!updatedCardIds.contains(oldId)) {
                    db.flashcardDao().deleteById(oldId);
                }
            }

            // Cập nhật hoặc thêm mới
            for (FlashcardEntity card : updatedCards) {
                card.setUpdatedAt(System.currentTimeMillis());
                db.flashcardDao().insertOrUpdate(card);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        }).start();
    }
}