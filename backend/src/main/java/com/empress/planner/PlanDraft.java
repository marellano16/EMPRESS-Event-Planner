package com.empress.planner;

import java.util.List;

/** What a generator produces: assumptions plus line items. No totals; Calculator adds those. */
public record PlanDraft(List<String> assumptions, List<LineItem> items) {
}
