package me.lolico.example.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.HashMap;
import java.util.Map;

public abstract class Serials {

    private static final String TYPE_URL_PREFIX = "types.lolico.me/";

    public static Any toAnyMessage(Message message) {
        if (message == null) {
            return null;
        }
        String messageType = getMessageType(message);
        MessageTypeLookup.register(messageType, message);
        return Any.pack(message, TYPE_URL_PREFIX);
    }

    public static <T extends Message> T toProtoMessage(Any any) {
        if (any == null) {
            return null;
        }
        String type = internalType(any);
        Class<T> clazz = MessageTypeLookup.lookup(type);
        if (clazz == null) {
            return null;
        }
        try {
            return any.unpack(clazz);
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public static String getMessageType(Message message) {
        return message.getDescriptorForType().getFullName();
    }

    private static String internalType(Any any) {
        String typeUrl = any.getTypeUrl();
        if (typeUrl.contains(TYPE_URL_PREFIX)) {
            return typeUrl.substring(TYPE_URL_PREFIX.length());
        }
        return typeUrl;
    }


    private static class MessageTypeLookup {

        protected static final Map<String, Class<?>> registry = new HashMap<>();

        public static void register(String type, Message message) {
            registry.put(type, message.getClass());
        }

        @SuppressWarnings("unchecked")
        public static <T> Class<T> lookup(String type) {
            return (Class<T>) registry.get(type);
        }
    }
}
