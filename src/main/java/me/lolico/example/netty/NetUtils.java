package me.lolico.example.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

public class NetUtils {

    public static InetAddress getLocalAddress() throws UnknownHostException, SocketException {
        InetAddress localAddress = InetAddress.getLocalHost();
        if (acceptAddress(localAddress)) {
            return localAddress;
        }
        return getLocalAddress0();
    }

    private static InetAddress getLocalAddress0() throws UnknownHostException, SocketException {
        for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements(); ) {
            NetworkInterface iface = nics.nextElement();
            if (acceptIface(iface)) {
                continue;
            }
            for (Enumeration<InetAddress> addrs = iface.getInetAddresses(); addrs.hasMoreElements(); ) {
                InetAddress address = addrs.nextElement();
                if (acceptAddress(address)) {
                    return address;
                }
            }
        }
        throw new UnknownHostException();
    }

    private static boolean acceptIface(NetworkInterface iface) throws SocketException {
        return iface != null && !iface.isLoopback() && !iface.isVirtual() && iface.isUp();
    }

    private static boolean acceptAddress(InetAddress address) {
        try {
            return address != null && !address.isLoopbackAddress() &&
                    !address.isAnyLocalAddress() &&
                    !address.isLinkLocalAddress() &&
                    address.isReachable(100);
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        InetAddress address = getLocalAddress();
        System.out.println(Arrays.toString(address.getAddress()));
        System.out.println(address.getHostAddress());
        System.out.println(address.getHostName());
        System.out.println(address.getCanonicalHostName());
    }
}
