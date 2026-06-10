package com.example.aistudyassistant.services.core;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiService {
    private final GenerativeModel gm;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GeminiService(String apiKey) {
        // Sử dụng model Gemini 1.5 Flash (Nhanh, thông minh, phù hợp mobile)
        this.gm = new GenerativeModel("gemini-2.5-flash", apiKey);
    }

    public void generateResponseAsync(String prompt, Callback callback) {
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        com.google.ai.client.generativeai.type.Content content =
                new com.google.ai.client.generativeai.type.Content.Builder()
                        .addText(prompt)
                        .build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onSuccess(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t.getMessage());
            }
        }, executor);
    }

    public interface Callback {
        void onSuccess(String response);
        void onFailure(String error);
    }
}