package me.lolico.example.netty.entity;

public class Response {

    public static final byte OK = 20;
    public static final byte ERROR = 50;

    private long id;
    private byte status = OK;
    private Object data;

    public Response(long id, Object data) {
        this.id = id;
        this.data = data;
    }

    public Response(long id, byte status, Object data) {
        this.id = id;
        this.status = status;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
