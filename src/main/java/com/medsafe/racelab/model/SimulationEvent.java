package com.medsafe.racelab.model;

public record SimulationEvent(
        int sequence,
        long elapsedMicros,
        String threadName,
        EventLevel level,
        String message) {
}
