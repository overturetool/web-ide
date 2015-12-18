package controllers;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.FileLoader;
import views.html.index;

public class Application extends Controller {

    public Result index() {
        FileLoader fileLoader = new FileLoader("workspace");
        String result = fileLoader.Load("bom.vdmsl");
        return ok(index.render(result));
    }

    public WebSocket<String> hello() {
        System.out.println("Here!!!");
        return new WebSocket<String>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

                // For each event received on the socket,
                in.onMessage(new Callback<String>() {
                    public void invoke(String event) {

                        // Log events to the console
                        System.out.println(event);

                    }
                });

                // When the socket is closed.
                in.onClose(new Callback0() {
                    public void invoke() {

                        System.out.println("Disconnected!");

                    }
                });

                // Send a single 'Hello!' message
                out.write("Hello!");
            }
        };
    }
}
