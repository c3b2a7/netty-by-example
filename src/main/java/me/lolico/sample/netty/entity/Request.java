package me.lolico.sample.netty.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
@AllArgsConstructor
public class Request {

    public static final byte NORMAL = 1;
    public static final byte PING = 2;
    public static final byte PONG = 3;

    private static final AtomicLong INVOKE_ID = new AtomicLong(1);

    private final long id = INVOKE_ID.getAndIncrement();
    private byte type = NORMAL;
    private Object data;

}
