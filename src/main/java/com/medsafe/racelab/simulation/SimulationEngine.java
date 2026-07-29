package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.BatchResult;
import com.medsafe.racelab.model.SimulationResult;

import java.util.ArrayList;
import java.util.List;

public final class SimulationEngine {
    public SimulationResult run(SafetyMode safetyMode) {
        return strategyFor(safetyMode).run();
    }

    public BatchResult repeat(SafetyMode safetyMode, int runs) {
        if (runs < 1 || runs > 1_000) {
            throw new IllegalArgumentException("Runs must be between 1 and 1000");
        }

        int started = 0;
        int violations = 0;
        long totalDuration = 0;
        for (int index = 0; index < runs; index++) {
            SimulationResult result = run(safetyMode);
            if (result.treatmentStarted()) {
                started++;
            }
            if (result.safetyViolation()) {
                violations++;
            }
            totalDuration += result.durationNanos();
        }

        double averageMillis = totalDuration / 1_000_000.0 / runs;
        return new BatchResult(safetyMode, runs, started, violations, averageMillis);
    }

    public List<BatchResult> compareAll(int runsPerMode) {
        List<BatchResult> results = new ArrayList<>();
        for (SafetyMode safetyMode : SafetyMode.values()) {
            results.add(repeat(safetyMode, runsPerMode));
        }
        return List.copyOf(results);
    }

    private SimulationStrategy strategyFor(SafetyMode safetyMode) {
        return switch (safetyMode) {
            case UNSAFE -> new UnsafeStrategy();
            case SYNCHRONIZED -> new SynchronizedStrategy();
            case REENTRANT_LOCK -> new ReentrantLockStrategy();
            case SEMAPHORE -> new SemaphoreStrategy();
        };
    }
}
