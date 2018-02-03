package rmi;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class TCPServer<T>{
    public static final int PORT = 12345;
    Class<T> c;
    T server;
    InetSocketAddress address = null;
    ServerSocket serverSocket = null;
    Skeleton skeleton = null;
    ArrayList<OPThread> threads = new ArrayList<>();
    boolean started = false;

    HandlerThread handlerThread  = null;

    public TCPServer(Class<T> c, T server, InetSocketAddress address, Skeleton skeleton) {
        this.c = c;
        this.server = server;
        this.address = address;
        this.skeleton = skeleton;

    }

    public void stopThread() {
        if(handlerThread != null) {
            handlerThread.stopThread();
            try {
                handlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void start() {

        try {
            serverSocket = new ServerSocket();
            if(address != null) {
                serverSocket.bind(address);
                skeleton.address = address;
            }
            else {
                serverSocket.bind(new InetSocketAddress(PORT));
                skeleton.address = new InetSocketAddress(serverSocket.getInetAddress(),
                        serverSocket.getLocalPort());

            }
            handlerThread = new HandlerThread(serverSocket);
            handlerThread.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private class OPThread extends Thread {

        private Socket socket = null;

        public OPThread(Socket socket) {
            this.socket = socket;

        }
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {

                output = new ObjectOutputStream(socket.getOutputStream());

                output.flush();

                input = new ObjectInputStream(socket.getInputStream());

                Method method = (Method)input.readObject();

                Object[] args = (Object[])input.readObject();


                Object object = method.invoke(server, args);

                output.writeObject(object);

            }
            catch (Exception e) {
                e.printStackTrace();

            }
            finally {
                if(socket!=null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private class HandlerThread extends Thread {

        private ServerSocket serverSocket = null;

        private volatile boolean stop = false;

        public HandlerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void stopThread() {
            stop = true;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    serverSocket = null;
                    e.printStackTrace();
                }
            }

        }

        public void run() {
            try {
                while(!stop) {
                    Socket socket = serverSocket.accept();

                    OPThread opThread = new OPThread(socket);

                    threads.add(opThread);

                    opThread.start();
                }

                try {
                    for(OPThread t: threads) {
                        t.join();
                    }

                }
                catch(InterruptedException ep) {
                    ep.printStackTrace();

                }

            } catch(SocketException e) {
                try {
                    if(serverSocket.isClosed()){
                        for(OPThread t: threads) {
                            t.join();
                        }
                    }

                }
                catch(InterruptedException ep) {
                    ep.printStackTrace();

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}