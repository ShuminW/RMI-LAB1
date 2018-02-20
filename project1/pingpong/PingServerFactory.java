package pingpong;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

import java.net.UnknownHostException;

public class PingServerFactory {

    Skeleton<PingPongServer> skeleton;

    public PingServerFactory(Skeleton<PingPongServer> skeleton) {
        this.skeleton = skeleton;
    }

    PingPongServer makePingServer() throws UnknownHostException {

        return Stub.create(PingPongServer.class, skeleton);

    }
}
