package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.EventLevel;
import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

final class ReentrantLockStrategy implements SimulationStrategy {
    @Override
    public SimulationResult run() {
        TreatmentState state = new TreatmentState();
        ReentrantLock lock = new ReentrantLock(true);
        EventLog log = new EventLog();
        ScenarioCoordinator coordinator = new ScenarioCoordinator();
        AtomicReference<TreatmentSnapshot> observed = new AtomicReference<>();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean violation = new AtomicBoolean();

        Runnable operator = () -> {
            lock.lock();
            try {
                log.add(EventLevel.INFO, "Operator acquired ReentrantLock.");
                SimulationSupport.beginXRayUpdate(state, log);
                coordinator.modeChanged.countDown();
                coordinator.await(coordinator.treatmentAttempted,
                        "treatment attempting to acquire ReentrantLock");
                SimulationSupport.finishXRayUpdate(state, log);
            } finally {
                lock.unlock();
                log.add(EventLevel.INFO, "Operator released ReentrantLock in finally.");
            }
        };

        Runnable treatment = () -> {
            coordinator.await(coordinator.modeChanged, "operator changing the mode");
            coordinator.treatmentAttempted.countDown();
            lock.lock();
            try {
                log.add(EventLevel.INFO, "Treatment acquired ReentrantLock after the update.");
                SimulationSupport.attemptTreatment(state, log, observed, started, violation);
            } finally {
                lock.unlock();
                log.add(EventLevel.INFO, "Treatment released ReentrantLock in finally.");
            }
        };

        long duration = SimulationSupport.runThreads(operator, treatment, log);
        return SimulationSupport.result(
                SafetyMode.REENTRANT_LOCK, state, observed, started, violation, duration, log);
    }
}
