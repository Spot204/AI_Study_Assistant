package com.example.aistudyassistant.features.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleTask> tasks;

    public ScheduleAdapter(List<ScheduleTask> tasks) {
        this.tasks = tasks;
    }

    public void updateTasks(List<ScheduleTask> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleTask task = tasks.get(position);
        holder.tvTaskTitle.setText(task.getTitle());
        String timeString = String.format("%s - %s", task.getStartTime(), task.getEndTime());
        holder.tvTaskTime.setText(timeString);
        
        if (task.isCompleted()) {
            holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            holder.ivStatus.setImageResource(android.R.drawable.checkbox_off_background);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskTime;
        ImageView ivStatus, ivTaskIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ivTaskIcon = itemView.findViewById(R.id.ivTaskIcon);
        }
    }
}
