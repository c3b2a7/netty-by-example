package me.lolico.sample.netty.listener;

public interface ReconnectionListener {

    ReconnectionListener NO_OP = attempt -> {
    };

    /**
     * Listener method notified on a reconnection attempt.
     *
     * @param attempt number of attempts.
     */
    void onReconnectAttempt(int attempt);
}
