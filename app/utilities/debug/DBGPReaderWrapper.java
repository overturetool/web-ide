package utilities.debug;

import org.overture.interpreter.debug.DBGPReaderV2;
import org.overture.interpreter.runtime.Interpreter;
import org.overture.interpreter.values.CPUValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DBGPReaderWrapper extends DBGPReaderV2 {
    class FakeSocket extends Socket {
        public void close() {
            // Dummy method
        }
    }

    public DBGPReaderWrapper() {
        super(null, 0, null, null, null, null);
    }

    private DBGPReaderWrapper(String host, int port, String ideKey, Interpreter interpreter, String expression, CPUValue cpu) {
        super(host, port, ideKey, interpreter, expression, cpu);
    }

    public void initiateWrapper(InputStream inputStream, OutputStream outputStream) {
        this.socket = new FakeSocket();
        this.input = inputStream;
        this.output = outputStream;
    }

    @Override
    protected void connect() throws IOException {
        if(!this.connected) {
//            if(this.port > 0) {
//                InetAddress server = InetAddress.getByName(this.host);
//                this.socket = new Socket(server, this.port);
//                this.input = this.socket.getInputStream();
//                this.output = this.socket.getOutputStream();
//            } else {
//                this.socket = null;
//                this.input = System.in;
//                this.output = System.out;
//                this.separator = 32;
//            }

            this.connected = true;
            //this.addThisReader();
            this.init();
            this.run();
        }
    }
}
