package core.processing;

import org.overture.webide.processing.Result;
import org.overture.webide.processing.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

public class TypeCheckClient extends AbstractClient<ObjectInputStream, ObjectOutputStream> {
    public TypeCheckClient(ServerSocket serverSocket, long timeout) {
        super(serverSocket, timeout);
    }

    @Override
    protected void init() throws IOException {
        this.in = new ObjectInputStream(this.socket.getInputStream());
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
    }

    public synchronized Result process(Task task) {
        try {
            this.out.writeObject(task);
            this.out.flush();
            return (Result) this.in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            return null;
        }
    }
}
