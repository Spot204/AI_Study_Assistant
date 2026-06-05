package com.example.aistudyassistant.features.flashcard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.ArrayList;
import java.util.List;

public class FlashcardDeckAdapter extends RecyclerView.Adapter<FlashcardDeckAdapter.DeckViewHolder> {

    private List<StudySetEntity> studySets = new ArrayList<>();
    private OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onDeckClick(StudySetEntity studySet);
    }

    public void setOnDeckClickListener(OnDeckClickListener listener) {
        this.listener = listener;
    }

    public void setStudySets(List<StudySetEntity> studySets) {
        this.studySets = studySets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        StudySetEntity studySet = studySets.get(position);
        holder.tvTitle.setText(studySet.getTitle());
        holder.tvCount.setText(holder.itemView.getContext().getString(R.string.deck_card_count, studySet.getTotalCards()));
        holder.progressBar.setProgress((int) studySet.getMasteryPercentage());
        holder.tvProgressText.setText(holder.itemView.getContext().getString(R.string.deck_mastery_percent, (int) studySet.getMasteryPercentage()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeckClick(studySet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studySets.size();
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount, tvProgressText;
        ProgressBar progressBar;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_deck_title);
            tvCount = itemView.findViewById(R.id.tv_deck_count);
            tvProgressText = itemView.findViewById(R.id.tv_deck_progress_text);
            progressBar = itemView.findViewById(R.id.pb_deck_progress);
        }
    }
}
