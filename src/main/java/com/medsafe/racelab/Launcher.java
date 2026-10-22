package com.medsafe.racelab;

/** Entry point for the shaded (fat) JAR; JavaFX cannot start from a class that extends Application. */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(MedSafeApplication.class, args);
    }
}
