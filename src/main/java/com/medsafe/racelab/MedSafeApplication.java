package com.medsafe.racelab;

import com.medsafe.racelab.auth.AuthService;
import com.medsafe.racelab.simulation.SimulationEngine;
import com.medsafe.racelab.ui.DashboardView;
import com.medsafe.racelab.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public final class MedSafeApplication extends Application {
    private static final double WIDTH = 1360;
    private static final double HEIGHT = 840;

    private final AuthService authService = new AuthService();
    private final SimulationEngine simulationEngine = new SimulationEngine();
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("MedSafe Race Lab - Safety-Critical Java Simulator");
        stage.setMinWidth(1080);
        stage.setMinHeight(700);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView login = new LoginView(authService, this::showDashboard);
        setRoot(login);
    }

    private void showDashboard(String username) {
        DashboardView dashboard = new DashboardView(
                username,
                simulationEngine,
                this::showLogin,
                url -> getHostServices().showDocument(url));
        setRoot(dashboard);
    }

    private void setRoot(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, WIDTH, HEIGHT);
            URL stylesheet = MedSafeApplication.class.getResource(
                    "/com/medsafe/racelab/app.css");
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
