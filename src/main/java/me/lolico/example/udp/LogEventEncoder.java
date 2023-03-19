package me.lolico.example.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LogEventEncoder extends MessageToMessageEncoder<LogEvent> {

    private final InetSocketAddress dstAddr;

    public LogEventEncoder(InetSocketAddress dstAddr) {
        this.dstAddr = dstAddr;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, LogEvent msg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer(msg.file().length() + msg.length() + 1);
        buffer.writeBytes(msg.file().getBytes(StandardCharsets.UTF_8));
        buffer.writeByte(LogEvent.SEPARATOR);
        try (RandomAccessFile fd = new RandomAccessFile(msg.file(), "r")) {
            buffer.writeBytes(fd.getChannel(), msg.position(), msg.length());
        }
        out.add(new DatagramPacket(buffer, dstAddr));
    }
}
