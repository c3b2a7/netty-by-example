package me.lolico.example.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import me.lolico.example.netty.listener.ReconnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * A watchdog used to perform some actions when an event is triggered.
 *
 * @see ReconnectionListener
 */
public class WatchdogHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WatchdogHandler.class);
    private static final ByteBuf HEARTBEAT = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer("heartbeat".getBytes(StandardCharsets.UTF_8)));

    private int attempts = 0;
    private final ReconnectionListener reconnectionListener;

    public WatchdogHandler() {
        this.reconnectionListener = ReconnectionListener.NO_OP;
    }

    public WatchdogHandler(ReconnectionListener reconnectionListener) {
        this.reconnectionListener = reconnectionListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        attempts = 0;
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        reconnectionListener.onReconnectAttempt(++attempts);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg.equals(HEARTBEAT)) {
            log.debug("Received heartbeat from {}", ctx.channel().remoteAddress());
            ReferenceCountUtil.release(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(HEARTBEAT);
            } else if (state == IdleState.READER_IDLE) {
                ctx.close();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
