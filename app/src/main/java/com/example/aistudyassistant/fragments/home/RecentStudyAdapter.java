package com.example.aistudyassistant.fragments.home;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentStudyAdapter extends RecyclerView.Adapter<RecentStudyAdapter.ViewHolder> {

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
        StudySetEntity item = studySets.get(position);
        
        holder.tvSubject.setText(item.getTitle());
        holder.tvDetails.setText(String.format(Locale.getDefault(), "%d thẻ", item.getTotalCards()));
        
        int progress = (int) item.getMasteryPercentage();
        progress = Math.max(0, Math.min(100, progress));
        
        holder.pbProgress.setProgress(progress);
        holder.tvPercent.setText(String.format(Locale.getDefault(), "%d%%", progress));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studySets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTypeIcon;
        TextView tvSubject, tvDetails, tvPercent;
        ProgressBar pbProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTypeIcon = itemView.findViewById(R.id.iv_deck_icon);
            tvSubject = itemView.findViewById(R.id.tv_deck_title);
            tvDetails = itemView.findViewById(R.id.tv_deck_count);
            tvPercent = itemView.findViewById(R.id.tv_deck_progress_text);
            pbProgress = itemView.findViewById(R.id.pb_deck_progress);
        }
    }
}
