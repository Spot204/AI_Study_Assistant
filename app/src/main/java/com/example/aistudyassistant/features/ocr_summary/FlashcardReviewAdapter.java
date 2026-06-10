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
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import java.util.ArrayList;
import java.util.List;

public class FlashcardReviewAdapter extends RecyclerView.Adapter<FlashcardReviewAdapter.ReviewViewHolder> {

    // Danh sách lưu trữ tạm thời các thẻ đang hiển thị trên màn hình
    private final List<FlashcardEntity> flashcardList = new ArrayList<>();

    // Hàm để nạp dữ liệu từ AI trả về vào Adapter
    public void setData(List<FlashcardEntity> list) {
        this.flashcardList.clear();
        this.flashcardList.addAll(list);
        notifyDataSetChanged();
    }

    // Hàm để lấy ra danh sách thẻ CUỐI CÙNG (sau khi user đã sửa/xóa) để lưu vào DB
    public List<FlashcardEntity> getList() {
        return flashcardList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        FlashcardEntity card = flashcardList.get(position);

        // Đổ chữ thô từ AI vào 2 ô nhập liệu Mặt trước / Mặt sau
        holder.etFront.setText(card.getFront());
        holder.etBack.setText(card.getBack());

        // 👁️ LẮNG NGHE USER SỬA CHỮ Ở MẶT TRƯỚC (CÂU HỎI)
        holder.etFront.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    // Cập nhật ngay thay đổi vào danh sách bộ nhớ tạm
                    flashcardList.get(adapterPos).setFront(s.toString());
                }
            }
        });

        // 👁️ LẮNG NGHE USER SỬA CHỮ Ở MẶT SAU (CÂU TRẢ LỜI)
        holder.etBack.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    // Cập nhật ngay thay đổi vào danh sách bộ nhớ tạm
                    flashcardList.get(adapterPos).setBack(s.toString());
                }
            }
        });

        // 🗑️ XỬ LÝ NÚT XÓA NHANH THẺ LỖI
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                flashcardList.remove(adapterPos); // Xóa khỏi danh sách tạm
                notifyItemRemoved(adapterPos);    // Tạo hiệu ứng biến mất trên UI
                notifyItemRangeChanged(adapterPos, flashcardList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcardList.size();
    }

    // Bộ khung quản lý các View thành phần trong 1 dòng thẻ review
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        EditText etFront, etBack;
        ImageButton btnDelete;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            etFront = itemView.findViewById(R.id.et_item_front);
            etBack = itemView.findViewById(R.id.et_item_back);
            btnDelete = itemView.findViewById(R.id.btn_item_delete);
        }
    }
}