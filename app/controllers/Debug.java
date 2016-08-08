package controllers;

import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import core.debug.DebugClient;
import core.debug.DebugFilters;
import core.debug.DebugManager;
import core.vfs.IVFS;
import core.vfs.commons.vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.vfs2.FileObject;
import play.mvc.WebSocket;

import java.util.Base64;

public class Debug extends Application {
    public WebSocket ws(String account, String path) {
        String type = request().getQueryString("type");
        String entry = request().getQueryString("entry");
        String coverageString = request().getQueryString("coverage");
        boolean coverage = Boolean.parseBoolean(coverageString);
        String defaultName = null;
        DebugManager manager;

        if (entry == null)
            return errorResponse("Missing entry");

        entry = StringUtils.newStringUtf8(Base64.getDecoder().decode(entry));
        int containsDefault = entry.indexOf("`");
        if (containsDefault > 0) {
            defaultName = entry.substring(0, containsDefault);
            defaultName = Base64.getEncoder().encodeToString(defaultName.getBytes());
        }

        IVFS<FileObject> file = new CommonsVFS(account, path);

        if (!file.exists())
            return errorResponse("file not found");

        if (type == null) {
            ModelWrapper modelWrapper = new ModelWrapper(file);
            type = modelWrapper.getDialect().toString().replace("_", "").toLowerCase();
        }

        if (file.isDirectory())
            manager = new DebugManager(entry, type, defaultName, file, coverage);
        else
            manager = new DebugManager(entry, defaultName, file, coverage);

        DebugClient client = manager.start();

        if (client == null)
            return errorResponse("Error occurred while initiating connection");

        String initialResponse = client.read();

        if (initialResponse == null) {
            client.close();
            return errorResponse("Initial read failed");
        }

        return WebSocket.Text.accept(request -> {
            String initialResponseFiltered = DebugFilters.ConvertPathsToRelative(initialResponse.replace("\u0000", ""));
            Source.single(initialResponseFiltered);

            return Flow.<String>create().map(msg -> {
                String filteredRequest = DebugFilters.ConvertPathToAbsolute(msg);
                String response = client.writeRead(filteredRequest);
                response = response != null ? response.replace("\u0000", "") : "";
                return DebugFilters.ConvertPathsToRelative(response);
            });
        });
    }

    private WebSocket errorResponse(String message) {
        return WebSocket.Text.accept(request -> Flow.fromSinkAndSource(Sink.last(), Source.single(message)));
    }
}
