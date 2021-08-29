package me.lolico.sample.netty.transport;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface Server extends Endpoint {

    Channel getChannel();

    ChannelGroup getChannels();

}
