package com.medsafe.racelab.simulation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class ScenarioCoordinator {
    final CountDownLatch modeChanged = new CountDownLatch(1);
    final CountDownLatch treatmentAttempted = new CountDownLatch(1);
    final CountDownLatch unsafeObservationComplete = new CountDownLatch(1);

    void await(CountDownLatch latch, String step) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while coordinating: " + step);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulation was interrupted during: " + step, exception);
        }
    }
}
