package me.lolico.sample.netty.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.lolico.sample.netty.transport.Server;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final Server server;

    public ServerHandler(Server server) {
        this.server = server;
    }
}
