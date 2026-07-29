package com.medsafe.racelab.simulation;

public enum SafetyMode {
    UNSAFE("Unsafe - no synchronization", "No mutual exclusion; timing controls the result."),
    SYNCHRONIZED("synchronized", "Java intrinsic monitor protects the complete transaction."),
    REENTRANT_LOCK("ReentrantLock", "Explicit lock/unlock with guaranteed cleanup in finally."),
    SEMAPHORE("Semaphore(1)", "A single fair permit provides mutual exclusion.");

    private final String displayName;
    private final String description;

    SafetyMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
