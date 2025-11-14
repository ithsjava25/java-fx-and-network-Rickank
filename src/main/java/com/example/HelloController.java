package com.example;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private Button sendButton;

    @FXML
    private Label messageLabel;

    @FXML
    private Label topicLabel;

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextArea messageInput;

    @FXML
    private TextField topicInput;

    @FXML
    private Button changeTopicButton;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    /**
     * Initializes the controller by binding UI components to the model, configuring controls, and setting up the message view.
     *
     * <p>Sets the initial greeting and topic display, requests focus for the message input, binds the message list
     * and the message input to the model, disables send/change-topic buttons when their inputs are empty, configures
     * the message list cells to render chat bubbles aligned and styled for sent versus received messages, and adds
     * a listener to auto-scroll to the latest message.</p>
     */
    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());

        Platform.runLater(() -> messageInput.requestFocus());

        topicLabel.setText("/" + model.getCurrentTopic());
        model.currentTopicProperty().addListener((obs, oldVal, newVal) -> {
            topicLabel.setText("/" + newVal);
        });

        messageView.setItems(model.getMessages());

        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    String text = messageInput.getText();
                    return text == null || text.trim().isEmpty();
                },
                messageInput.textProperty()
        ));

        if (changeTopicButton != null) {
            changeTopicButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> {
                        String text = topicInput.getText();
                        return text == null || text.trim().isEmpty();
                    },
                    topicInput.textProperty()
            ));
        }


        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null || msg.message() == null || msg.message().isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Skapa bubble-label
                    Label bubble = new Label(msg.message());
                    bubble.setWrapText(true);
                    bubble.setMaxWidth(250);
                    bubble.setPadding(new Insets(10));
                    bubble.getStyleClass().add("chat-bubble"); // Basstyle

                    HBox container = new HBox(bubble);
                    container.setPadding(new Insets(5));

                    // Använd CSS-klasser för skickat/mottaget
                    if (model.getUserId().equals(msg.id())) {
                        bubble.getStyleClass().add("chat-bubble-sent");
                        container.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        bubble.getStyleClass().add("chat-bubble-received");
                        container.setAlignment(Pos.CENTER_LEFT);
                    }

                    setText(null);
                    setGraphic(container);
                }
            }
        });


        // Scrolla ner till senaste meddelandet
        model.getMessages().addListener((javafx.collections.ListChangeListener<NtfyMessageDto>) change -> {
            Platform.runLater(() -> {
                if (!messageView.getItems().isEmpty()) {
                    messageView.scrollTo(messageView.getItems().size() - 1);
                }
            });
        });
    }

    /**
     * Sends the composed message asynchronously and updates the UI according to the send result.
     *
     * On success, clears the message input and refocuses it. On failure, displays an error alert
     * indicating the message could not be sent.
     */
    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        model.sendMessageAsync(success -> {
            if (success) {
                Platform.runLater(() -> messageInput.clear());
                Platform.runLater(() -> messageInput.requestFocus());
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Send Failed");
                    alert.setHeaderText("Failed to send message");
                    alert.setContentText("Could not send your message. Please try again.");
                    alert.showAndWait();
                });
            }
        });
    }

    /**
     * Updates the model's current topic from the topic input field and clears the input.
     *
     * If the topic input contains non-whitespace text, sets the model's current topic to that value and then clears the topic input field.
     */
    @FXML
    private void changeTopic(ActionEvent actionEvent) {
        String newTopic = topicInput.getText();
        if (newTopic != null && !newTopic.isBlank()) {
            model.setCurrentTopic(newTopic);
            topicInput.clear();
        }
    }
}