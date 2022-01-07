package me.lolico.example.netty.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Response {

    public static final byte OK = 20;
    public static final byte ERROR = 50;

    private long id;
    private byte status = OK;
    private Object data;
}
