package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.EventLevel;
import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentMode;
import com.medsafe.racelab.model.TreatmentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class SimulationSupport {
    private SimulationSupport() {
    }

    static void beginXRayUpdate(TreatmentState state, EventLog log) {
        log.add(EventLevel.ACTION, "Operator begins changing ELECTRON treatment to X_RAY.");
        state.setMode(TreatmentMode.X_RAY);
        log.add(EventLevel.WARNING,
                "Mode is now X_RAY, but the target is not yet in place and verification is still stale.");
    }

    static void finishXRayUpdate(TreatmentState state, EventLog log) {
        state.setVerified(false);
        log.add(EventLevel.INFO, "Previous verification is invalidated.");
        state.setEnergyMeV(25);
        log.add(EventLevel.INFO, "Energy configured to 25 MeV.");
        state.setTargetInPlace(true);
        log.add(EventLevel.INFO, "Beam-spreading target moved into place.");
        state.setVerified(true);
        log.add(EventLevel.SUCCESS, "The complete X-ray configuration is now verified.");
    }

    static void attemptTreatment(
            TreatmentState state,
            EventLog log,
            AtomicReference<TreatmentSnapshot> observedAtStart,
            AtomicBoolean treatmentStarted,
            AtomicBoolean safetyViolation) {
        TreatmentSnapshot observed = state.snapshot();
        observedAtStart.set(observed);
        log.add(EventLevel.ACTION,
                "Treatment controller reads mode=" + observed.mode()
                        + ", energy=" + observed.energyMeV()
                        + ", target=" + observed.targetInPlace()
                        + ", verified=" + observed.verified() + ".");

        if (!observed.verified()) {
            log.add(EventLevel.WARNING, "Treatment is blocked because configuration is not verified.");
            return;
        }

        treatmentStarted.set(true);
        if (observed.isSafeConfiguration()) {
            log.add(EventLevel.SUCCESS, "Simulated treatment starts with a consistent configuration.");
        } else {
            safetyViolation.set(true);
            log.add(EventLevel.DANGER,
                    "SAFETY VIOLATION: simulated treatment starts with an inconsistent configuration.");
        }
    }

    static long runThreads(Runnable operatorAction, Runnable treatmentAction, EventLog log) {
        long startedAt = System.nanoTime();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread treatmentThread = thread("TreatmentThread", treatmentAction, failure, log);
        Thread operatorThread = thread("OperatorThread", operatorAction, failure, log);

        log.add(EventLevel.INFO, "Starting two concurrent tasks.");
        treatmentThread.start();
        operatorThread.start();

        join(operatorThread, 3_000);
        join(treatmentThread, 3_000);

        if (operatorThread.isAlive() || treatmentThread.isAlive()) {
            operatorThread.interrupt();
            treatmentThread.interrupt();
            throw new IllegalStateException("Simulation threads did not finish in time");
        }
        if (failure.get() != null) {
            throw new IllegalStateException("Simulation task failed", failure.get());
        }

        return System.nanoTime() - startedAt;
    }

    private static Thread thread(
            String name,
            Runnable action,
            AtomicReference<Throwable> failure,
            EventLog log) {
        Thread thread = new Thread(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.compareAndSet(null, throwable);
                log.add(EventLevel.DANGER, "Task failed: " + throwable.getMessage());
            }
        }, name);
        thread.setDaemon(true);
        return thread;
    }

    private static void join(Thread thread, long timeoutMillis) {
        try {
            thread.join(timeoutMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for simulation", exception);
        }
    }

    static SimulationResult result(
            SafetyMode mode,
            TreatmentState state,
            AtomicReference<TreatmentSnapshot> observedAtStart,
            AtomicBoolean treatmentStarted,
            AtomicBoolean safetyViolation,
            long durationNanos,
            EventLog log) {
        boolean violation = safetyViolation.get();
        String summary = violation
                ? "Unsafe timing allowed treatment to start before the configuration update completed."
                : "The critical section remained atomic; treatment observed a consistent configuration.";
        return new SimulationResult(
                mode,
                observedAtStart.get(),
                state.snapshot(),
                treatmentStarted.get(),
                violation,
                durationNanos,
                summary,
                log.snapshot());
    }
}
