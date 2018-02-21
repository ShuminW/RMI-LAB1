package pingpong;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

import java.net.UnknownHostException;

public class PingServerFactory implements PingPongServer{


    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong" + idNumber;
    }
}
