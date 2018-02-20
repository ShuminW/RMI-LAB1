package rmi;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * Server for managing listening thread
 * @param <T> server type
 */
public class TCPServer<T>{
    public static final int PORT = 49152;
    Class<T> c = null;
    T server = null;
    InetSocketAddress address = null;
    ServerSocket serverSocket = null;
    Skeleton skeleton = null;

    HandlerThread handlerThread  = null;

    public TCPServer(Class<T> c, T server, InetSocketAddress address, Skeleton skeleton) {
        this.c = c;
        this.server = server;
        this.address = address;
        this.skeleton = skeleton;

    }


    /**
     * stop listening thread
     */
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


    /**
     * @return whether is listening thread is running
     */
    public boolean isAlive() {
        return handlerThread != null && handlerThread.isAlive();
    }

    /**
     * for bind address and start listening thread
     */
    public void start() throws rmi.RMIException {

        try {
            if(address != null) {
                serverSocket = new ServerSocket(address.getPort());
                skeleton.address = address;
            }
            else {
                serverSocket = new ServerSocket(PORT);
                skeleton.address = new InetSocketAddress(serverSocket.getInetAddress(),
                        serverSocket.getLocalPort());

            }
            handlerThread = new HandlerThread(serverSocket);
            handlerThread.start();


        } catch (IOException e) {
            throw new RMIException(e.getMessage());
        }
    }


    /**
     * Thread for operation service
     */
    private class OPThread extends Thread {

        private Socket socket = null;
        private Object ret = null;

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

                String name = (String)input.readObject();

                Class<?>[] type = (Class<?>[])input.readObject();

                Object[] args = (Object[])input.readObject();

                Method method = server.getClass().getMethod(name,type);

                ret = method.invoke(server, args);
            }
            catch (Exception e) {
                ret = e;
            }
            try {
                output.writeObject(ret);
            } catch (IOException e) {
                skeleton.service_error(new rmi.RMIException(e.getMessage()));
                e.printStackTrace();
            }
            finally {
                if(socket!=null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        skeleton.service_error(new rmi.RMIException(e.getMessage()));
                    }
                }
                if(input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        skeleton.service_error(new rmi.RMIException(e.getMessage()));
                    }
                }
                if(output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        skeleton.service_error(new rmi.RMIException(e.getMessage()));
                    }
                }
            }


        }
    }

    /**
     * listening handler thread
     */
    private class HandlerThread extends Thread {

        private ServerSocket serverSocket;

        private volatile boolean stop = false;

        ArrayList<OPThread> threads = new ArrayList<>();


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


            }
            catch(SocketException e) {
                for(OPThread t: threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            catch (Exception e) {
                skeleton.listen_error(e);
            }


        }
    }
}