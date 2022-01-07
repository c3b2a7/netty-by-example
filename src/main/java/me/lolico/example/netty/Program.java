package me.lolico.example.netty;

import me.lolico.example.netty.transport.Endpoint;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Supplier;

public class Program {
    public static void main(String[] args) throws Throwable {
        String type = args[0];
        Supplier<SocketAddress> supplier = () -> new InetSocketAddress(args[1], Integer.parseInt(args[2]));
        if ("--connect".equals(type)) {
            Endpoint.connect(supplier.get()).open();
        } else if ("--bind".equals(type)) {
            Endpoint.bind(supplier.get()).open();
        }
    }
}
