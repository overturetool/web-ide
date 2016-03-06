package core.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpUtils {
//    public synchronized static InputStream getContentAsInputStream(String inputUrl) {
//        URL url;
//        try {
//            url = new URL(inputUrl);
//        } catch (MalformedURLException e) {
//            return null;
//        }
//
//        URLConnection connection;
//        try {
//            connection = url.openConnection();
//            return connection.getInputStream();
////            try (InputStream inputStream = connection.getInputStream()) {
////                return inputStream;
////            }
//        } catch (IOException e) {
//            return null;
//        }
//    }

    public synchronized static HttpResponse getHttpResponse(String inputUrl) throws IOException {
//        HttpHeaders header = new HttpHeaders();
//        header.setIfModifiedSince("Thu, 01 Mar 2016 12:00:00 GMT");
        GenericUrl gUrl = new GenericUrl(inputUrl);
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        HttpRequest request = requestFactory.buildGetRequest(gUrl);//.setHeaders(header);
        return request.execute();
    }

    public synchronized static String getContentAsString(String inputUrl) {
        try {
            InputStream inputStream = getHttpResponse(inputUrl).getContent();
            if (inputStream != null)
                return IOUtils.toString(inputStream);
        } catch (IOException e) {
            e.getMessage();
        }
        return null;
    }

    public synchronized static JsonNode getContentAsJson(String inputUrl) {
        try {
            InputStream inputStream = getHttpResponse(inputUrl).getContent();
            if (inputStream != null)
                return new ObjectMapper().readTree(inputStream);
        } catch (IOException e) {
            e.getMessage();
        }
        return null;
    }

    public synchronized static Path downloadFile(String inputUrl, String destination) {
        String filename = Paths.get(inputUrl).getFileName().toString();

        URL url;
        try {
            url = new URL(inputUrl);
        } catch (MalformedURLException e) {
            return null;
        }

        Path path;
        URLConnection con;
        try {
            con = url.openConnection();
            path = Paths.get(destination + File.separator + filename);

            try (InputStream stream = con.getInputStream()) {
                Files.copy(stream, path);
            }

            return path;
        } catch (IOException e) {
            return null;
        }
    }
}
