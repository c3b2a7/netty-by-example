package me.lolico.sample.netty.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import me.lolico.sample.netty.NettyEventLoopFactory;
import me.lolico.sample.netty.handler.ClientHandler;
import me.lolico.sample.netty.handler.WatchdogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    private final SocketAddress socketAddress;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    public NettyClient(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public void open() {
        bootstrap = new Bootstrap();
        ClassResolver classResolver = ClassResolvers.softCachingResolver(null);
        eventLoopGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyClientEventLoopGroup", false);
        bootstrap.group(eventLoopGroup)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 30, 0))
                                .addLast(new WatchdogHandler(attempt -> {
                                    log.info("Channel has disconnected, start to reconnect...");
                                    NettyClient.this.reconnect();
                                }))
                                .addLast("decoder", new ObjectDecoder(classResolver))
                                .addLast("encoder", new ObjectEncoder())
                                .addLast("handler", new ClientHandler(NettyClient.this));
                    }
                });
        doConnect();
    }

    @Override
    public void close() throws Throwable {
        if (channel != null) {
            channel.close();
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        bootstrap = null;
    }

    private void doConnect() {
        ChannelFuture channelFuture = bootstrap.connect(socketAddress);
        channel = channelFuture.channel();
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Client connect to {} success!", socketAddress);
            } else {
                log.error("Client connect to " + socketAddress + " failed!, auto reconnecting...", future.cause());
                EventLoop eventLoop = future.channel().eventLoop();
                eventLoop.schedule(this::reconnect, 5 * 1000, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void reconnect() {
        if (channel != null && !channel.isActive()) {
            doConnect();
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
