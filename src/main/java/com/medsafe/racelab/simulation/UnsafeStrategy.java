package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class UnsafeStrategy implements SimulationStrategy {
    @Override
    public SimulationResult run() {
        TreatmentState state = new TreatmentState();
        EventLog log = new EventLog();
        ScenarioCoordinator coordinator = new ScenarioCoordinator();
        AtomicReference<TreatmentSnapshot> observed = new AtomicReference<>();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean violation = new AtomicBoolean();

        Runnable operator = () -> {
            SimulationSupport.beginXRayUpdate(state, log);
            coordinator.modeChanged.countDown();
            coordinator.await(coordinator.unsafeObservationComplete,
                    "treatment observing the unprotected intermediate state");
            SimulationSupport.finishXRayUpdate(state, log);
        };

        Runnable treatment = () -> {
            coordinator.await(coordinator.modeChanged, "operator changing the mode");
            coordinator.treatmentAttempted.countDown();
            try {
                SimulationSupport.attemptTreatment(state, log, observed, started, violation);
            } finally {
                coordinator.unsafeObservationComplete.countDown();
            }
        };

        long duration = SimulationSupport.runThreads(operator, treatment, log);
        return SimulationSupport.result(
                SafetyMode.UNSAFE, state, observed, started, violation, duration, log);
    }
}
