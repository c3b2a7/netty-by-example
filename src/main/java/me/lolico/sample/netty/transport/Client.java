package me.lolico.sample.netty.transport;

import io.netty.channel.Channel;

public interface Client extends Endpoint {

    void reconnect();

    Channel getChannel();

}
