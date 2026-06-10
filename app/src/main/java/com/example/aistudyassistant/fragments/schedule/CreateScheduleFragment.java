package com.example.aistudyassistant.fragments.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.R;
import com.example.aistudyassistant.data.repository.ScheduleRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.Locale;

public class CreateScheduleFragment extends Fragment {

    // Khai báo các view
    private EditText edtTitle;
    private MaterialButton btnSave;

    // Ngày và Giờ
    private MaterialCardView cardDate, cardStartTime, cardEndTime;
    private TextView tvDate, tvStartTime, tvEndTime;

    // Thông báo và Lặp lại
    private MaterialCardView cardReminder, cardRepeat;
    private TextView tvReminder, tvRepeat;

    // Các Card chứa loại Task
    private MaterialCardView typeReview, typeQuiz, typeFlashcard, typeReading;
    private String selectedTaskType = "Review";

    private ScheduleRepository scheduleRepository;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveTask();
                } else {
                    Toast.makeText(requireContext(), "Cần quyền thông báo để nhắc nhở lịch học!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_create_schedule, container, false); // Nhớ đổi tên layout nếu cần
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Repository
        scheduleRepository = new ScheduleRepository(requireContext(), AppDatabase.getDatabase(requireContext()).scheduleDao());

        initViews(view);
        setupTaskTypeListeners();

        // 3 Hàm mới để xử lý logic hiển thị Bảng chọn
        setupDateAndTimePickers();
        setupDropdowns();

        setupSaveEvent();
    }

    private void initViews(View view) {
        edtTitle = view.findViewById(R.id.edtTitle);
        btnSave = view.findViewById(R.id.btnSave);

        // Ánh xạ Ngày Giờ
        cardDate = view.findViewById(R.id.cardDate);
        tvDate = view.findViewById(R.id.tvDate);
        cardStartTime = view.findViewById(R.id.cardStartTime);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        cardEndTime = view.findViewById(R.id.cardEndTime);
        tvEndTime = view.findViewById(R.id.tvEndTime);

        // Ánh xạ Nhắc nhở và Lặp lại (Nhớ thêm ID bên XML nhé)
        cardReminder = view.findViewById(R.id.cardReminder);
        tvReminder = view.findViewById(R.id.tvReminder);
        cardRepeat = view.findViewById(R.id.cardRepeat);
        tvRepeat = view.findViewById(R.id.tvRepeat);

        // Ánh xạ Loại Task
        typeReview = view.findViewById(R.id.typeReview);
        typeQuiz = view.findViewById(R.id.typeQuiz);
        typeFlashcard = view.findViewById(R.id.typeFlashcard);
        typeReading = view.findViewById(R.id.typeReading);

        // Nút Back
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });
    }

    // ================== LOGIC CHỌN NGÀY VÀ GIỜ ==================
    private void setupDateAndTimePickers() {
        // Mở lịch chọn Ngày
        cardDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        // Cập nhật lên màn hình. Lưu ý: month bắt đầu từ 0 nên phải +1
                        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        tvDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        // Mở đồng hồ chọn Giờ Bắt Đầu
        cardStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));

        // Mở đồng hồ chọn Giờ Kết Thúc
        cardEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));
    }

    // Hàm dùng chung để hiển thị TimePickerDialog
    private void showTimePicker(TextView targetTextView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    // Đổi định dạng 24h sang 12h (AM/PM) cho đẹp
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int hour = hourOfDay % 12;
                    if (hour == 0) hour = 12; // 0 giờ thì hiển thị là 12 AM

                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
                    targetTextView.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false); // false: dùng chuẩn 12h (AM/PM) thay vì 24h
        timePicker.show();
    }

    // ================== LOGIC CHỌN NHẮC NHỞ & LẶP LẠI ==================
    private void setupDropdowns() {
        // Mảng dữ liệu cho phần Nhắc nhở
        String[] reminderOptions = {"Không thông báo", "Trước 5 phút", "Trước 10 phút", "Trước 15 phút", "Trước 30 phút", "Trước 1 giờ"};

        cardReminder.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn thời gian nhắc nhở")
                    .setItems(reminderOptions, (dialog, which) -> {
                        // Khi người dùng bấm vào một dòng, cập nhật TextView
                        tvReminder.setText(reminderOptions[which]);
                    })
                    .show();
        });

        // Mảng dữ liệu cho phần Lặp lại
        String[] repeatOptions = {"Không lặp lại", "Hàng ngày", "Hàng tuần", "Hàng tháng"};

        cardRepeat.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Tần suất lặp lại")
                    .setItems(repeatOptions, (dialog, which) -> {
                        tvRepeat.setText(repeatOptions[which]);
                    })
                    .show();
        });
    }

    // ================== LOGIC CHỌN LOẠI TASK ==================
    private void setupTaskTypeListeners() {
        typeReview.setOnClickListener(v -> selectTaskType(typeReview, "Review"));
        typeQuiz.setOnClickListener(v -> selectTaskType(typeQuiz, "Quiz"));
        typeFlashcard.setOnClickListener(v -> selectTaskType(typeFlashcard, "Flashcard"));
        typeReading.setOnClickListener(v -> selectTaskType(typeReading, "Reading"));
    }

    private void selectTaskType(MaterialCardView selectedCard, String typeName) {
        typeReview.setStrokeColor(Color.parseColor("#EEEEEE"));
        typeQuiz.setStrokeColor(Color.parseColor("#EEEEEE"));
        typeFlashcard.setStrokeColor(Color.parseColor("#EEEEEE"));
        typeReading.setStrokeColor(Color.parseColor("#EEEEEE"));
        selectedCard.setStrokeColor(Color.parseColor("#7C4DFF"));
        selectedTaskType = typeName;
    }

    // ================== LOGIC NÚT LƯU ==================
    private void setupSaveEvent() {
        btnSave.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    saveTask();
                }
            } else {
                saveTask();
            }
        });
    }

    private void saveTask() {
        String title = edtTitle.getText().toString().trim();
        String date = tvDate.getText().toString();
        String startTime = tvStartTime.getText().toString();
        String endTime = tvEndTime.getText().toString();
        String reminder = tvReminder.getText().toString();
        String repeat = tvRepeat.getText().toString();

        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tiêu đề!");
            edtTitle.requestFocus();
            return;
        }

        // THỰC HIỆN LƯU VÀO DATABASE
        ScheduleTask newTask = new ScheduleTask();
        newTask.setTitle(title);
        newTask.setType(selectedTaskType);
        newTask.setDate(date);
        newTask.setStartTime(startTime);
        newTask.setEndTime(endTime);

        // Chuyển đổi Reminder sang phút
        int reminderMin = 0;
        if (reminder.contains("5 phút")) reminderMin = 5;
        else if (reminder.contains("10 phút")) reminderMin = 10;
        else if (reminder.contains("15 phút")) reminderMin = 15;
        else if (reminder.contains("30 phút")) reminderMin = 30;
        else if (reminder.contains("1 giờ")) reminderMin = 60;
        else if (reminder.equals("Không thông báo")) reminderMin = -1;

        newTask.setReminderMinutes(reminderMin);
        newTask.setRepeatType(repeat);

        scheduleRepository.insertTask(newTask);

        Toast.makeText(requireContext(), "Đã lưu lịch học thành công!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
    }
}