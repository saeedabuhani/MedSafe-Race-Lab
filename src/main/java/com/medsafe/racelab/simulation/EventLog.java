package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.EventLevel;
import com.medsafe.racelab.model.SimulationEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

final class EventLog {
    private final long startedAt = System.nanoTime();
    private final AtomicInteger sequence = new AtomicInteger();
    private final CopyOnWriteArrayList<SimulationEvent> events = new CopyOnWriteArrayList<>();

    void add(EventLevel level, String message) {
        long elapsedMicros = (System.nanoTime() - startedAt) / 1_000;
        events.add(new SimulationEvent(
                sequence.incrementAndGet(),
                elapsedMicros,
                Thread.currentThread().getName(),
                level,
                message));
    }

    List<SimulationEvent> snapshot() {
        return events.stream()
                .sorted(java.util.Comparator.comparingInt(SimulationEvent::sequence))
                .toList();
    }
}
