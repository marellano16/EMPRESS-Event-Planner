package com.empress.planner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Turns the model's JSON text into a validated PlanDraft. Model output is
 * never trusted: anything malformed or implausible throws GenerationException.
 */
public final class PlanParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> CATEGORIES = Set.of("food", "supplies", "venue", "prizes");

    private static final double MAX_UNIT_PRICE = 10_000;
    private static final double MAX_PER_PERSON = 50;
    private static final int MAX_QUANTITY = 1_000;
    private static final int MAX_PACK_SIZE = 10_000;
    private static final int MAX_ITEMS = 60;

    private PlanParser() { }

    public static PlanDraft parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new GenerationException("Model returned an empty response");
        }

        JsonNode root;
        try {
            root = MAPPER.readTree(stripFences(raw));
        } catch (JsonProcessingException e) {
            throw new GenerationException("Model response was not valid JSON", e);
        }
        if (root == null || !root.isObject()) {
            throw new GenerationException("Model response was not a JSON object");
        }

        List<String> assumptions = new ArrayList<>();
        JsonNode assumptionsNode = root.get("assumptions");
        if (assumptionsNode != null && assumptionsNode.isArray()) {
            for (JsonNode a : assumptionsNode) {
                assumptions.add(a.asText());
            }
        }

        JsonNode itemsNode = root.get("items");
        if (itemsNode == null || !itemsNode.isArray() || itemsNode.isEmpty()) {
            throw new GenerationException("Model response had no items");
        }
        if (itemsNode.size() > MAX_ITEMS) {
            throw new GenerationException("Model response had too many items");
        }

        List<LineItem> items = new ArrayList<>();
        for (JsonNode node : itemsNode) {
            items.add(parseItem(node));
        }
        return new PlanDraft(assumptions, items);
    }

    private static LineItem parseItem(JsonNode n) {
        String name = n.path("name").asText("").trim();
        if (name.isEmpty()) {
            throw new GenerationException("An item had no name");
        }

        String category = n.path("category").asText("").trim().toLowerCase();
        if (!CATEGORIES.contains(category)) {
            throw new GenerationException("Item '" + name + "' has an unknown category: " + category);
        }

        LineItem.Kind kind;
        try {
            kind = LineItem.Kind.valueOf(n.path("kind").asText("").trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GenerationException("Item '" + name + "' has an unknown kind");
        }

        double unitPrice = n.path("unitPrice").asDouble(0);
        double perPerson = n.path("perPerson").asDouble(0);
        int quantity = n.path("quantity").asInt(0);
        int packSize = n.path("packSize").asInt(0);

        if (unitPrice < 0 || unitPrice > MAX_UNIT_PRICE) {
            throw new GenerationException("Item '" + name + "' has an implausible price");
        }
        if (perPerson < 0 || perPerson > MAX_PER_PERSON) {
            throw new GenerationException("Item '" + name + "' has an implausible per-person amount");
        }
        if (quantity < 0 || quantity > MAX_QUANTITY) {
            throw new GenerationException("Item '" + name + "' has an implausible quantity");
        }
        if (packSize < 0 || packSize > MAX_PACK_SIZE) {
            throw new GenerationException("Item '" + name + "' has an implausible pack size");
        }

        switch (kind) {
            case PER_PERSON -> {
                if (perPerson <= 0) {
                    throw new GenerationException("Item '" + name + "' needs perPerson > 0");
                }
            }
            case FIXED -> {
                if (quantity <= 0) {
                    throw new GenerationException("Item '" + name + "' needs quantity > 0");
                }
            }
            case PACK -> {
                if (perPerson <= 0 || packSize <= 0) {
                    throw new GenerationException("Item '" + name + "' needs perPerson > 0 and packSize > 0");
                }
            }
        }

        return new LineItem(name, category, kind, unitPrice, perPerson, quantity, packSize);
    }

    /** Models sometimes wrap JSON in ```json fences even when told not to. */
    static String stripFences(String raw) {
        String s = raw.trim();
        if (s.startsWith("```")) {
            int newline = s.indexOf('\n');
            s = newline >= 0 ? s.substring(newline + 1) : s.substring(3);
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3);
            }
        }
        return s.trim();
    }
}
