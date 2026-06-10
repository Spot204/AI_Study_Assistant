package com.example.aistudyassistant.fragments.schedule;

import android.util.Log;
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
        
        if (holder.ivTaskStatus != null) {
            Log.d("ScheduleAdapter", "ivTaskStatus is NOT null at position " + position);
            if (task.isCompleted()) {
                holder.ivTaskStatus.setImageResource(android.R.drawable.checkbox_on_background);
            } else {
                holder.ivTaskStatus.setImageResource(android.R.drawable.checkbox_off_background);
            }
        } else {
            Log.e("ScheduleAdapter", "ivTaskStatus IS NULL at position " + position);
        }

        if (holder.ivReminderIcon != null) {
            if (task.getReminderMinutes() > 0) {
                holder.ivReminderIcon.setVisibility(View.VISIBLE);
            } else {
                holder.ivReminderIcon.setVisibility(View.GONE);
            }
        } else {
            Log.e("ScheduleAdapter", "ivReminderIcon is null at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskTime;
        ImageView ivTaskStatus, ivTaskIcon, ivReminderIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            ivTaskStatus = itemView.findViewById(R.id.ivTaskStatus);
            ivTaskIcon = itemView.findViewById(R.id.ivTaskIcon);
            ivReminderIcon = itemView.findViewById(R.id.ivReminderIcon);
        }
    }
}
