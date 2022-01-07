package me.lolico.example.netty.transport;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface Server extends Endpoint {

    /**
     * Get the channel bound to the given address
     *
     * @return channel
     */
    Channel getChannel();

    /**
     * Get all channels connected to the current server
     *
     * @return A channel group
     */
    ChannelGroup getChannels();

}
