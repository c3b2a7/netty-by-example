package me.lolico.example.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.util.concurrent.GenericFutureListener;
import me.lolico.example.netty.NettyEventLoopFactory;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

public class LogEventBroadcaster implements GenericFutureListener<ChannelFuture> {

    private final Bootstrap bootstrap;
    private final String file;

    public static final int MAX_UDP_SIZE = 512;

    public LogEventBroadcaster(InetSocketAddress dstAddress, String file) {
        this.file = file;
        bootstrap = new Bootstrap();
        bootstrap.group(NettyEventLoopFactory.eventLoopGroup(1, "UDP-Worker", false))
                .channel(NettyEventLoopFactory.udpChannelClass())
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(dstAddress));
    }

    public void run() {
        ChannelFuture channelFuture = bootstrap.bind(0).syncUninterruptibly();
        channelFuture.addListener(this);
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            Channel channel = future.channel();
            long pointer = 0;
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                for (; ; ) {
                    long len = raf.length();
                    if (len < pointer) {
                        len = len > MAX_UDP_SIZE ? MAX_UDP_SIZE : len;
                        channel.writeAndFlush(new LogEvent(file, 0, (int) len));
                        pointer = len;
                    } else if (len > pointer) {
                        int bytes = (int) (len - pointer);
                        if (bytes > MAX_UDP_SIZE) {
                            channel.writeAndFlush(new LogEvent(file, pointer, MAX_UDP_SIZE));
                            pointer += MAX_UDP_SIZE;
                        } else {
                            channel.writeAndFlush(new LogEvent(file, pointer, bytes));
                            pointer = len;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Thread.interrupted();
                        break;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(new InetSocketAddress(args[0], Integer.parseInt(args[1])), args[2]);
        broadcaster.run();
    }
}
