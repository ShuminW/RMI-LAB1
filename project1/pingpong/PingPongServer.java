package pingpong;
import rmi.*;
public class PingPongServer {

    String ping(int idNumber) throws RMIException {
        return "Pong" + idNumber;
    }
}
