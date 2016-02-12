package WebSockets;

import core.utilities.String2XML;
import Helpers.WebSocketTestClientSync;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class debug {
    // Debug protocol: http://xdebug.org/docs-dbgp.php
    @Test
    public void ShouldReceiveCorrectSequenceOfXMLResponses() throws Exception {
        final Object lock = new Object();
        URI uri = new URI("ws://localhost:9000/debug/test/test_ws/bom.vdmsl?entry=UGFydHMoMSwgYm9tKQ==");

        List<String> list = new ArrayList<>();

        final WebSocketTestClientSync client = new WebSocketTestClientSync(uri);
        client.addMessageHandler(new WebSocketTestClientSync.MessageHandler() {
            @Override
            public void handleMessage(String message) {
                int subStringFrom = message.trim().indexOf("<");
                message = message.trim().substring(subStringFrom);
                list.add(message);
                //System.out.println(message);
                synchronized (lock) { lock.notify(); }
            }
        });

        List<String> commands = new ArrayList<>();
        commands.add("status -i 0");
        commands.add("breakpoint_set -i 1 -t line -f test/test_ws/bom.vdmsl -n 23");
        commands.add("breakpoint_list -i 2");
        commands.add("run -i 3");
        commands.add("run -i 4");

        // Send commands to debug protocol
        synchronized (lock) { lock.wait(5000); }
        for (String command : commands) {
            client.sendMessage(command);
            synchronized (lock) { lock.wait(5000); }
        }

        List<Document> documents = String2XML.Convert(list);

        Element init = documents.get(0).getDocumentElement();
        Element status = documents.get(1).getDocumentElement();
        Element internal0 = (Element) status.getFirstChild();
        Element breakpointSet = documents.get(2).getDocumentElement();
        Element breakpointList = documents.get(3).getDocumentElement();
        Element breakpoint = (Element) breakpointList.getFirstChild();
        Element run0 = documents.get(4).getDocumentElement();
        Element internal1 = (Element) run0.getFirstChild();
        Element run1 = documents.get(5).getDocumentElement();
        Node cdata = run1.getFirstChild();
        String entryDecoded = StringUtils.newStringUtf8(Base64.getDecoder().decode(cdata.getNodeValue())).replaceAll("(\n)", "");

        //System.out.println(entryDecoded);

        // Assertions
        assertThat(list.size(), is(commands.size() + 1)); // +1 is for initial response

        assertThat(init.getAttribute("appid"), is("VDM_SL"));
        assertThat(init.getAttribute("idekey"), is("webIDE"));

        assertThat(status.getAttribute("transaction_id"), is("0"));
        assertThat(status.getAttribute("command"), is("status"));
        assertThat(status.getAttribute("status"), is("starting"));
        assertThat(status.getAttribute("reason"), is("ok"));
        assertThat(internal0.getAttribute("threadState"), is("RUNNING"));

        assertThat(breakpointSet.getAttribute("transaction_id"), is("1"));
        assertThat(breakpointSet.getAttribute("command"), is("breakpoint_set"));
        assertThat(breakpointSet.getAttribute("state"), is("enabled"));

        assertThat(breakpointList.getAttribute("transaction_id"), is("2"));
        assertThat(breakpointList.getAttribute("command"), is("breakpoint_list"));
        assertThat(breakpoint.getAttribute("id"), is("1"));
        assertThat(breakpoint.getAttribute("type"), is("line"));
        assertThat(breakpoint.getAttribute("state"), is("enabled"));
        assertThat(new File(breakpoint.getAttribute("filename").substring(5)).isAbsolute(), is(true));
        assertThat(breakpoint.getAttribute("lineno"), is("23"));

        assertThat(run0.getAttribute("transaction_id"), is("3"));
        assertThat(run0.getAttribute("status"), is("break"));
        assertThat(run0.getAttribute("reason"), is("ok"));
        assertThat(internal1.getAttribute("threadState"), is("RUNNING"));

        assertThat(entryDecoded, is("Parts(1, bom) = {2, 3, 4, 5, 6}"));
    }

    @Test
    public void FileNotFound() throws URISyntaxException, IOException, InterruptedException {
        final Object lock = new Object();
        URI uri = new URI("ws://localhost:9000/debug/test/test_ws/bom1.vdmsl?entry=UGFydHMoMSwgYm9tKQ==");

        List<String> list = new ArrayList<>();

        final WebSocketTestClientSync client = new WebSocketTestClientSync(uri);
        client.addMessageHandler(new WebSocketTestClientSync.MessageHandler() {
            @Override
            public void handleMessage(String message) {
                list.add(message);
                //System.out.println(message);
                synchronized (lock) { lock.notify(); }
            }
        });

        synchronized (lock) { lock.wait(5000); }

        assertThat(list.size(), is(1));
        assertThat(list.get(0), is("file not found"));
    }

    @Test
    public void InterleavedSessions() throws URISyntaxException, InterruptedException, IOException {
//        final Object lock0 = new Object();
//        final Object lock1 = new Object();
        URI uri0 = new URI("ws://localhost:9000/debug/test/test_ws/bom.vdmsl?entry=UGFydHMoMSwgYm9tKQ==");
        URI uri1 = new URI("ws://localhost:9000/debug/kdsaaby/kds_ws/barSL/bag.vdmsl?entry=QkFHVEVTVGBUZXN0QmFnQWxsKCk=");

        List<String> list = new ArrayList<>();

        final WebSocketTestClientSync client0 = new WebSocketTestClientSync(uri0);
        client0.addMessageHandler(message -> {
            int subStringFrom = message.trim().indexOf("<");
            message = message.trim().substring(subStringFrom);
            list.add(message);
//            System.out.println("client0: " + message);
//            synchronized (lock0) { lock0.notify(); }
        });

        final WebSocketTestClientSync client1 = new WebSocketTestClientSync(uri1);
        client1.addMessageHandler(message -> {
            int subStringFrom = message.trim().indexOf("<");
            message = message.trim().substring(subStringFrom);
            list.add(message);
//            System.out.println("client1: " + message);
//            synchronized (lock1) { lock1.notify(); }
        });

        List<String> commandsForClient0 = new ArrayList<>();
        commandsForClient0.add("status -i 0");
        commandsForClient0.add("breakpoint_set -i 1 -t line -f test/test_ws/bom.vdmsl -n 23");
        commandsForClient0.add("breakpoint_list -i 2");
        commandsForClient0.add("run -i 3");
        commandsForClient0.add("run -i 4");

        List<String> commandsForClient1 = new ArrayList<>();
        commandsForClient1.add("status -i 10");
        commandsForClient1.add("status -i 11");
        commandsForClient1.add("status -i 12");
        commandsForClient1.add("status -i 13");
        commandsForClient1.add("status -i 14");

        int client0Size = commandsForClient0.size();
        int client1Size = commandsForClient1.size();

        int upperBound = Integer.max(client0Size, client1Size);

        // Send commands to debug protocol
//        synchronized (lock0) { lock0.wait(5000); }
//        synchronized (lock1) { lock1.wait(5000); }
        for (int i = 0; i < upperBound; i++) {
            if (i < commandsForClient0.size()) {
                client0.sendMessage(commandsForClient0.get(i));
//                synchronized (lock0) { lock0.wait(5000); }
            }

            if (i < commandsForClient1.size()) {
                client1.sendMessage(commandsForClient1.get(i));
//                synchronized (lock1) { lock1.wait(5000); }
            }
        }

//        synchronized (lock) { lock.wait(5000); }
//        synchronized (lock) { lock.wait(5000); }

        Thread.sleep(10000);

        assertThat(list.size(), is(client0Size + client1Size + 2));
    }
}
