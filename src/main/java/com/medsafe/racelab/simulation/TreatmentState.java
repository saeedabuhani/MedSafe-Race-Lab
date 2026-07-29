package com.medsafe.racelab.simulation;

import com.medsafe.racelab.model.TreatmentMode;
import com.medsafe.racelab.model.TreatmentSnapshot;

final class TreatmentState {
    private TreatmentMode mode = TreatmentMode.ELECTRON;
    private int energyMeV = 10;
    private boolean targetInPlace;
    private boolean verified = true;
    private long revision = 1;

    TreatmentSnapshot snapshot() {
        return new TreatmentSnapshot(mode, energyMeV, targetInPlace, verified, revision);
    }

    void setMode(TreatmentMode mode) {
        this.mode = mode;
        revision++;
    }

    void setEnergyMeV(int energyMeV) {
        this.energyMeV = energyMeV;
        revision++;
    }

    void setTargetInPlace(boolean targetInPlace) {
        this.targetInPlace = targetInPlace;
        revision++;
    }

    void setVerified(boolean verified) {
        this.verified = verified;
        revision++;
    }
}
