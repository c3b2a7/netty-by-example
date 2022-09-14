package me.lolico.example.netty;

import me.lolico.example.netty.transport.Endpoint;
import me.lolico.example.netty.transport.TransparentProxyServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Supplier;

public class Program {
    public static void main(String[] args) throws Throwable {
        String type = args[0];
        Supplier<SocketAddress> supplier = () -> new InetSocketAddress(args[1], Integer.parseInt(args[2]));
        Endpoint endpoint = switch (type) {
            case "-c", "--connect" -> Endpoint.connect(supplier.get());
            case "-b", "--bind" -> Endpoint.bind(supplier.get());
            case "-tp", "--transparentProxy" -> Endpoint.transparentProxy(supplier.get());
            case "-us", "--upstream" -> new TransparentProxyServer(supplier.get(), new InetSocketAddress(args[3], Integer.parseInt(args[4])));
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        endpoint.open();
    }
}
