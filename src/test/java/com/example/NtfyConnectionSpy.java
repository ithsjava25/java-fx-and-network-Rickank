package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    String message;
    Consumer<NtfyMessageDto> handler;

    /**
     * Records the provided message and asynchronously signals success to the callback.
     *
     * <p>Stores the message in the spy's state and invokes the callback with {@code true} on a separate thread.
     *
     * @param message the message to record
     * @param callback callback invoked with {@code true} to indicate simulated send success
     */
    @Override
    public void send(String message, Consumer<Boolean> callback) {
        this.message = message;
        new Thread(() -> callback.accept(true)).start();
    }


    /**
     * Registers a handler to receive incoming Ntfy messages.
     *
     * The provided consumer is stored and will be invoked by simulateIncoming when a message is simulated.
     *
     * @param messageHandler the consumer to call with incoming {@code NtfyMessageDto} instances; may be {@code null} to unregister the handler
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    /**
     * Simulates arrival of a message by delivering the given message to the registered handler if present.
     *
     * @param msg the incoming message to deliver to the registered message handler; ignored if no handler is registered
     */
    public void simulateIncoming(NtfyMessageDto msg) {
        if (handler != null) handler.accept(msg);
    }
}