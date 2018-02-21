package pingpong;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetSocketAddress;

public class ServerMain {
    public static void main(String[] args) {
        PingPongServer server = new PingPongServerImpl();
        Skeleton<PingPongServer> skeleton = new Skeleton<PingPongServer>(PingPongServer.class, server, new InetSocketAddress("0.0.0.0", 7000));
        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }

    }
}
