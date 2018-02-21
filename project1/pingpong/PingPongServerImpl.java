package pingpong;

import rmi.RMIException;

public class PingPongServerImpl implements PingPongServer {
    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong" + idNumber;
    }
}
