package com.cricform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BowlerFormScoreServiceTest {

    private BowlerFormScoreService service;

    @BeforeEach
    void setUp() {
        service = new BowlerFormScoreService(null, null);
    }

    @Test
    void shouldReturnWicketWeights() {
        assertEquals(15, service.getWicketWeight("bowled"), 0.0001);
        assertEquals(14, service.getWicketWeight("lbw"), 0.0001);
        assertEquals(12, service.getWicketWeight("caught"), 0.0001);
        assertEquals(10, service.getWicketWeight("stumped"), 0.0001);
        assertEquals(0, service.getWicketWeight("runout"), 0.0001);
    }

    @Test
    void shouldCalculateEconomyByFormatAndPhase() {
        assertEquals(30, service.getEconomyScore(4.5, "POWERPLAY", "ODI"), 0.0001);
        assertEquals(18, service.getEconomyScore(9.5, "DEATH", "ODI"), 0.0001);
        assertEquals(25, service.getEconomyScore(5.5, "MIDDLE", "ODI"), 0.0001);
        assertEquals(20, service.getEconomyScore(3.5, "TEST", "TEST"), 0.0001);
    }

    @Test
    void shouldCalculateOversComponent() {
        assertEquals(10, service.getOversComponent(4, 4), 0.0001);
        assertEquals(5, service.getOversComponent(3, 4), 0.0001);
        assertEquals(0, service.getOversComponent(1.5, 4), 0.0001);
    }

    @Test
    void shouldCalculateMaidenBonus() {
        assertEquals(15, service.calculateMaidenBonus(3), 0.0001);
    }
}
