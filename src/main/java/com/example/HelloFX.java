package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloFX extends Application {

    /**
     * Initializes the JavaFX user interface, sets up the scene and stylesheet, and shows the primary stage.
     *
     * Loads the FXML layout "hello-view.fxml", creates a Scene sized 768×576, sets the window title to
     * "RuneChat", applies the "style.css" stylesheet, attaches the scene to the provided stage, and displays it.
     *
     * @param stage the primary stage provided by the JavaFX runtime to host the application scene
     * @throws Exception if the FXML or stylesheet resources cannot be loaded or another initialization error occurs
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 768, 576);
        stage.setTitle("RuneChat");

        scene.getStylesheets().add(Objects.requireNonNull(HelloFX.class.getResource("style.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();


    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments; unused by this application
     */
    public static void main(String[] args) {
        launch();
    }

}