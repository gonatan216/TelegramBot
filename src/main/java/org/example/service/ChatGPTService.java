package com.avishai.service;

import com.avishai.config.Config;
import com.avishai.domain.Question;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ChatGPTService {
    private static volatile ChatGPTService instance;
    private final HttpClient httpClient;
    private final Gson gson;

    private ChatGPTService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    }

    public static ChatGPTService getInstance() {
        if (instance == null) {
            synchronized (ChatGPTService.class) {
                if (instance == null) {
                    instance = new ChatGPTService();
                }
            }
        }
        return instance;
    }

    public CompletableFuture<List<Question>> generateSurvey(String topic) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String promptText = "You are a survey generator. " +
                        "Generate 1 to 3 questions about the topic: " + topic +
                        ". Each question must have 2 to 4 options." +
                        " Respond ONLY with a raw JSON array matching this exact format: " +
                        "[{\"text\": \"Question?\", \"options\": [\"O1\", \"O2\"]}]. " +
                        "Do not include markdown code blocks or any conversational text.";

                String encodedText = URLEncoder.encode(promptText, StandardCharsets.UTF_8);
                String url = "https://shaitest-production-3066.up.railway.app/api-request?token=" +
                        Config.CHAT_GPT_TOKEN + "&text=" + encodedText;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Proxy API Error: " + response.statusCode());
                }

                String contentString = response.body();
                contentString = contentString.replace("\\\"", "\"")
                        .replace("\\n", "")
                        .replace("```json", "")
                        .replace("```", "");

                int startIndex = contentString.indexOf('[');
                int endIndex = contentString.lastIndexOf(']');

                if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                    contentString = contentString.substring(startIndex, endIndex + 1);
                } else {
                    throw new RuntimeException("Could not locate a JSON array in the API response.");
                }

                log.info("Sanitized JSON passed to Gson: {}", contentString);
                return gson.fromJson(contentString, new TypeToken<List<Question>>(){}.getType());

            } catch (Exception e) {
                log.warn("OpenAI API failed, falling back to mock data. Reason: {}", e.getMessage());
                Question q1 = Question.builder()
                        .text("What is the most interesting aspect of " + topic + "?")
                        .options(java.util.List.of("Theory", "Practice", "History", "Other"))
                        .build();
                Question q2 = Question.builder()
                        .text("How well do you understand " + topic + "?")
                        .options(java.util.List.of("Very well", "Somewhat", "Beginner", "Not at all"))
                        .build();
                Question q3 = Question.builder()
                        .text("Would you recommend learning about " + topic + "?")
                        .options(java.util.List.of("Yes, definitely", "Maybe", "Probably not", "No"))
                        .build();
                return java.util.List.of(q1, q2, q3);
            }
        });
    }
}
