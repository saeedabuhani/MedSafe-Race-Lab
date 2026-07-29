package com.medsafe.racelab.ui;

import com.medsafe.racelab.simulation.SafetyMode;

final class CodeSamples {
    private CodeSamples() {
    }

    static String forMode(SafetyMode mode) {
        return switch (mode) {
            case UNSAFE -> """
                    // UNSAFE: the shared treatment state is changed in steps.
                    void changeToXRay() {
                        state.mode = X_RAY;       // intermediate unsafe state
                        state.verified = false;
                        state.energyMeV = 25;
                        state.targetInPlace = true;
                        state.verified = true;
                    }

                    void startTreatment() {
                        // Another thread may be halfway through changeToXRay().
                        if (state.verified) {
                            beamOn(state);        // check and use are not atomic
                        }
                    }
                    """;
            case SYNCHRONIZED -> """
                    private final Object monitor = new Object();

                    void changeToXRay() {
                        synchronized (monitor) {
                            state.mode = X_RAY;
                            state.verified = false;
                            state.energyMeV = 25;
                            state.targetInPlace = true;
                            state.verified = true;
                        } // monitor is released automatically
                    }

                    void startTreatment() {
                        synchronized (monitor) {
                            if (state.verified && state.isSafe()) {
                                beamOn(state);
                            }
                        }
                    }
                    """;
            case REENTRANT_LOCK -> """
                    private final ReentrantLock lock = new ReentrantLock(true);

                    void changeToXRay() {
                        lock.lock();
                        try {
                            updateCompleteTreatmentConfiguration();
                        } finally {
                            lock.unlock(); // always release in finally
                        }
                    }

                    void startTreatment() {
                        lock.lock();
                        try {
                            if (state.verified && state.isSafe()) {
                                beamOn(state);
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                    """;
            case SEMAPHORE -> """
                    private final Semaphore mutex = new Semaphore(1, true);

                    void startTreatment() throws InterruptedException {
                        boolean acquired = false;
                        try {
                            mutex.acquire();
                            acquired = true;
                            if (state.verified && state.isSafe()) {
                                beamOn(state);
                            }
                        } finally {
                            if (acquired) {
                                mutex.release();
                            }
                        }
                    }

                    // The update method uses the same one-permit semaphore.
                    // Semaphore(2) would allow two threads into the section.
                    """;
        };
    }
}
