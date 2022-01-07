package me.lolico.example.netty.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.lolico.example.netty.NettyEventLoopFactory;
import me.lolico.example.netty.handler.ProxyServerHandler;
import me.lolico.example.netty.listener.ChannelGroupListener;

import java.net.SocketAddress;

public class ProxyServer implements Server {

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final SocketAddress socketAddress;

    public ProxyServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public void open() throws Exception {
        serverBootstrap = new ServerBootstrap();
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss", false);
        workerGroup = NettyEventLoopFactory.eventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, "NettyServerWorker", false);
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler("LocalTunnel"))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024))
                                .addLast(new ChannelGroupListener(channels))
                                .addLast(new ProxyServerHandler(ProxyServer.this));
                    }
                })
                .bind(socketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ProxyServer.this.serverChannel = future.channel();
                    } else {
                        close();
                    }
                })
                .syncUninterruptibly();
    }

    @Override
    public void close() throws Exception {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        channels.close();
        serverBootstrap = null;
    }

    public Channel getChannel() {
        return serverChannel;
    }

    public ChannelGroup getChannels() {
        return channels;
    }
}
