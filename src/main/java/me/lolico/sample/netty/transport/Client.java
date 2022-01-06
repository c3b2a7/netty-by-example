package me.lolico.sample.netty.transport;

import io.netty.channel.Channel;

public interface Client extends Endpoint {

    /**
     * Reconnect server
     */
    void reconnect() throws Exception;

    /**
     * Get the channel connected to the server
     *
     * @return channel
     */
    Channel getChannel();

}
