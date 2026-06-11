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
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentStudyAdapter extends RecyclerView.Adapter<RecentStudyAdapter.ViewHolder> {

    private List<StudySessionEntity> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());

    public void setSessions(List<StudySessionEntity> sessions) {
        this.sessions = sessions;
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
        StudySessionEntity session = sessions.get(position);
        
        String typeLabel = "Học tập";
        int color = 0xFF6366F1; // Default Indigo
        int iconRes = R.drawable.ic_book; // Default icon

        if ("quiz".equals(session.getType())) {
            typeLabel = "Trắc nghiệm";
            color = 0xFFF59E0B; // Orange
            iconRes = R.drawable.ic_quiz;
        } else if ("flashcard".equals(session.getType())) {
            typeLabel = "Thẻ ghi nhớ";
            color = 0xFF10B981; // Green
            iconRes = R.drawable.ic_flashcard;
        } else if ("chat".equals(session.getType())) {
            typeLabel = "Hỏi đáp AI";
            color = 0xFF8B5CF6; // Purple
            iconRes = R.drawable.ic_chat;
        }

        // Set Icon and its background color tint (light version)
        holder.ivTypeIcon.setImageResource(iconRes);
        holder.ivTypeIcon.setColorFilter(color);
        
        // Tạo màu nền nhạt (15% alpha) từ màu chính
        int alphaColor = ColorUtils.setAlphaComponent(color, 38); 
        holder.ivTypeIcon.setBackgroundTintList(ColorStateList.valueOf(alphaColor));

        holder.tvSubject.setText(session.getReferenceId() != null ? session.getReferenceId() : "Phiên học tự do");
        holder.tvDetails.setText(String.format(Locale.getDefault(), "%s • %d phút", typeLabel, session.getDurationMinutes()));
        
        // Cập nhật tiến trình: Đảm bảo tỉ lệ % chính xác (0-100)
        int progress = session.getScore();
        progress = Math.max(0, Math.min(100, progress));
        
        holder.pbProgress.setProgress(progress);
        holder.tvPercent.setText(String.format(Locale.getDefault(), "%d%%", progress));
        holder.tvPercent.setTextColor(color);
        
        // Tùy chỉnh màu Progress Bar theo loại - Chỉ tint phần PROGRESS
        Drawable progressDrawable = holder.pbProgress.getProgressDrawable();
        if (progressDrawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) progressDrawable;
            Drawable p = ld.findDrawableByLayerId(android.R.id.progress);
            if (p != null) {
                p.setTint(color);
            }
        } else if (progressDrawable != null) {
            progressDrawable.setTint(color);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTypeIcon;
        TextView tvSubject, tvDetails, tvPercent;
        ProgressBar pbProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
            tvSubject = itemView.findViewById(R.id.tvRecentSubject);
            tvDetails = itemView.findViewById(R.id.tvRecentDetails);
            tvPercent = itemView.findViewById(R.id.tvProgressPercent);
            pbProgress = itemView.findViewById(R.id.pbRecentProgress);
        }
    }
}
