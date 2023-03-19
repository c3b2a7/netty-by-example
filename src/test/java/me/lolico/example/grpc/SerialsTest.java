package me.lolico.example.grpc;


import com.google.protobuf.Any;
import me.lolico.example.proto.Message;
import org.junit.Test;

public class SerialsTest {
    @Test
    public void test() {
        Message.Request req = Message.Request.newBuilder()
                .setId(0)
                .setType(Message.Request.Type.PING)
                .build();

        System.out.println(Serials.getMessageType(req));

        // converts Message into Any
        Any any = Serials.toAnyMessage(req);
        System.out.println(Serials.getMessageType(any));

        // converts Any into Message
        Message.Request protoMessage = Serials.toProtoMessage(any);
        System.out.println(Serials.getMessageType(protoMessage));

        // equals to original req
        System.out.println(protoMessage.equals(req));
    }
}
