package com.example.aistudyassistant.services.quiz;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.aistudyassistant.database.dao.QuizDao;
import com.example.aistudyassistant.database.entities.QuizEntity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizService {

    private final QuizDao quizDao;
    private final FirebaseFirestore firestore;
    // ExecutorService dùng để xử lý các tác vụ Database/Mạng ở luồng nền ngầm
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public QuizService(QuizDao quizDao) {
        this.quizDao = quizDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Giao diện Callback để thông báo kết quả lên tầng UI (Giao diện) nếu cần
     */
    public interface QuizServiceCallback {
        void onLocalSaved(QuizEntity quiz);
        void onCloudSynced(QuizEntity quiz);
        void onError(Exception e);
    }

    /**
     * HÀM CHÍNH: Tạo mới một bộ Quiz (Offline-First)
     * Lưu ngay vào Room DB local -> Cập nhật UI -> Đẩy ngầm lên Firebase
     */
    public void createNewQuiz(final QuizEntity newQuiz, final QuizServiceCallback callback) {
        if (newQuiz == null) return;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // BƯỚC 1: Đảm bảo các trường đồng bộ được gán giá trị mặc định chuẩn xác
                    newQuiz.setUpdatedAt(System.currentTimeMillis());
                    newQuiz.setSyncStatus("pending_insert");

                    // BƯỚC 2: Lưu trực tiếp xuống Room Database Local trước
                    quizDao.insertQuiz(newQuiz);
                    Log.d("QUIZ_SERVICE", "Đã lưu Quiz vào Room DB Local thành công.");

                    // Báo về luồng chính là dữ liệu Local đã sẵn sàng để hiển thị lên màn hình
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onLocalSaved(newQuiz);
                            }
                        });
                    }

                    // BƯỚC 3: Kích hoạt luồng đẩy ngầm lên Firebase Cloud
                    uploadQuizToFirebase(newQuiz, callback);

                } catch (final Exception e) {
                    Log.e("QUIZ_SERVICE", "Lỗi trong quá trình tạo Quiz local: ", e);
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Hàm phụ trợ xử lý đẩy dữ liệu Object lên Firestore và cập nhật lại trạng thái local
     */
    private void uploadQuizToFirebase(final QuizEntity quiz, final QuizServiceCallback callback) {
        // Ánh xạ dữ liệu từ QuizEntity sang Map (Bỏ trường syncStatus theo đúng phương án)
        final Map<String, Object> firestoreMap = new HashMap<>();
        firestoreMap.put("quizId", quiz.getQuizId());
        firestoreMap.put("userId", quiz.getUserId());
        firestoreMap.put("title", quiz.getTitle());
        firestoreMap.put("sourceSetId", quiz.getSourceSetId());
        firestoreMap.put("sourceDocumentId", quiz.getSourceDocumentId());
        firestoreMap.put("timeLimitMinutes", quiz.getTimeLimitMinutes());
        firestoreMap.put("bestScore", quiz.getBestScore());
        firestoreMap.put("questionsJson", quiz.getQuestionsJson());
        firestoreMap.put("updatedAt", quiz.getUpdatedAt());

        // Thực hiện ghi lên Firestore bộ sưu tập "quizzes"
        firestore.collection("quizzes")
                .document(quiz.getQuizId())
                .set(firestoreMap)
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Nếu đẩy lên mạng thành công, chạy ngầm cập nhật lại trạng thái Room thành 'synced'
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                quiz.setSyncStatus("synced");
                                quizDao.updateQuiz(quiz);
                                Log.d("QUIZ_SERVICE", "Đồng bộ Firebase thành công! Đã chuyển trạng thái Room sang 'synced'.");

                                if (callback != null) {
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onCloudSynced(quiz);
                                        }
                                    });
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(final Exception e) {
                        // Mất mạng hoặc lỗi: Giữ nguyên trạng thái 'pending_insert' dưới local để luồng sync quét lại sau
                        Log.w("QUIZ_SERVICE", "Không thể kết nối Firebase. Giữ trạng thái 'pending_insert' để đồng bộ sau.");
                        if (callback != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(e); // Bạn có thể thông báo lỗi mạng nhẹ lên giao diện nếu muốn
                                }
                            });
                        }
                    }
                });
    }

    /**
     * Giải phóng bộ nhớ của Executor khi Service hoặc ViewModel bị hủy
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}