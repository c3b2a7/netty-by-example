package me.lolico.example.netty.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.lolico.example.netty.transport.Server;

public abstract class AbstractServerHandler extends ChannelInboundHandlerAdapter {

    private final Server server;

    public AbstractServerHandler(Server server) {
        this.server = server;
    }
}
