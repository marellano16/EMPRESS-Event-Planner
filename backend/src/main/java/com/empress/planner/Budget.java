package com.empress.planner;

import java.util.Map;

public record Budget(
        double subtotal,
        double contingency,
        double total,
        double costPerMember,
        boolean overBudget,
        Map<String, Double> byCategory) {
}
