package com.example.calendarapp.ai;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiAIManager {
    private static final String TAG = "GeminiAIManager";
    private static final String API_KEY = "AIzaSyDPpKlw3MRWuZUyj2tSemn4nXxRRLxQT2I"; // Thay bằng API key thực tế
    private static final String MODEL_NAME = "gemini-1.5-flash"; // Cập nhật model name mới

    private GenerativeModelFutures model;
    private Executor executor;
    private Context context;

    public interface AIResponseCallback {
        void onSuccess(String response);
        void onError(String error);
        void onTypingStart();
    }

    public GeminiAIManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        initializeModel();
    }

    private void initializeModel() {
        try {
            GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY);
            model = GenerativeModelFutures.from(gm);
            Log.d(TAG, "Gemini AI model initialized successfully with " + MODEL_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Gemini AI model", e);
        }
    }

    public void sendMessage(String userMessage, AIResponseCallback callback) {
        if (model == null) {
            callback.onError("AI model chưa được khởi tạo");
            return;
        }

        if (API_KEY.equals("YOUR_GEMINI_API_KEY")) {
            callback.onError("Vui lòng cấu hình API key Gemini trong GeminiAIManager.java");
            return;
        }

        // Tạo system prompt cho AI về giáo dục
        String systemPrompt = createEducationalSystemPrompt();
        String fullPrompt = systemPrompt + "\n\nCâu hỏi của học sinh: " + userMessage;

        callback.onTypingStart();

        Content content = new Content.Builder()
                .addText(fullPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String aiResponse = result.getText();
                    if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                        callback.onSuccess(aiResponse.trim());
                    } else {
                        callback.onError("AI không thể tạo phản hồi");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing AI response", e);
                    callback.onError("Lỗi xử lý phản hồi từ AI");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error getting AI response", t);
                String errorMessage = "Lỗi kết nối với AI";

                if (t.getMessage() != null) {
                    if (t.getMessage().contains("API key")) {
                        errorMessage = "API key không hợp lệ. Vui lòng kiểm tra lại.";
                    } else if (t.getMessage().contains("quota")) {
                        errorMessage = "Đã vượt quá giới hạn sử dụng API. Vui lòng thử lại sau.";
                    } else if (t.getMessage().contains("not found")) {
                        errorMessage = "Model AI không tồn tại. Vui lòng cập nhật ứng dụng.";
                    }
                }

                callback.onError(errorMessage);
            }
        }, executor);
    }

    private String createEducationalSystemPrompt() {
        return "Bạn là một trợ lý AI thông minh chuyên về giáo dục và học tập. " +
                "Nhiệm vụ của bạn là hỗ trợ học sinh và sinh viên trong việc học tập. " +
                "Hãy trả lời các câu hỏi một cách:\n" +
                "- Chính xác và có căn cứ khoa học\n" +
                "- Dễ hiểu, phù hợp với trình độ học sinh\n" +
                "- Khuyến khích tư duy phản biện\n" +
                "- Đưa ra ví dụ cụ thể khi cần thiết\n" +
                "- Sử dụng tiếng Việt tự nhiên và thân thiện\n" +
                "- Tập trung vào các chủ đề giáo dục như: toán học, khoa học, lịch sử, văn học, ngoại ngữ, tin học, v.v.\n" +
                "- Nếu câu hỏi không liên quan đến học tập, hãy lịch sự chuyển hướng về chủ đề giáo dục\n" +
                "- Trả lời ngắn gọn, súc tích nhưng đầy đủ thông tin\n\n";
    }

    public void cleanup() {
        if (executor != null) {
            executor = null;
        }
    }
}
