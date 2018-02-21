package pingpong;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class PingServerFactory{
    PingPongServer makePingServer() {
        return  Stub.create(PingPongServer.class, new InetSocketAddress("128.2.13.138", 7000));
    }

}
