package com.medsafe.racelab.model;

import com.medsafe.racelab.simulation.SafetyMode;

public record BatchResult(
        SafetyMode safetyMode,
        int runs,
        int treatmentsStarted,
        int safetyViolations,
        double averageDurationMillis) {

    public double failureRatePercent() {
        return runs == 0 ? 0.0 : (100.0 * safetyViolations) / runs;
    }
}
