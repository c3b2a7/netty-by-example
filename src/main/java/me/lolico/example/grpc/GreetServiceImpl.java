package me.lolico.example.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import me.lolico.example.proto.GreetServiceGrpc;
import me.lolico.example.proto.MessageProto.Request;
import me.lolico.example.proto.MessageProto.Response;

import java.nio.charset.StandardCharsets;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void hello(Request request, StreamObserver<Response> responseObserver) {
        Response response = null;
        try {
            response = buildResponse(request);
        } catch (Throwable throwable) {
            responseObserver.onError(throwable);
        }
        if (response != null) {
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Request> helloStream(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Request request) {
                try {
                    responseObserver.onNext(buildResponse(request));
                } catch (Throwable throwable) {
                    responseObserver.onError(throwable);
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
            }
        };
    }

    private Response buildResponse(Request request) throws Throwable {
        return Response.newBuilder()
                .setId(request.getId())
                .setStatus(Response.Status.OK)
                .setData(ByteString.copyFrom("Hello Grpc".getBytes(StandardCharsets.UTF_8)))
                .build();
    }
}
