package com.empress.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/**
 * Drafts a plan by calling Google's Gemini API over plain HTTPS (no SDK needed).
 * The API key stays in this class and is never logged or sent to the frontend.
 * Only the event type, member count and budget cap are sent, never member names.
 */
public class GeminiGenerator implements PlanGenerator {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static final String SYSTEM_PROMPT = """
            You are an event-planning assistant for a college student club.
            Given an event type, a member count and a budget cap, draft a materials list.

            Respond with JSON only, no other text, in exactly this shape:
            {"assumptions": ["..."],
             "items": [{"name": "...", "category": "food|supplies|venue|prizes",
                        "kind": "PER_PERSON|FIXED|PACK", "unitPrice": 0.00,
                        "perPerson": 0, "quantity": 0, "packSize": 0}]}

            How to fill in "kind":
            - PER_PERSON: cost = unitPrice * perPerson * members. Set perPerson. quantity and packSize are 0.
            - FIXED: cost = unitPrice * quantity. Set quantity. perPerson and packSize are 0.
            - PACK: sold in packs. unitPrice is the price of ONE WHOLE PACK, perPerson is units needed
              per person, packSize is units per pack. quantity is 0.

            Other rules:
            - Use realistic US retail prices in USD.
            - Skip items a student club would already own.
            - List your assumptions, for example "2 slices of pizza per person".
            - Do not add up totals or add a contingency. The app does the math.

            Example for "pizza night", 40 members:
            {"assumptions": ["2 slices per person", "about $2.25 per slice"],
             "items": [
               {"name": "Pizza slice", "category": "food", "kind": "PER_PERSON",
                "unitPrice": 2.25, "perPerson": 2, "quantity": 0, "packSize": 0},
               {"name": "Paper plates (50 pack)", "category": "supplies", "kind": "PACK",
                "unitPrice": 8.00, "perPerson": 1, "quantity": 0, "packSize": 50},
               {"name": "Speaker rental", "category": "venue", "kind": "FIXED",
                "unitPrice": 40.00, "perPerson": 0, "quantity": 1, "packSize": 0}]}
            """;

    private final String apiKey;
    private final String model;
    private final HttpClient http;

    public GeminiGenerator(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** Retries once if the model's answer is not usable JSON. HTTP errors fail right away. */
    @Override
    public PlanDraft generate(PlanRequest request) {
        GenerationException lastParseError = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            String text = callGemini(request);
            try {
                return PlanParser.parse(text);
            } catch (GenerationException e) {
                lastParseError = e;
            }
        }
        throw new GenerationException(
                "Could not get a valid plan after 2 attempts: " + lastParseError.getMessage(), lastParseError);
    }

    /** A short, safe hint for the most common failures. Never includes the key. */
    static String httpHint(int status) {
        return switch (status) {
            case 400, 403 -> " (check GEMINI_API_KEY, and that GEMINI_MODEL is a current model name)";
            case 404 -> " (model not found: set GEMINI_MODEL to a current model name from Google AI Studio)";
            case 429 -> " (rate limit reached: wait a minute and try again)";
            default -> "";
        };
    }

    static String userPrompt(PlanRequest r) {
        return String.format(Locale.US,
                "Event type: %s%nMember count: %d%nBudget cap (USD): %.2f%nReturn the JSON plan.",
                r.eventType(), r.memberCount(), r.budgetCap());
    }

    private String callGemini(PlanRequest request) {
        ObjectNode body = MAPPER.createObjectNode();
        body.putObject("systemInstruction").putArray("parts").addObject().put("text", SYSTEM_PROMPT);
        ObjectNode content = body.putArray("contents").addObject();
        content.put("role", "user");
        content.putArray("parts").addObject().put("text", userPrompt(request));
        ObjectNode generationConfig = body.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.2);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(BASE_URL + model + ":generateContent"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GenerationException("Gemini returned HTTP " + response.statusCode()
                        + httpHint(response.statusCode()));
            }

            JsonNode text = MAPPER.readTree(response.body()).at("/candidates/0/content/parts/0/text");
            if (text.isMissingNode() || text.asText().isBlank()) {
                throw new GenerationException("Gemini returned no text (the response may have been blocked)");
            }
            return text.asText();
        } catch (IOException e) {
            throw new GenerationException("Could not reach Gemini: " + e.getClass().getSimpleName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenerationException("Request to Gemini was interrupted", e);
        }
    }
}
