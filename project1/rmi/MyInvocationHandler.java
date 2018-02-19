package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;
import java.io.*;

public class MyInvocationHandler implements InvocationHandler, Serializable {
	private Class<?> remoteClass;
  	private InetSocketAddress address;

  	public MyInvocationHandler(Class<?> remoteClass, InetSocketAddress address) {
        this.remoteClass = remoteClass;
        this.address = address;
    }
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    	// equals if they implement the same remote interface and connect to the same skeleton.
    	if (m.equals(Object.class.getMethod("equals", Object.class))) {
        	Object o = args[0];

        	if (o == null || !Proxy.isProxyClass(o.getClass()))
        		return false;
            InvocationHandler handler = Proxy.getInvocationHandler(o);
            if (!(handler instanceof MyInvocationHandler)) 
                return false;
            MyInvocationHandler myHandler = (MyInvocationHandler)handler;
            if (!this.remoteClass.equals(myHandler.remoteClass))
            	return false;
            InetSocketAddress addr = myHandler.address;
            if (addr == null){
            	if (this.address != null)
            		return false;
            } else {
            	if (this.address == null)
            		return false;
            	if (!addr.getAddress().equals(this.address.getAddress()) || addr.getPort() != this.address.getPort())
            		return false;
        	}
            return true;
        } 
        // hashcode
        if (m.equals(Object.class.getMethod("hashCode")))
        	return this.address.hashCode() * 31 + this.remoteClass.hashCode() * 17;
        // toString
        if (m.equals(Object.class.getMethod("toString"))) {
        	return "interface name: " + this.remoteClass.getName() + " and remote address: " + this.address.toString();
        }
        
        Object response = null;
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            socket = new Socket(this.address.getHostName(), this.address.getPort());
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(m.getName());
            oos.writeObject(m.getParameterTypes());
            oos.writeObject(args);
            oos.flush();
            
            response = ois.readObject();

        } catch (Exception e) {
            throw new RMIException("connection error");
        } finally {
            try {
                oos.close();
                ois.close();
                socket.close();
            } catch (Exception e) {
            	throw new RMIException("close error");
            }
        }

        if (response instanceof Exception) {
            Throwable e = (Throwable)response;
            throw e.getCause();
        } else
            return response;
    }
}