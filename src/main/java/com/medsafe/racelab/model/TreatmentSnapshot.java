package com.medsafe.racelab.model;

public record TreatmentSnapshot(
        TreatmentMode mode,
        int energyMeV,
        boolean targetInPlace,
        boolean verified,
        long revision) {

    public boolean isSafeConfiguration() {
        return switch (mode) {
            case ELECTRON -> !targetInPlace && energyMeV >= 6 && energyMeV <= 20;
            case X_RAY -> targetInPlace && energyMeV >= 20 && energyMeV <= 25;
        };
    }

    public String safetyExplanation() {
        if (isSafeConfiguration()) {
            return mode == TreatmentMode.X_RAY
                    ? "X-ray mode has the target in place and an allowed energy."
                    : "Electron mode has no target in the beam path and an allowed energy.";
        }

        if (mode == TreatmentMode.X_RAY && !targetInPlace) {
            return "X-ray mode was selected before the target was in place.";
        }
        if (mode == TreatmentMode.ELECTRON && targetInPlace) {
            return "Electron mode must not use the X-ray target.";
        }
        return "The selected energy is incompatible with the treatment mode.";
    }
}
