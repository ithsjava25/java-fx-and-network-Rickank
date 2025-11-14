package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final String userId;
    private String currentTopic;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a connection configured from environment variables.
     *
     * Reads `HOST_NAME` and `USER_ID` from the environment and sets the connection's topic
     * to the value of `DEFAULT_TOPIC` if present, or `"mytopic"` otherwise.
     *
     * @throws NullPointerException if `HOST_NAME` or `USER_ID` is not set in the environment
     */
    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        this.hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        this.userId = Objects.requireNonNull(dotenv.get("USER_ID"), "USER_ID");
        this.currentTopic = dotenv.get("DEFAULT_TOPIC", "mytopic");
    }

    /**
     * Initialize a connection targeting the specified host, using default credentials and topic.
     *
     * <p>The instance will use userId "testuser" and topic "mytopic" unless changed.
     *
     * @param hostName the base URL of the ntfy host to connect to
     */
    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
        this.userId = "testuser";
        this.currentTopic = "mytopic";
    }

    /**
     * Constructs a NtfyConnectionImpl configured with the specified host, user, and topic.
     *
     * @param hostName the base URL of the ntfy host to connect to
     * @param userId   the user identifier to include with requests
     * @param topic    the initial topic name to use for sending and receiving messages
     */
    public NtfyConnectionImpl(String hostName, String userId, String topic) {
        this.hostName = hostName;
        this.userId = userId;
        this.currentTopic = topic;
    }

    /**
     * User identifier used in outgoing requests.
     *
     * @return the configured user identifier
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the current topic used for sending and receiving messages.
     *
     * @return the name of the current topic
     */
    public String getCurrentTopic() {
        return currentTopic;
    }

    /**
     * Updates the topic used for subsequent send and receive operations.
     *
     * @param topic the new topic name to set
     */
    public void setCurrentTopic(String topic) {
        this.currentTopic = topic;
    }

    /**
     * Sends the given message to the current topic on the configured host and delivers whether the send succeeded to the callback.
     *
     * @param message the message body to post to the current topic
     * @param callback consumer invoked with `true` if the HTTP response status code is in the 2xx range, `false` otherwise
     */
    @Override
    public void send(String message, Consumer<Boolean> callback) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .header("X-User-Id", userId)
                .uri(URI.create(hostName + "/" + currentTopic))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> response.statusCode() / 100 == 2)
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return false;
                })
                .thenAccept(callback);
    }

    /**
     * Subscribes to the current topic and delivers each successfully parsed message to the provided handler.
     *
     * <p>Lines received from the topic are parsed as JSON into {@code NtfyMessageDto}; messages that fail parsing are skipped.</p>
     *
     * @param messageHandler consumer invoked for each parsed {@code NtfyMessageDto} received from the topic
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/" + currentTopic + "/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.out.println("Failed to parse message: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }
}