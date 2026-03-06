package com.app.CryptoAnalyser.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeminiService {
    private final String apiKey;
    private final List<String> conversationHistory;
    private boolean lastResponseTruncated = false;

    public GeminiService(String apiKey) {
        this.apiKey = apiKey;
        this.conversationHistory = new ArrayList<>();
    }

    public String askQuestion(String question) {
        try {
            QuestionAnalysis analysis = analyzeQuestion(question);
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

            String optimizedPrompt = buildOptimizedPrompt(question, analysis);
            String jsonRequest = buildJsonRequest(optimizedPrompt);

            System.out.println("=== DEBUG GEMINI API ===");
            System.out.println("API Key length: " + (apiKey != null ? apiKey.length() : "null"));
            System.out.println("API URL: " + apiUrl.replace(apiKey, "HIDDEN_KEY"));
            System.out.println("Question: " + question);

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Lire le message d'erreur si ce n'est pas HTTP_OK
            if (responseCode != HttpURLConnection.HTTP_OK) {
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("Error Response: " + errorResponse.toString());
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = parseResponse(connection);
                String finalResponse = manageResponse(response, question, analysis);
                updateConversationHistory(question, finalResponse);
                return finalResponse;
            } else {
                lastResponseTruncated = false;
                System.err.println("API Error - Response Code: " + responseCode);
                return "❌ Erreur API (Code: " + responseCode + "). Vérifiez votre clé API.";
            }

        } catch (Exception e) {
            lastResponseTruncated = false;
            System.err.println("Exception in askQuestion: " + e.getMessage());
            e.printStackTrace();
            return "❌ Erreur de connexion: " + e.getMessage();
        }
    }
    public boolean wasLastResponseTruncated() {
        return lastResponseTruncated;
    }

    private QuestionAnalysis analyzeQuestion(String question) {
        String lower = question.toLowerCase();
        if (lower.contains("plus détaillé") || lower.contains("continue") || lower.contains("suite")) {
            return new QuestionAnalysis("FOLLOW_UP", "Développer la réponse précédente");
        } else if (lower.contains("quoi") || lower.contains("qu'est") || lower.length() < 15) {
            return new QuestionAnalysis("SIMPLE", "Réponse concise et directe");
        } else if (lower.contains("explique") || lower.contains("comment") || lower.contains("pourquoi")) {
            return new QuestionAnalysis("EXPLANATION", "Explication structurée et pédagogique");
        } else if (lower.contains("détaillé") || lower.contains("complet") || lower.contains("exhaustif")) {
            return new QuestionAnalysis("DETAILED", "Réponse technique approfondie");
        } else {
            return new QuestionAnalysis("STANDARD", "Réponse équilibrée");
        }
    }

    private String buildOptimizedPrompt(String question, QuestionAnalysis analysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un expert en cryptographie. Réponds en français.\n\n");

        switch (analysis.getType()) {
            case "SIMPLE":
                prompt.append("🔹 **Réponse attendue: CONCISE**\n");
                prompt.append("Maximum 2-3 phrases.\n\n");
                break;
            case "EXPLANATION":
                prompt.append("🔹 **Réponse attendue: EXPLICATION PÉDAGOGIQUE**\n");
                prompt.append("2-3 paragraphes maximum.\n\n");
                break;
            case "DETAILED":
                prompt.append("🔹 **Réponse attendue: APPROFONDIE**\n");
                prompt.append("Analyse technique complète mais optimisée.\n\n");
                break;
            case "FOLLOW_UP":
                prompt.append("🔹 **Réponse attendue: DÉVELOPPEMENT**\n");
                prompt.append("Développe la réponse précédente.\n\n");
                if (!conversationHistory.isEmpty()) {
                    prompt.append("**Contexte:**\n");
                    int start = Math.max(0, conversationHistory.size() - 2);
                    for (int i = start; i < conversationHistory.size(); i++) {
                        prompt.append(conversationHistory.get(i)).append("\n");
                    }
                }
                break;
            default:
                prompt.append("🔹 **Réponse attendue: ÉQUILIBRÉE**\n");
                prompt.append("Sois complet mais concis.\n\n");
                break;
        }

        prompt.append("**Question:** ").append(question.replace("plus détaillé", "").replace("détaillé", "").trim());
        return prompt.toString();
    }

    private String manageResponse(String rawResponse, String question, QuestionAnalysis analysis) {
        String cleaned = cleanResponse(rawResponse);
        if (!isResponseComplete(cleaned, analysis)) {
            lastResponseTruncated = true;
            System.out.println("⚠️ La réponse a été probablement tronquée !");
            return cleaned + getContinuationSuggestion(analysis);
        } else {
            lastResponseTruncated = false;
            return cleaned;
        }
    }

    private boolean isResponseComplete(String response, QuestionAnalysis analysis) {
        int length = response.length();
        switch (analysis.getType()) {
            case "SIMPLE": return length >= 50 && length <= 300;
            case "EXPLANATION": return length >= 200 && length <= 600;
            case "DETAILED": return length >= 400 && length <= 1000;
            case "FOLLOW_UP": return length >= 300;
            default: return length >= 150 && length <= 500;
        }
    }

    private String getContinuationSuggestion(QuestionAnalysis analysis) {
        return "\n\n💡 **Pour aller plus loin:**\n• Posez une question plus spécifique\n• Demandez un aspect particulier\n• Requestez des exemples concrets";
    }

    private String cleanResponse(String response) {
        return response.replaceAll("\\.\\.\\.\\s*$", ".")
                .replaceAll("…\\s*$", ".")
                .replaceAll("\\s*\\.\\.\\.\\s*", ". ")
                .trim();
    }

    private String buildJsonRequest(String prompt) {
        return "{"
                + "\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}],"
                + "\"generationConfig\": {\"maxOutputTokens\": 2048,\"temperature\":0.4,\"topP\":0.8,\"topK\":40}"
                + "}";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String parseResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        try {
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return extractTextFromJson(jsonResponse);
        } catch (Exception e) {
            return "Erreur de format de réponse.";
        }
    }

    private void updateConversationHistory(String question, String response) {
        conversationHistory.add("User: " + question);
        conversationHistory.add("Assistant: " + response);
        if (conversationHistory.size() > 6) {
            conversationHistory.remove(0);
            conversationHistory.remove(0);
        }
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public String listAvailableModels() {
        return "Modèle actuel: Gemini 2.5 Flash (limité à ~2000 tokens)";
    }

    private String extractTextFromJson(JsonObject jsonResponse) {
        try {
            return jsonResponse.get("candidates").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("content").getAsJsonObject()
                    .get("parts").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "Format de réponse inattendu.";
        }
    }

    private static class QuestionAnalysis {
        private final String type;
        private final String description;

        public QuestionAnalysis(String type, String description) {
            this.type = type;
            this.description = description;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
    }
}
