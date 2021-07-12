package me.lolico.sample.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.net.SocketAddress;

public interface Server {
    void open() throws Throwable;

    void close() throws Throwable;

    Channel getChannel();

    ChannelGroup getChannels();

     static Server bind(SocketAddress socketAddress) {
        return new NettyServer(socketAddress);
    }
}
