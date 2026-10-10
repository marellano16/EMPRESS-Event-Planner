package com.empress.planner;

import java.util.List;

/** What POST /plan returns to the frontend. */
public record PlanResponse(
        String eventType,
        int memberCount,
        List<String> assumptions,
        List<LineItem> items,
        Budget budget) {
}
