package com.example.aistudyassistant.fragments;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.example.aistudyassistant.features.quiz.QuizPlayActivity;
import java.util.ArrayList;
import java.util.List;

public class RecentStudyAdapter extends RecyclerView.Adapter<RecentStudyAdapter.ViewHolder> {

    public static class RecentActivity {
        public StudySessionEntity session;
        public String title;

        public RecentActivity(StudySessionEntity session, String title) {
            this.session = session;
            this.title = title;
        }
    }

    private List<RecentActivity> activities = new ArrayList<>();

    public void setActivities(List<RecentActivity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_study, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);
        StudySessionEntity session = activity.session;
        
        holder.tvTitle.setText(activity.title != null ? activity.title : session.getType().toUpperCase());
        
        String typeDisplay = "quiz".equalsIgnoreCase(session.getType()) ? "Quiz" : "Flashcard";
        holder.tvInfo.setText(typeDisplay + " • " + session.getDurationMinutes() + " phút • Điểm: " + session.getScore());
        
        int indicatorColor = 0xFF6366F1; // Default Indigo
        if ("quiz".equalsIgnoreCase(session.getType())) {
            indicatorColor = 0xFF10B981; // Green
        } else if ("flashcard".equalsIgnoreCase(session.getType())) {
            indicatorColor = 0xFFF59E0B; // Amber
        }
        holder.viewIndicator.setBackgroundColor(indicatorColor);

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if ("quiz".equalsIgnoreCase(session.getType())) {
                intent = new Intent(v.getContext(), QuizPlayActivity.class);
                intent.putExtra("QUIZ_ID", session.getReferenceId());
                intent.putExtra("QUIZ_TITLE", activity.title);
            } else {
                intent = new Intent(v.getContext(), FlashcardStudyActivity.class);
                intent.putExtra("SET_ID", session.getReferenceId());
                intent.putExtra("SET_TITLE", activity.title);
            }
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIndicator;
        TextView tvTitle, tvInfo;

        ViewHolder(View itemView) {
            super(itemView);
            viewIndicator = itemView.findViewById(R.id.viewTypeIndicator);
            tvTitle = itemView.findViewById(R.id.tvStudyTitle);
            tvInfo = itemView.findViewById(R.id.tvStudyInfo);
        }
    }
}
