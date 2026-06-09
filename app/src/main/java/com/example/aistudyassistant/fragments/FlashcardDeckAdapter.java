package com.example.aistudyassistant.fragments;

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

public class FlashcardDeckAdapter extends RecyclerView.Adapter<FlashcardDeckAdapter.ViewHolder> {

    private List<StudySetEntity> studySets = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(StudySetEntity studySet);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setStudySets(List<StudySetEntity> studySets) {
        this.studySets = studySets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_deck, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudySetEntity studySet = studySets.get(position);
        holder.tvTitle.setText(studySet.getTitle());
        holder.tvCount.setText(studySet.getTotalCards() + " thẻ");
        holder.pbProgress.setProgress((int) studySet.getMasteryPercentage());
        holder.tvProgressText.setText((int) studySet.getMasteryPercentage() + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(studySet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studySets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount, tvProgressText;
        ProgressBar pbProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_deck_title);
            tvCount = itemView.findViewById(R.id.tv_deck_count);
            tvProgressText = itemView.findViewById(R.id.tv_deck_progress_text);
            pbProgress = itemView.findViewById(R.id.pb_deck_progress);
        }
    }
}
