package me.lolico.example.netty.entity;

import java.util.concurrent.atomic.AtomicLong;


public class Request {

    public static final byte NORMAL = 1;
    public static final byte PING = 2;
    public static final byte PONG = 3;

    private static final AtomicLong INVOKE_ID = new AtomicLong(1);

    private final long id = INVOKE_ID.getAndIncrement();
    private byte type = NORMAL;
    private Object data;

    public Request(Object data) {
        this.data = data;
    }

    public Request(byte type, Object data) {
        this.type = type;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
