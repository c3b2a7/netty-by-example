package me.lolico.example.netty.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.lolico.example.netty.handler.AbstractServerHandler;
import me.lolico.example.netty.handler.ForwardingHandler;
import me.lolico.example.netty.listener.ChannelGroupListener;

import java.net.SocketAddress;

public class TransparentProxyServer extends AbstractServer {

    private final SocketAddress upstream;

    public TransparentProxyServer(SocketAddress socketAddress, SocketAddress upstream) {
        super(1, Runtime.getRuntime().availableProcessors() + 1,
                socketAddress, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        this.upstream = upstream;
    }

    @Override
    protected ChannelInitializer<Channel> childChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline()
                        .addLast(new LoggingHandler("LocalTunnel", LogLevel.INFO, ByteBufFormat.SIMPLE))
                        // .addLast(new HttpServerCodec())
                        // .addLast(new HttpObjectAggregator(1024 * 1024))
                        .addLast(new ChannelGroupListener(TransparentProxyServer.this.getChannels()))
                        .addLast(new AbstractServerHandler(TransparentProxyServer.this) {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                Channel local = ctx.channel();
                                new Bootstrap()
                                        .group(local.eventLoop())
                                        .channel(local.getClass())
                                        .handler(new ChannelInitializer<>() {
                                            @Override
                                            protected void initChannel(Channel ch) throws Exception {
                                                ch.pipeline().addLast(new LoggingHandler("RemoteTunnel", LogLevel.INFO, ByteBufFormat.SIMPLE));
                                            }
                                        })
                                        .connect(upstream)
                                        .addListener((ChannelFutureListener) future -> {
                                            if (future.isSuccess()) {
                                                Channel remote = future.channel();
                                                local.pipeline().remove(this);
                                                local.pipeline().addLast(new ForwardingHandler(remote));
                                                remote.pipeline().addLast(new ForwardingHandler(local));
                                                remote.writeAndFlush(msg);
                                            } else {
                                                local.close();
                                            }
                                        });
                            }
                        });
            }
        };
    }

    @Override
    protected ServerBootstrap preProcessBootstrap(ServerBootstrap serverBootstrap) {
        return serverBootstrap
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
    }
}
