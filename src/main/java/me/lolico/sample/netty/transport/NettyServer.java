package me.lolico.sample.netty.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.lolico.sample.netty.NettyEventLoopFactory;
import me.lolico.sample.netty.handler.ServerHandler;
import me.lolico.sample.netty.handler.WatchdogHandler;
import me.lolico.sample.netty.listener.ChannelGroupListener;
import me.lolico.sample.netty.listener.ReconnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class NettyServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final SocketAddress socketAddress;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelGroup channels;
    private Channel channel;

    public NettyServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public void open() throws Exception {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        ClassResolver classResolver = ClassResolvers.softCachingResolver(null);
        bootstrap = new ServerBootstrap();
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss", false);
        workerGroup = NettyEventLoopFactory.eventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, "NettyServerWorker", true);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(90, 0, 0))
                                .addLast(new WatchdogHandler(ReconnectionListener.NO_OP))
                                .addLast(new ChannelGroupListener(channels))
                                .addLast("decoder", new ObjectDecoder(classResolver))
                                .addLast("encoder", new ObjectEncoder())
                                .addLast("handler", new ServerHandler(NettyServer.this){});
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(socketAddress).syncUninterruptibly();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("Server started at {} success!", socketAddress);
            } else {
                log.error("Server started at " + socketAddress + " failed!", future.cause());
            }
        });
        channel = channelFuture.channel();
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
        }
        channels.close();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        bootstrap = null;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelGroup getChannels() {
        return channels;
    }
}
