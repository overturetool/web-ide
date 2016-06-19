package core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiRuntimeServer extends Remote {
    String getMessage(String content) throws RemoteException;
}
