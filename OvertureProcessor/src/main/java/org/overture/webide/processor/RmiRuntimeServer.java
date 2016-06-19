package org.overture.webide.processor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiRuntimeServer implements IRmiRuntimeServer {

    public RmiRuntimeServer() throws RemoteException {
        super();
    }

    public String getMessage(String content) throws RemoteException {
        return content + " 1";
    }

    public static void main(String args[]) {
        //System.setProperty("java.rmi.server.hostname", "192.168.99.100");
        //System.setProperty("java.security.policy", "java.security.AllPermission");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try{
            String name = "RmiRuntimeServer";
            RmiRuntimeServer runtimeServer = new RmiRuntimeServer();
            RmiRuntimeServer stub = (RmiRuntimeServer) UnicastRemoteObject.exportObject(runtimeServer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            System.out.println("RmiRuntimeServer bound");
        }
        catch(Exception e){
            System.out.println("RmiRuntimeServer exception:");
            e.printStackTrace();
        }
    }

//    public static void main(String args[]) throws Exception {
////        if (System.getSecurityManager() == null)
////            System.setSecurityManager(new RMISecurityManager());
//
//        System.out.println("RMI server started");
//
//        try {
//            //special exception handler for registry creation
//            LocateRegistry.createRegistry(1099);
//            System.out.println("java RMI registry created.");
//        } catch (RemoteException e) {
//            //do nothing, error means registry already exists
//            System.out.println("java RMI registry already exists.");
//        }
//
//        //Instantiate RmiServer
//
//        RmiRuntimeServer obj = new RmiRuntimeServer();
//
//        // Bind this object instance to the name "RmiServer"
//        Naming.rebind("//localhost/RmiRuntimeServer", obj);
//        System.out.println("PeerServer bound in registry");
//    }
}
