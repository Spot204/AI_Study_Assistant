package com.example.aistudyassistant.fragments.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.AchievementEntity;
import com.example.aistudyassistant.database.entities.UserAchievementEntity;
import com.example.aistudyassistant.database.AppDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private List<UserAchievementEntity> userAchievements = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void setAchievements(List<UserAchievementEntity> achievements) {
        this.userAchievements = achievements;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserAchievementEntity userAch = userAchievements.get(position);
        
        executorService.execute(() -> {
            AchievementEntity ach = AppDatabase.getDatabase(holder.itemView.getContext())
                    .achievementDao().getAchievementById(userAch.getAchievementId());
            
            if (ach != null) {
                holder.itemView.post(() -> {
                    holder.tvTitle.setText(ach.getTitle());
                    holder.tvDescription.setText(ach.getDescription());
                    
                    // Simple logic to map icon name to resource
                    int resId = holder.itemView.getContext().getResources().getIdentifier(
                            ach.getIconUrl(), "drawable", holder.itemView.getContext().getPackageName());
                    if (resId != 0) {
                        holder.ivIcon.setImageResource(resId);
                    } else {
                        holder.ivIcon.setImageResource(R.drawable.ic_trophy);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return userAchievements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDescription;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.iv_achievement_icon);
            tvTitle = view.findViewById(R.id.tv_achievement_title);
            tvDescription = view.findViewById(R.id.tv_achievement_description);
        }
    }
}
