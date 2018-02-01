package rmi;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer<T> {
    public static final int PORT = 12345;
    Class<T> c;
    T server;
    InetSocketAddress address;
    boolean stop = false;

    public TCPServer(Class<T> c, T server, InetSocketAddress address) {
        this. c = c;
        this.server = server;
        this.address = address;
        init();

    }

    public void init() {
        try {

            ServerSocket serverSocket = new ServerSocket();
            if(serverSocket != null) {
                serverSocket.bind(address);
            }
            else {
                serverSocket.bind(new InetSocketAddress(PORT));
            }

            while (!stop) {
                Socket client = serverSocket.accept();
                new HandlerThread(client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private class HandlerThread implements Runnable {
        private Socket socket;
        ObjectOutputStream output;
        ObjectInputStream input;
        public HandlerThread(Socket client) {
            socket = client;
            new Thread(this).start();
        }

        public void run() {
            try {
                output = new ObjectOutputStream(socket.getOutputStream());

                output.flush();

                input = new ObjectInputStream (socket.getInputStream());

                Method method = (Method)input.readObject();

                Object[] args = (Object[])input.readObject();

                Object object = method.invoke(server, args);

                output.writeObject(object);



            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        socket = null;
                        System.out.println(e.getMessage());
                    }
                }
                if(input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}  