package me.lolico.example.netty.transport;

import io.netty.channel.Channel;

import java.net.SocketAddress;

public interface Endpoint {

    /**
     * Open this endpoint
     */
    void open() throws Exception;

    /**
     * Close this endpoint
     */
    void close() throws Exception;

    /**
     * Get the channel connected to the endpoint
     *
     * @return channel
     */
    Channel getChannel();

    static Server bind(SocketAddress socketAddress) {
        return new NettyServer(socketAddress);
    }

    static Client connect(SocketAddress socketAddress) {
        return new NettyClient(socketAddress);
    }
}
