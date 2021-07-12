package me.lolico.sample.netty.client;

import io.netty.channel.Channel;

import java.net.SocketAddress;

public interface Client {

    void open();

    void reconnect();

    Channel getChannel();

    static Client connect(SocketAddress socketAddress) {
        return new NettyClient(socketAddress);
    }
}
