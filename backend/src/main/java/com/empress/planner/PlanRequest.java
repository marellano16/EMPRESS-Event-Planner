package com.empress.planner;

/** What the frontend sends to POST /plan. Only counts and event type, never member names. */
public record PlanRequest(String eventType, int memberCount, double budgetCap) {
}
