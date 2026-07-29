package com.medsafe.racelab.model;

import com.medsafe.racelab.simulation.SafetyMode;

import java.util.List;

public record SimulationResult(
        SafetyMode safetyMode,
        TreatmentSnapshot observedAtStart,
        TreatmentSnapshot finalState,
        boolean treatmentStarted,
        boolean safetyViolation,
        long durationNanos,
        String summary,
        List<SimulationEvent> events) {
}
