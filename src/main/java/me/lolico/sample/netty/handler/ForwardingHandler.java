package me.lolico.sample.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ForwardingHandler extends ChannelInboundHandlerAdapter {

    private final Channel target;

    public ForwardingHandler(Channel target) {
        if (target == null){
            throw new IllegalArgumentException("target");
        }
        this.target = target;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (target.isActive()) {
            target.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        target.writeAndFlush(msg);
    }
}
