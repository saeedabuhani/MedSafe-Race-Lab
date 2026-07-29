package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.EventLevel;
import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class SynchronizedStrategy implements SimulationStrategy {
    @Override
    public SimulationResult run() {
        TreatmentState state = new TreatmentState();
        Object monitor = new Object();
        EventLog log = new EventLog();
        ScenarioCoordinator coordinator = new ScenarioCoordinator();
        AtomicReference<TreatmentSnapshot> observed = new AtomicReference<>();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean violation = new AtomicBoolean();

        Runnable operator = () -> {
            synchronized (monitor) {
                log.add(EventLevel.INFO, "Operator acquired the intrinsic monitor.");
                SimulationSupport.beginXRayUpdate(state, log);
                coordinator.modeChanged.countDown();
                coordinator.await(coordinator.treatmentAttempted,
                        "treatment attempting to enter the synchronized block");
                SimulationSupport.finishXRayUpdate(state, log);
            }
            log.add(EventLevel.INFO, "Operator released the intrinsic monitor automatically.");
        };

        Runnable treatment = () -> {
            coordinator.await(coordinator.modeChanged, "operator changing the mode");
            coordinator.treatmentAttempted.countDown();
            synchronized (monitor) {
                log.add(EventLevel.INFO, "Treatment acquired the monitor after the update completed.");
                SimulationSupport.attemptTreatment(state, log, observed, started, violation);
            }
        };

        long duration = SimulationSupport.runThreads(operator, treatment, log);
        return SimulationSupport.result(
                SafetyMode.SYNCHRONIZED, state, observed, started, violation, duration, log);
    }
}
