package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface NtfyConnection {

    /**
 * Sends a message over the connection and notifies the caller of the outcome.
 *
 * @param message  the payload to send
 * @param callback consumer invoked with `true` if the message was sent successfully, `false` otherwise
 */
void send(String message, Consumer<Boolean> callback);

    /**
 * Registers a handler that will be invoked for each incoming message.
 *
 * @param messageHandler consumer invoked with each received {@link NtfyMessageDto}
 */
void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
     * Retrieves the current topic used by this connection.
     *
     * @return the current topic string; by default returns "mytopic".
     */
    default String getCurrentTopic() {
        return "mytopic";
    }

    /**
     * Sets the connection's current topic.
     *
     * The default implementation does nothing.
     *
     * @param topic the topic to set; ignored by the default implementation
     */
    default void setCurrentTopic(String topic) {

    }

    /**
     * Get the identifier of the current user.
     *
     * Default implementation returns "unknown".
     *
     * @return the user identifier; "unknown" when not specified
     */
    default String getUserId() {
        return "unknown";
    }
}