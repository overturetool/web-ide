package utilities;

import org.overture.interpreter.debug.DBGPReaderV2;

public class DBGPReaderServer implements Runnable {
    private Thread t;
    private String[] args;

    public DBGPReaderServer(String type, String host, int port, String key, String entry, String file) {
        this.args = new String[]{
            type,
            "-h", host,
            "-p", Integer.toString(port),
            "-k", key,
            "-e", entry,
            file
        };
    }

    @Override
    public void run() {
        DBGPReaderV2.main(args);
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, this.getClass().toString());
            t.start();
        }
    }
}
