package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class DBGPReaderConnector implements Runnable {
    private Thread t;
    private int port;

    private ServerSocket server;
    private Socket client;

    private BufferedReader in;
    private PrintWriter out;
    public boolean isConnected;

    public DBGPReaderConnector(int port) {
        this.port = port;
    }

    public void connect() {
        String line;

        try {
            server = new ServerSocket(port);
//            server.setReuseAddress(false);
//            server.bind(new InetSocketAddress(port));
        }
        catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            System.exit(-1);
        }

        try {
            System.out.println("Ready to connect");
            client = server.accept();
            isConnected = true;
            System.out.println("Connection accepted!");
        }
        catch (IOException e) {
            System.out.println("Accept failed on port " + port);
            System.exit(-1);
        }

        // Initiating I/O to Overture Debugger
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        }
        catch (IOException e) {
            System.out.println("Read failed");
            System.exit(-1);
        }

//        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//        String s;
//        int size = 0;
//
//        System.out.println("Ready to receive input");
//        while (true) {
//            try {
//                line = in.readLine();
//                size = extractSize(line);
//                int numOfDigits = Integer.toString(size).length();
//
//                if (numOfDigits < line.length())
//                    line = line.trim().substring(numOfDigits);
//
//                System.out.print("in>");
//                System.out.print(size);
//                System.out.println(line);
//
//                // Receive console input
//                // TODO : Will be replaced with WebSocket input
//                s = br.readLine();
//                // TODO : Will be replaced with Web Socket output
//                System.out.println("out>" + s);
//
//                if (s.equals("exit")) {
//                    br.close();
//                    in.close();
//                    out.close();
//                    System.exit(-1);
//                }
//
//                out.println(s);
//                out.flush();
//            }
//            catch (IOException e) {
//                System.out.println("Read failed");
//                System.exit(-1);
//            }
//        }
    }

    public String initialRead() {
        String line = "";

        while (Objects.equals(line, "")) {
            try {
                if (in != null)
                    line = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return line;
    }

    public void disconnect() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String send(String event) {
        String line = "";

        out.println(event);
        out.flush();

        try {
            line = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    @Override
    public void run() {
        connect();
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, this.getClass().toString());
            t.start ();
        }
    }

//    private void process(String s) {
//        if (s == null || s.isEmpty())
//            return;
//
//        Document doc = convertStringToDocument(s.trim());
//
//        if (doc == null)
//            return;
//
//        NodeList tag = doc.getDocumentElement().getElementsByTagName("property");
//
//        if (tag.getLength() == 0)
//            return;
//
//        List<String> data = new ArrayList<String>();
//
//        for (int i = 0; i < tag.getLength(); i++) {
//            Node firstChild = tag.item(i).getFirstChild();
//            try {
//                byte[] bytes = Base64.decode(firstChild.getNodeValue());
//                data.add(new String(bytes));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    private int extractSize(String s) {
//        String prefix = "";
//
//        if (s == null || s.isEmpty())
//            return 0;
//
//        for (char c : s.toCharArray()) {
//            if (Character.isDigit(c)) {
//                prefix += c;
//            }
//            else if (c == '<') {
//                break;
//            }
//        }
//
//        return Integer.parseInt(prefix);
//    }

//    private Document convertStringToDocument(String xmlStr) {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder;
//        try {
//            builder = factory.newDocumentBuilder();
//            return builder.parse(new InputSource(new StringReader(xmlStr)));
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
