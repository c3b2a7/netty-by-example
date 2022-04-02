package me.lolico.example.netty.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.lolico.example.netty.NettyEventLoopFactory;
import me.lolico.example.netty.handler.AbstractServerHandler;
import me.lolico.example.netty.handler.WatchdogHandler;
import me.lolico.example.netty.listener.ChannelGroupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class NettyServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final SocketAddress socketAddress;

    public NettyServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public void open() throws Exception {
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss", false);
        workerGroup = NettyEventLoopFactory.eventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, "NettyServerWorker", true);
        new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(90, 0, 0))
                                .addLast(new WatchdogHandler())
                                .addLast(new ChannelGroupListener(channels))
                                .addLast("decoder", new ObjectDecoder(ClassResolvers.softCachingResolver(null)))
                                .addLast("encoder", new ObjectEncoder())
                                .addLast("handler", new AbstractServerHandler(NettyServer.this){});
                    }
                })
                .bind(socketAddress)
                .addListener((ChannelFuture future) -> {
                    if (future.isSuccess()) {
                        serverChannel = future.channel();
                        log.info("Server started at {} success!", socketAddress);
                    } else {
                        close();
                        log.error("Server started at " + socketAddress + " failed!", future.cause());
                    }
                })
                .syncUninterruptibly();
    }

    @Override
    public void close() throws Exception {
        if (serverChannel != null) {
            serverChannel.close();
        }
        channels.close();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public Channel getServerChannel() {
        return serverChannel;
    }

    @Override
    public ChannelGroup getChannels() {
        return channels;
    }
}
