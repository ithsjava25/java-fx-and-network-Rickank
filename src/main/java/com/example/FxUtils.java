package com.example;

import javafx.application.Platform;

public class FxUtils {

    /**
     * Ensures the given task runs on the JavaFX Application Thread when possible.
     *
     * If already on the JavaFX Application Thread the task is executed immediately;
     * if the JavaFX platform is not initialized the task is executed on the current thread.
     *
     * @param task the work to execute
     */
    static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }
}