package com.empress.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PlanParserTest {

    private static final String VALID = """
            {"assumptions": ["2 slices per person"],
             "items": [
               {"name": "Pizza", "category": "food", "kind": "PER_PERSON",
                "unitPrice": 2.25, "perPerson": 2, "quantity": 0, "packSize": 0},
               {"name": "Plates", "category": "supplies", "kind": "PACK",
                "unitPrice": 8.0, "perPerson": 1, "quantity": 0, "packSize": 50},
               {"name": "Speaker", "category": "venue", "kind": "FIXED",
                "unitPrice": 40.0, "perPerson": 0, "quantity": 1, "packSize": 0}]}
            """;

    @Test
    void validJson_isParsed() {
        PlanDraft draft = PlanParser.parse(VALID);

        assertEquals(3, draft.items().size());
        assertEquals("2 slices per person", draft.assumptions().get(0));
        assertEquals(LineItem.Kind.PACK, draft.items().get(1).kind());
        assertEquals(50, draft.items().get(1).packSize());
    }

    @Test
    void jsonWrappedInCodeFences_isParsed() {
        PlanDraft draft = PlanParser.parse("```json\n" + VALID + "\n```");

        assertEquals(3, draft.items().size());
    }

    @Test
    void notJson_isRejected() {
        assertThrows(GenerationException.class, () -> PlanParser.parse("Sure! Here is your plan."));
        assertThrows(GenerationException.class, () -> PlanParser.parse(""));
        assertThrows(GenerationException.class, () -> PlanParser.parse(null));
    }

    @Test
    void negativePrice_isRejected() {
        String bad = """
                {"assumptions": [], "items": [
                  {"name": "Pizza", "category": "food", "kind": "PER_PERSON",
                   "unitPrice": -2.0, "perPerson": 2, "quantity": 0, "packSize": 0}]}
                """;

        assertThrows(GenerationException.class, () -> PlanParser.parse(bad));
    }

    @Test
    void packItemWithoutPackSize_isRejected() {
        String bad = """
                {"assumptions": [], "items": [
                  {"name": "Plates", "category": "supplies", "kind": "PACK",
                   "unitPrice": 8.0, "perPerson": 1, "quantity": 0, "packSize": 0}]}
                """;

        assertThrows(GenerationException.class, () -> PlanParser.parse(bad));
    }

    @Test
    void unknownCategoryOrKind_isRejected() {
        String badCategory = """
                {"assumptions": [], "items": [
                  {"name": "Pizza", "category": "snacks", "kind": "PER_PERSON",
                   "unitPrice": 2.0, "perPerson": 2, "quantity": 0, "packSize": 0}]}
                """;
        String badKind = """
                {"assumptions": [], "items": [
                  {"name": "Pizza", "category": "food", "kind": "PER_PIZZA",
                   "unitPrice": 2.0, "perPerson": 2, "quantity": 0, "packSize": 0}]}
                """;

        assertThrows(GenerationException.class, () -> PlanParser.parse(badCategory));
        assertThrows(GenerationException.class, () -> PlanParser.parse(badKind));
    }

    @Test
    void emptyItemsList_isRejected() {
        assertThrows(GenerationException.class,
                () -> PlanParser.parse("{\"assumptions\": [], \"items\": []}"));
    }
}
