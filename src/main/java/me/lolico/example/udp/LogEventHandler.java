package me.lolico.example.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LogEventHandler extends SimpleChannelInboundHandler<LogEvent> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent event) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(event.file());
        builder.append("] [");
        builder.append(event.srcAddr());
        builder.append("] : ");
        builder.append(event.msg());
        System.out.println(builder);
    }
}