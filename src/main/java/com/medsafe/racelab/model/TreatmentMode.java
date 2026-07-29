package com.medsafe.racelab.model;

public enum TreatmentMode {
    ELECTRON("Electron therapy"),
    X_RAY("X-ray therapy");

    private final String displayName;

    TreatmentMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
