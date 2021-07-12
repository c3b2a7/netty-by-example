package me.lolico.sample.netty.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import me.lolico.sample.netty.client.Client;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }
}
