package me.lolico.example.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.concurrent.ThreadFactory;

public class NettyEventLoopFactory {

    public static EventLoopGroup eventLoopGroup(int threads, String threadFactoryName, boolean isDaemon) {
        ThreadFactory threadFactory = new DefaultThreadFactory(threadFactoryName, isDaemon);
        return shouldEpoll() ? new EpollEventLoopGroup(threads, threadFactory) :
                new NioEventLoopGroup(threads, threadFactory);
    }

    public static Class<? extends SocketChannel> socketChannelClass() {
        return shouldEpoll() ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return shouldEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> udpChannelClass() {
        return shouldEpoll() ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    private static boolean shouldEpoll() {
        String osName = SystemPropertyUtil.get("os.name");
        return osName.toLowerCase().contains("linux") && Epoll.isAvailable();
    }
}
