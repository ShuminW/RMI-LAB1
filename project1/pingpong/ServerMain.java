package pingpong;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetSocketAddress;

public class ServerMain {
    public static void main(String[] args) {
        PingPongServer server = new PingPongServer();
        Skeleton<PingPongServer> skeleton = new Skeleton<PingPongServer>(PingPongServer.class,server);
        try {
            skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }

        PingServerFactory factory = new PingServerFactory(skeleton);
        Skeleton<PingServerFactory> skeleton1 = new Skeleton<PingServerFactory>(PingServerFactory.class, factory, new InetSocketAddress("localhost", 7000));
        try {
            skeleton1.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }

    }
}
