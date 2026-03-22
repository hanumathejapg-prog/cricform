package com.cricform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatterFormScoreServiceTest {

    private BatterFormScoreService service;

    @BeforeEach
    void setUp() {
        service = new BatterFormScoreService(null, null);
    }

    @Test
    void shouldCalculateSequenceWeight() {
        assertEquals(1.0, service.calculateSequenceWeight(1), 0.0001);
        assertEquals(0.1, service.calculateSequenceWeight(20), 0.0001);
    }

    @Test
    void shouldReturnFormatMultiplier() {
        assertEquals(1.3, service.getFormatMultiplier("T20I"), 0.0001);
        assertEquals(1.1, service.getFormatMultiplier("ODI"), 0.0001);
        assertEquals(1.0, service.getFormatMultiplier("TEST"), 0.0001);
    }

    @Test
    void shouldApplyMatchContextMultiplier() {
        assertEquals(1.3, service.getMatchContextMultiplier("FINAL"), 0.0001);
        assertEquals(1.2, service.getMatchContextMultiplier("KNOCKOUT"), 0.0001);
        assertEquals(1.0, service.getMatchContextMultiplier("GROUP"), 0.0001);
        assertEquals(0.8, service.getMatchContextMultiplier("DEAD_RUBBER"), 0.0001);
    }

    @Test
    void shouldApplyMultiplierCap() {
        assertEquals(2.0, service.applyMultiplierCap(2.7), 0.0001);
        assertEquals(1.7, service.applyMultiplierCap(1.7), 0.0001);
    }

    @Test
    void shouldNormalizeTo100() {
        assertEquals(50.0, service.normalizeTo100(50, 0, 100), 0.0001);
        assertEquals(0.0, service.normalizeTo100(-10, 0, 100), 0.0001);
        assertEquals(100.0, service.normalizeTo100(120, 0, 100), 0.0001);
    }
}
