package me.lolico.example.udp;


import java.net.InetSocketAddress;
import java.util.Objects;

public final class LogEvent {
    public static final byte SEPARATOR = ':';

    private String file;
    private long position;
    private int length;

    private InetSocketAddress srcAddr;
    private String msg;

    public LogEvent(String file, long position, int length) {
        this.file = file;
        this.position = position;
        this.length = length;
    }

    public LogEvent(String file, String msg, InetSocketAddress srcAddr) {
        this.file = file;
        this.msg = msg;
        this.srcAddr = srcAddr;
    }

    public String file() {
        return file;
    }

    public long position() {
        return position;
    }

    public int length() {
        return length;
    }

    public InetSocketAddress srcAddr() {
        return srcAddr;
    }

    public String msg() {
        return msg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LogEvent) obj;
        return Objects.equals(this.file, that.file) &&
                this.position == that.position &&
                this.length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, position, length);
    }

    @Override
    public String toString() {
        return "LogEvent[" +
                "file=" + file + ", " +
                "position=" + position + ", " +
                "length=" + length + ']';
    }

}
