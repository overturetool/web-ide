package core.runtime;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RuntimeProcessQueue {
    private LinkedList<RuntimeSocketClient> busyProcesses = new LinkedList<>();
    private ArrayBlockingQueue<RuntimeSocketClient> availableProcesses;
    private long timeout;

    // Locks for making the associated methods atomic, but not mutual exclusive
    private static final Object acquireLock = new Object();
    private static final Object releaseLock = new Object();
    private static final Object sizeLock = new Object();

    public RuntimeProcessQueue(int capacity, boolean fair, long timeout) {
        this.availableProcesses = new ArrayBlockingQueue<>(capacity, fair);
        this.timeout = timeout;
    }

    public RuntimeSocketClient acquire() {
        synchronized (acquireLock) {
            RuntimeSocketClient runtimeSocketClient;

            try {
                runtimeSocketClient = this.availableProcesses.poll(this.timeout, TimeUnit.MILLISECONDS);
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
