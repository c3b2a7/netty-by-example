package me.lolico.sample.netty;

import me.lolico.sample.netty.server.Server;

import java.net.InetSocketAddress;

public class ServerProgram {
    public static void main(String[] args) throws Throwable {
        Server server = Server.bind(new InetSocketAddress("0.0.0.0", 7000));
        server.open();
    }
}
