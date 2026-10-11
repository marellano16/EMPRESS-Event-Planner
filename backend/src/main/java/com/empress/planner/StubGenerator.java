package com.empress.planner;

import java.util.List;

/** Hard-coded sample plan. Used when no API key is set, and handy for frontend work. */
public class StubGenerator implements PlanGenerator {

    @Override
    public PlanDraft generate(PlanRequest request) {
        List<LineItem> items = List.of(
                new LineItem("Pizza slices", "food", LineItem.Kind.PER_PERSON, 1.50, 2, 0, 0),
                new LineItem("Paper plates (pack of 50)", "supplies", LineItem.Kind.PACK, 8.00, 1, 0, 50),
                new LineItem("Speaker rental", "venue", LineItem.Kind.FIXED, 40.00, 0, 1, 0));
        List<String> assumptions = List.of(
                "STUB DATA: no GEMINI_API_KEY is set, so this is not from a model",
                "2 slices of pizza per person",
                "1 plate per person");
        return new PlanDraft(assumptions, items);
    }
}
