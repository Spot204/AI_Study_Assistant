package com.example.aistudyassistant.fragments.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.entities.QuizEntity;
import java.util.ArrayList;
import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.ViewHolder> {

    private List<QuizEntity> quizList = new ArrayList<>();

    public void setData(List<QuizEntity> newList) {
        this.quizList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizEntity quiz = quizList.get(position);
        holder.tvTitle.setText(quiz.getTitle());
        // Bạn có thể bổ sung hiển thị số câu hỏi hoặc điểm cao nhất ở đây
        holder.tvStats.setText("Thời gian: " + quiz.getTimeLimitMinutes() + " phút | Điểm cao nhất: " + quiz.getBestScore());
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvStats = itemView.findViewById(R.id.tvQuizStats);
        }
    }
}
