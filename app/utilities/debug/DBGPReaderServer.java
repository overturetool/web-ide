package utilities.debug;

import org.overture.interpreter.debug.DBGPReaderV2;

public class DBGPReaderServer extends Thread {
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
}
