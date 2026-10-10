package com.empress.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CalculatorTest {

    private static final double DELTA = 0.01;

    @Test
    void zeroMembers_onlyFixedCostsRemain_andCostPerMemberIsZero() {
        List<LineItem> items = List.of(
                new LineItem("Pizza", "food", LineItem.Kind.PER_PERSON, 1.50, 2, 0, 0),
                new LineItem("Plates", "supplies", LineItem.Kind.PACK, 8.00, 1, 0, 50),
                new LineItem("Speaker", "venue", LineItem.Kind.FIXED, 50.00, 0, 1, 0));

        Budget b = Calculator.compute(items, 0, 0);

        assertEquals(50.00, b.subtotal(), DELTA);
        assertEquals(55.00, b.total(), DELTA);
        assertEquals(0.0, b.costPerMember(), DELTA);
        assertFalse(b.overBudget());
    }

    @Test
    void packItems_roundUpToWholePacks() {
        LineItem plates = new LineItem("Plates", "supplies", LineItem.Kind.PACK, 8.00, 1, 0, 50);

        // 120 plates needed -> 3 packs of 50 -> $24.00
        assertEquals(24.00, Calculator.lineCost(plates, 120), DELTA);
        // exactly 50 needed -> 1 pack
        assertEquals(8.00, Calculator.lineCost(plates, 50), DELTA);
        // 51 needed -> 2 packs
        assertEquals(16.00, Calculator.lineCost(plates, 51), DELTA);
    }

    @Test
    void largeEvent_addsContingencyAndFlagsOverBudget() {
        List<LineItem> items = List.of(
                new LineItem("Pizza", "food", LineItem.Kind.PER_PERSON, 1.25, 2, 0, 0));

        Budget b = Calculator.compute(items, 500, 1000);

        assertEquals(1250.00, b.subtotal(), DELTA);
        assertEquals(125.00, b.contingency(), DELTA);
        assertEquals(1375.00, b.total(), DELTA);
        assertEquals(2.75, b.costPerMember(), DELTA);
        assertTrue(b.overBudget());
        assertEquals(1250.00, b.byCategory().get("food"), DELTA);
    }

    @Test
    void negativeMembers_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.compute(List.of(), -1, 0));
    }
}
