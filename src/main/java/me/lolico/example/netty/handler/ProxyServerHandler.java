package me.lolico.example.netty.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import me.lolico.example.netty.transport.Server;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ProxyServerHandler extends AbstractServerHandler {
    public ProxyServerHandler(Server server) {
        super(server);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) msg;
            final Channel localChannel = ctx.channel();

            Tuple2<String, Integer> hostAndPort = parseHostAndPort(httpRequest);
            localChannel.config().setAutoRead(false);

            new Bootstrap()
                    .group(localChannel.eventLoop())
                    .channel(localChannel.getClass())
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler("RemoteTunnel"))
                                    .addLast(new HttpRequestEncoder());
                        }
                    })
                    .connect(hostAndPort.getT1(), hostAndPort.getT2())
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            final Channel remoteChannel = future.channel();

                            if (httpRequest.method() == HttpMethod.CONNECT) {
                                localChannel.writeAndFlush(new DefaultHttpResponse(
                                        httpRequest.protocolVersion(), HttpResponseStatus.OK));
                            } else {
                                remoteChannel.writeAndFlush(httpRequest);
                            }
                            // ???????????????Http????????????????????????????????????????????? Http ?????????????????????????????? TCP ????????????
                            // ????????????????????????????????? localChannel ??????????????????????????? remoteChannel ?????????????????? ForwardingHandler ????????????
                            // ????????????????????????????????????
                            // ?????????   --->  localChannel  --->  tunnel ---> remoteChannel ---> ????????????
                            // ???????????? --->  remoteChannel  --->  tunnel ---> localChannel ---> ?????????
                            localChannel.pipeline().remove(HttpServerCodec.class);
                            localChannel.pipeline().remove(HttpObjectAggregator.class);
                            localChannel.pipeline().remove(ProxyServerHandler.class);
                            localChannel.pipeline().addLast(new ForwardingHandler(remoteChannel));

                            remoteChannel.pipeline().remove(HttpRequestEncoder.class);
                            remoteChannel.pipeline().addLast(new ForwardingHandler(localChannel));

                            // Everything is ready, start automatic forwarding
                            localChannel.config().setAutoRead(true);
                        } else {
                            localChannel.close();
                        }
                    });
        }
    }

    private Tuple2<String, Integer> parseHostAndPort(HttpRequest httpRequest) {
        String[] hostAndPort;
        if (httpRequest.method() == HttpMethod.CONNECT) {
            hostAndPort = httpRequest.uri().split(":");
        } else {
            hostAndPort = httpRequest.headers()
                    .get(HttpHeaderNames.HOST).split(":");
        }
        if (hostAndPort.length == 2) {
            return Tuples.<String, Integer>fn2().apply(new Object[]{
                    hostAndPort[0], Integer.parseInt(hostAndPort[1])
            });
        }
        // Use protocol default port
        if (httpRequest.method() == HttpMethod.CONNECT) {
            // HTTPS
            return Tuples.of(hostAndPort[0], 443);
        } else {
            // HTTP
            return Tuples.of(hostAndPort[0], 80);
        }
    }
}
