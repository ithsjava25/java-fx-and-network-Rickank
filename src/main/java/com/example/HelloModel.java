package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

import static com.example.FxUtils.runOnFx;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final StringProperty currentTopic = new SimpleStringProperty();

    /**
     * Create a HelloModel bound to the given NtfyConnection.
     *
     * Initializes the model's current topic from the connection and begins receiving messages for that topic.
     */
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        this.currentTopic.set(connection.getCurrentTopic());
        receiveMessage();
    }

    /**
     * Provides the observable list of received messages for UI binding.
     *
     * @return the live ObservableList of NtfyMessageDto messages.
     */
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Gets the current message text intended for sending.
     *
     * @return the current message text, or {@code null} if no text is set
     */
    public String getMessageToSend() {
        return messageToSend.get();
    }

    /**
     * Exposes the property that holds the text currently entered for sending so the UI can bind to it.
     *
     * @return the `StringProperty` representing the message currently entered for sending
     */
    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    /**
     * Update the text currently entered for sending.
     *
     * @param message the new message text; may be {@code null} to clear the input
     */
    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    /**
     * Retrieve the currently selected topic.
     *
     * @return the current topic, or {@code null} if no topic is set
     */
    public String getCurrentTopic() {
        return currentTopic.get();
    }

    /**
     * Exposes the JavaFX property for the currently selected topic so callers can observe or bind to it.
     *
     * @return the StringProperty holding the current topic value
     */
    public StringProperty currentTopicProperty() {
        return currentTopic;
    }

    /**
     * Update the model's current topic and refresh messages when a non-empty topic is provided.
     *
     * <p>If the provided topic is non-null and not blank, this updates the connection's current
     * topic, updates the model's currentTopic property, clears the message list, and restarts
     * receiving messages for the new topic. If the topic is null or blank, no action is taken.
     *
     * @param topic the new topic to set; ignored if null or blank
     */
    public void setCurrentTopic(String topic) {
        if (topic != null && !topic.isBlank()) {
            connection.setCurrentTopic(topic);
            this.currentTopic.set(topic);
            messages.clear();
            receiveMessage();
        }
    }

    /**
     * Provides the current user's identifier.
     *
     * @return the current user's identifier
     */
    public String getUserId() {
        return connection.getUserId();
    }

    /**
     * Retrieve the application's greeting text.
     *
     * @return the greeting text "RuneChat"
     */
    public String getGreeting() {
        return "RuneChat";
    }

    /**
     * Determine whether the current draft message is eligible to be sent.
     *
     * @return `true` if the draft message is non-null and contains at least one non-whitespace character, `false` otherwise.
     */
    public boolean canSendMessage() {
        String msg = messageToSend.get();
        return msg != null && !msg.isBlank();
    }

    /**
     * Sends the current message through the connection and reports the outcome via the provided callback.
     *
     * If the message is null or blank the callback is invoked with `false` and no send is attempted. On successful send,
     * the input is cleared only if it has not changed since sending. The callback is invoked with `true` on success and
     * `false` on failure.
     *
     * @param callback consumer invoked with `true` when the message was sent successfully, `false` otherwise
     */
    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            System.out.println("Nothing to send!");
            callback.accept(false);
            return;
        }

        connection.send(msg, success -> {
            if (success) {
                runOnFx(() -> {
                    if (msg.equals(messageToSend.get())) {
                        messageToSend.set("");
                    }
                });
                callback.accept(true);
            } else {
                System.out.println("Failed to send message!");
                callback.accept(false);
            }
        });
    }

    /**
     * Subscribes to the connection's incoming messages and appends each received message with a non-null, non-blank text to the observable messages list on the JavaFX Application Thread.
     *
     * Messages that are null or whose message text is null or blank are ignored.
     */
    public void receiveMessage() {
        connection.receive(m -> {
            if (m == null || m.message() == null || m.message().isBlank()) return;
            runOnFx(() -> messages.add(m));
        });
    }


}