package pingpong;

import rmi.Stub;

import java.net.InetSocketAddress;

public class PingPongClient {
    public static void main(String[] args) {
        PingPongServer stub = Stub.create(PingPongServer.class, new InetSocketAddress("128.2.13.138", 7000));
        try {
            for(int i = 0; i < 4; i++) {
                System.out.println(stub.ping(i));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }
}
