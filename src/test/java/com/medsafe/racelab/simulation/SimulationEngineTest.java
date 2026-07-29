package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.BatchResult;
import com.medsafe.racelab.model.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineTest {
    private final SimulationEngine engine = new SimulationEngine();

    @Test
    void unsafeModeDeterministicallyExposesTheIntermediateState() {
        SimulationResult result = engine.run(SafetyMode.UNSAFE);

        assertTrue(result.treatmentStarted());
        assertTrue(result.safetyViolation());
        assertNotNull(result.observedAtStart());
        assertFalse(result.observedAtStart().isSafeConfiguration());
        assertTrue(result.finalState().isSafeConfiguration());
        assertTrue(result.events().stream()
                .anyMatch(event -> event.message().contains("SAFETY VIOLATION")));
    }

    @Test
    void synchronizedModeKeepsTheConfigurationAtomic() {
        assertSafe(engine.run(SafetyMode.SYNCHRONIZED));
    }

    @Test
    void reentrantLockModeKeepsTheConfigurationAtomic() {
        assertSafe(engine.run(SafetyMode.REENTRANT_LOCK));
    }

    @Test
    void singlePermitSemaphoreKeepsTheConfigurationAtomic() {
        assertSafe(engine.run(SafetyMode.SEMAPHORE));
    }

    @Test
    void comparisonProducesFailuresOnlyForUnsafeMode() {
        List<BatchResult> results = engine.compareAll(20);

        assertEquals(4, results.size());
        BatchResult unsafe = results.stream()
                .filter(result -> result.safetyMode() == SafetyMode.UNSAFE)
                .findFirst()
                .orElseThrow();
        assertEquals(20, unsafe.safetyViolations());

        results.stream()
                .filter(result -> result.safetyMode() != SafetyMode.UNSAFE)
                .forEach(result -> assertEquals(0, result.safetyViolations()));
    }

    private void assertSafe(SimulationResult result) {
        assertTrue(result.treatmentStarted());
        assertFalse(result.safetyViolation());
        assertNotNull(result.observedAtStart());
        assertTrue(result.observedAtStart().isSafeConfiguration());
        assertTrue(result.finalState().isSafeConfiguration());
    }
}
