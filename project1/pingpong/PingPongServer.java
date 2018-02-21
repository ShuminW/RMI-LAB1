package pingpong;
import rmi.*;
public interface PingPongServer {

    String ping(int idNumber) throws RMIException;

}
