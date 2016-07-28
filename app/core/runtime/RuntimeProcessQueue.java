package core.runtime;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RuntimeProcessQueue {
    private ArrayBlockingQueue<RuntimeSocketClient> availableProcesses;
    private LinkedList<RuntimeSocketClient> busyProcesses = new LinkedList<>();

    // Locks for making the associated methods atomic, but not mutual exclusive
    private static final Object acquireLock = new Object();
    private static final Object releaseLock = new Object();
    private static final Object sizeLock = new Object();

    public RuntimeProcessQueue(int capacity, boolean fair) {
        this.availableProcesses = new ArrayBlockingQueue<>(capacity, fair);
    }

    public RuntimeSocketClient acquire() {
        synchronized (acquireLock) {
            RuntimeSocketClient runtimeSocketClient;

            try {
                runtimeSocketClient = this.availableProcesses.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return null;
            }

            if (runtimeSocketClient != null && runtimeSocketClient.isProcessAlive()) {
                this.busyProcesses.add(runtimeSocketClient);
                return runtimeSocketClient;
            } else {
                return null;
            }
        }
    }

    public void release(RuntimeSocketClient runtimeClient) {
        synchronized (releaseLock) {
            try {
                this.availableProcesses.add(runtimeClient);
            } catch (IllegalStateException e) {
                runtimeClient.close();
            } catch (NullPointerException e) { /* ignored */ }
            this.busyProcesses.remove(runtimeClient);
        }
    }

    public int size() {
        synchronized (sizeLock) {
            return this.busyProcesses.size() + this.availableProcesses.size();
        }
    }

    public void addBusyProcess(RuntimeSocketClient runtimeClient) {
        this.busyProcesses.add(runtimeClient);
    }
}
