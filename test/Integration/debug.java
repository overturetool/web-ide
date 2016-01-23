package Integration;

import Integration.Helpers.String2XML;
import Integration.Helpers.WebSocketTestClientSync;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class debug {
    // Debug protocol: http://xdebug.org/docs-dbgp.php
    @Test
    public void ShouldReceiveCorrectSequenceOfXMLResponses() throws Exception {
        URI uri = new URI("ws://localhost:9000/debug/test/test_ws/bom.vdmsl?entry=UGFydHMoMSwgYm9tKQ==");

        List<String> list = new ArrayList<>();

        final WebSocketTestClientSync client = new WebSocketTestClientSync(uri);
        client.addMessageHandler(new WebSocketTestClientSync.MessageHandler() {
            @Override
            public void handleMessage(String message) {
                int subStringFrom = message.trim().indexOf("<");
                message = message.trim().substring(subStringFrom);
                list.add(message);
                System.out.println(message);
            }
        });

        // Send commands to debug protocol
        int pendingCalls = 6;
        client.sendMessage("status -i 0");
        client.sendMessage("breakpoint_set -i 1 -t line -f test/test_ws/bom.vdmsl -n 23");
        client.sendMessage("breakpoint_list -i 2");
        client.sendMessage("run -i 3");
        client.sendMessage("context_names -d 1 -i 4");

        // TODO : Find some more appropriate way to await ws responses
        while (list.size() < pendingCalls) {
            // busy wait
        }

        // Close client websocket
        //client.close();

        List<Document> documents = String2XML.Convert(list);

        Element init = documents.get(0).getDocumentElement();
        Element status = documents.get(1).getDocumentElement();
        Element internal0 = (Element) status.getFirstChild();
        Element breakpointSet = documents.get(2).getDocumentElement();
        Element breakpointList = documents.get(3).getDocumentElement();
        Element breakpoint = (Element) breakpointList.getFirstChild();
        Element run = documents.get(4).getDocumentElement();
        Element internal1 = (Element) run.getFirstChild();

        //System.out.println(internal.getAttributes().getLength());
        //System.out.println(breakpoint.getAttribute("type"));

        // Assertions
        assertThat(list.size(), is(pendingCalls));

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

        assertThat(run.getAttribute("transaction_id"), is("3"));
        assertThat(run.getAttribute("status"), is("break"));
        assertThat(run.getAttribute("reason"), is("ok"));
        assertThat(internal1.getAttribute("threadState"), is("RUNNING"));
    }

    //@Test
    public void FileNotFound() throws URISyntaxException, IOException {
        URI uri = new URI("ws://localhost:9000/debug/test/test_ws/bom1.vdmsl?entry=UGFydHMoMSwgYm9tKQ==");

        List<String> list = new ArrayList<>();

        final WebSocketTestClientSync client = new WebSocketTestClientSync(uri);
        client.addMessageHandler(new WebSocketTestClientSync.MessageHandler() {
            @Override
            public void handleMessage(String message) {
                list.add(message);
                System.out.println(message);
            }
        });

        client.sendMessage("status -i 0");

        // TODO : Find some more appropriate way to await ws responses
        while (list.size() < 1) {
            // busy wait
        }

        // Close client websocket
        client.close();

        assertThat(list.get(0), is("file not found"));
    }
}
