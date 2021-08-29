package me.lolico.sample.netty.transport;

import java.net.SocketAddress;

public interface Endpoint {

    void open() throws Throwable;

    void close() throws Throwable;

    static Server bind(SocketAddress socketAddress) {
        return new NettyServer(socketAddress);
    }

    static Client connect(SocketAddress socketAddress) {
        return new NettyClient(socketAddress);
    }
}
