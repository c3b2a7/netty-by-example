package me.lolico.sample.netty.transport;

import org.junit.Test;

import java.net.InetSocketAddress;

public class ProxyServerTest {

    @Test
    public void open() throws Exception {
        ProxyServer server = new ProxyServer(new InetSocketAddress(8000));
        server.open();
        Thread.sleep(1000);
        server.getChannel().closeFuture().sync();
    }
}