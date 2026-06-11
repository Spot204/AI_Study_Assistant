package com.example.aistudyassistant.features.ocr_summary;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.R;
import java.util.ArrayList;
import java.util.List;

public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.QuizViewHolder> {

    // Lớp Model nội bộ để lưu trữ tạm thời một câu hỏi Quiz trước khi đóng gói vào DB
    public static class QuizItem {
        public String question;
        public List<String> options = new ArrayList<>();
        public int correctAnswerIndex; // 1, 2, 3 hoặc 4

        public QuizItem(String question, List<String> options, int correctAnswerIndex) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    private final List<QuizItem> quizList = new ArrayList<>();

    public void setData(List<QuizItem> list) {
        this.quizList.clear();
        this.quizList.addAll(list);
        notifyDataSetChanged();
    }

    public List<QuizItem> getList() {
        return quizList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_review, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizItem item = quizList.get(position);

        // 1. GỠ BỎ TẤT CẢ TEXTWATCHER CŨ (NẾU CÓ) ĐỂ TRÁNH TRÙNG LẶP KHI CUỘN
        if (holder.etQuestion.getTag() instanceof TextWatcher) holder.etQuestion.removeTextChangedListener((TextWatcher) holder.etQuestion.getTag());
        if (holder.etOpt1.getTag() instanceof TextWatcher) holder.etOpt1.removeTextChangedListener((TextWatcher) holder.etOpt1.getTag());
        if (holder.etOpt2.getTag() instanceof TextWatcher) holder.etOpt2.removeTextChangedListener((TextWatcher) holder.etOpt2.getTag());
        if (holder.etOpt3.getTag() instanceof TextWatcher) holder.etOpt3.removeTextChangedListener((TextWatcher) holder.etOpt3.getTag());
        if (holder.etOpt4.getTag() instanceof TextWatcher) holder.etOpt4.removeTextChangedListener((TextWatcher) holder.etOpt4.getTag());
        if (holder.etCorrect.getTag() instanceof TextWatcher) holder.etCorrect.removeTextChangedListener((TextWatcher) holder.etCorrect.getTag());

        // 2. NẠP DỮ LIỆU AN TOÀN VÀO CÁC Ô NHẬP LIỆU
        holder.etQuestion.setText(item.question);
        if (item.options.size() >= 4) {
            holder.etOpt1.setText(item.options.get(0));
            holder.etOpt2.setText(item.options.get(1));
            holder.etOpt3.setText(item.options.get(2));
            holder.etOpt4.setText(item.options.get(3));
        }
        holder.etCorrect.setText(String.valueOf(item.correctAnswerIndex));

        // 3. TẠO VÀ GẮN CÁC BỘ LẮNG NGHE MỚI (Dùng getBindingAdapterPosition() thay cho getAdapterPosition() cũ)

        TextWatcher questionWatcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) quizList.get(pos).question = s.toString();
            }
        };
        holder.etQuestion.addTextChangedListener(questionWatcher);
        holder.etQuestion.setTag(questionWatcher); // Lưu vết lại vào Tag

        TextWatcher opt1Watcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && quizList.get(pos).options.size() > 0) {
                    quizList.get(pos).options.set(0, s.toString());
                }
            }
        };
        holder.etOpt1.addTextChangedListener(opt1Watcher);
        holder.etOpt1.setTag(opt1Watcher);

        TextWatcher opt2Watcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && quizList.get(pos).options.size() > 1) {
                    quizList.get(pos).options.set(1, s.toString());
                }
            }
        };
        holder.etOpt2.addTextChangedListener(opt2Watcher);
        holder.etOpt2.setTag(opt2Watcher);

        TextWatcher opt3Watcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && quizList.get(pos).options.size() > 2) {
                    quizList.get(pos).options.set(2, s.toString());
                }
            }
        };
        holder.etOpt3.addTextChangedListener(opt3Watcher);
        holder.etOpt3.setTag(opt3Watcher);

        TextWatcher opt4Watcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && quizList.get(pos).options.size() > 3) {
                    quizList.get(pos).options.set(3, s.toString());
                }
            }
        };
        holder.etOpt4.addTextChangedListener(opt4Watcher);
        holder.etOpt4.setTag(opt4Watcher);

        TextWatcher correctWatcher = new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    try {
                        int index = Integer.parseInt(s.toString().trim());
                        if (index >= 1 && index <= 4) {
                            quizList.get(pos).correctAnswerIndex = index;
                        }
                    } catch (NumberFormatException e) {
                        // Tránh crash ứng dụng nếu user lỡ tay xóa trống ô số
                    }
                }
            }
        };
        holder.etCorrect.addTextChangedListener(correctWatcher);
        holder.etCorrect.setTag(correctWatcher);

        // 🗑️ XỬ LÝ NÚT XÓA CÂU HỎI QUIZ
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                quizList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, quizList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    // Helper class để code ngắn gọn hơn, không cần override đủ 3 hàm TextWatcher liên tục
    private abstract static class CustomTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    // Bộ khung quản lý các View thành phần trong 1 câu Quiz review
    static class QuizViewHolder extends RecyclerView.ViewHolder {
        EditText etQuestion, etOpt1, etOpt2, etOpt3, etOpt4, etCorrect;
        ImageButton btnDelete;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            etQuestion = itemView.findViewById(R.id.et_quiz_question);
            etOpt1 = itemView.findViewById(R.id.et_quiz_opt1);
            etOpt2 = itemView.findViewById(R.id.et_quiz_opt2);
            etOpt3 = itemView.findViewById(R.id.et_quiz_opt3);
            etOpt4 = itemView.findViewById(R.id.et_quiz_opt4);
            etCorrect = itemView.findViewById(R.id.et_quiz_correct);
            btnDelete = itemView.findViewById(R.id.btn_quiz_delete);
        }
    }
}