package org.overture.webide.processor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiRuntimeServer extends Remote {
    String getMessage(String content) throws RemoteException;
}
