package core.processing;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProcessQueue {
    private LinkedList<TypeCheckClient> busyProcesses = new LinkedList<>();
    private ArrayBlockingQueue<TypeCheckClient> availableProcesses;
    private long timeout;

    // Locks for making the associated methods atomic, but not mutual exclusive
    private static final Object acquireLock = new Object();
    private static final Object releaseLock = new Object();
    private static final Object sizeLock = new Object();

    public ProcessQueue(int capacity, boolean fair, long timeout) {
        this.availableProcesses = new ArrayBlockingQueue<>(capacity, fair);
        this.timeout = timeout;
    }

    public TypeCheckClient acquire() {
        synchronized (acquireLock) {
            TypeCheckClient processClient;

            try {
                processClient = this.availableProcesses.poll(this.timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return null;
            }

            if (processClient != null && processClient.isProcessAlive()) {
                this.busyProcesses.add(processClient);
                return processClient;
            } else {
                return null;
            }
        }
    }

    public void release(TypeCheckClient processClient) {
        synchronized (releaseLock) {
            try {
                this.availableProcesses.add(processClient);
            } catch (IllegalStateException e) {
                processClient.close();
            } catch (NullPointerException e) { /* ignored */ }
            this.busyProcesses.remove(processClient);
        }
    }

    public int size() {
        synchronized (sizeLock) {
            return this.busyProcesses.size() + this.availableProcesses.size();
        }
    }

    public void addBusyProcess(TypeCheckClient processClient) {
        this.busyProcesses.add(processClient);
    }
}
