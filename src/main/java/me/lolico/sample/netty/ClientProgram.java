package me.lolico.sample.netty;

import io.netty.channel.Channel;
import me.lolico.sample.netty.client.Client;

import java.net.InetSocketAddress;

public class ClientProgram {
    public static void main(String[] args) {
        Client client = Client.connect(new InetSocketAddress("0.0.0.0", 7000));
        client.open();
        Channel channel = client.getChannel();
    }
}
