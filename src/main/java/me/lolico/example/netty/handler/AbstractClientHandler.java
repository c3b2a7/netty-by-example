package me.lolico.example.netty.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.lolico.example.netty.transport.Client;

public abstract class AbstractClientHandler extends ChannelInboundHandlerAdapter {

    private final Client client;

    public AbstractClientHandler(Client client) {
        this.client = client;
    }
}
