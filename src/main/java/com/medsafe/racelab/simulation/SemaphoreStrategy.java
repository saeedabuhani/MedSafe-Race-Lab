package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.EventLevel;
import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentSnapshot;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class SemaphoreStrategy implements SimulationStrategy {
    @Override
    public SimulationResult run() {
        TreatmentState state = new TreatmentState();
        Semaphore mutex = new Semaphore(1, true);
        EventLog log = new EventLog();
        ScenarioCoordinator coordinator = new ScenarioCoordinator();
        AtomicReference<TreatmentSnapshot> observed = new AtomicReference<>();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean violation = new AtomicBoolean();

        Runnable operator = () -> {
            boolean acquired = false;
            try {
                mutex.acquire();
                acquired = true;
                log.add(EventLevel.INFO, "Operator acquired the single semaphore permit.");
                SimulationSupport.beginXRayUpdate(state, log);
                coordinator.modeChanged.countDown();
                coordinator.await(coordinator.treatmentAttempted,
                        "treatment attempting to acquire the semaphore permit");
                SimulationSupport.finishXRayUpdate(state, log);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Operator was interrupted", exception);
            } finally {
                if (acquired) {
                    mutex.release();
                    log.add(EventLevel.INFO, "Operator released the permit in finally.");
                }
            }
        };

        Runnable treatment = () -> {
            coordinator.await(coordinator.modeChanged, "operator changing the mode");
            coordinator.treatmentAttempted.countDown();
            boolean acquired = false;
            try {
                mutex.acquire();
                acquired = true;
                log.add(EventLevel.INFO, "Treatment acquired the permit after the update.");
                SimulationSupport.attemptTreatment(state, log, observed, started, violation);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Treatment was interrupted", exception);
            } finally {
                if (acquired) {
                    mutex.release();
                    log.add(EventLevel.INFO, "Treatment released the permit in finally.");
                }
            }
        };

        long duration = SimulationSupport.runThreads(operator, treatment, log);
        return SimulationSupport.result(
                SafetyMode.SEMAPHORE, state, observed, started, violation, duration, log);
    }
}
