package core.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiRuntimeClient {

    public RmiRuntimeClient() throws Exception {
        System.out.println("Client started!");
        System.setProperty("java.security.policy", "java.security.AllPermission");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String name = "RmiRuntimeServer";
        Registry registry = LocateRegistry.getRegistry("localhost");
        IRmiRuntimeServer obj = (IRmiRuntimeServer) registry.lookup(name);

        System.out.println(obj.getMessage("hello"));
    }

//    public static void main(String args[]) throws Exception {
//        System.out.println("Client started!");
//        IRmiRuntimeServer obj = (IRmiRuntimeServer) Naming.lookup("rmi://localhost/RmiRuntimeServer");
//        System.out.println(obj.getMessage("hello"));
//    }

//    public void getMessage() throws Exception {
//        IRmiRuntimeServer obj = (IRmiRuntimeServer) Naming.lookup("rmi://localhost/RmiRuntimeServer");
//        System.out.println(obj.getMessage("hello"));
//    }
}
