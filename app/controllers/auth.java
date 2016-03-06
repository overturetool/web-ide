package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import core.StatusCode;
import core.auth.SessionStore;
import core.utilities.ServerUtils;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class auth extends Controller {
    private static final HttpTransport httpTransport = new NetHttpTransport();
    private static final String clientId = "915544938368-etbmhsu4bk7illn6eriesf60v6q059kh.apps.googleusercontent.com";

    public Result verify(String tokenId) {
        String accessToken = ServerUtils.extractAccessToken(request());

        if (accessToken == null)
            return unauthorized("Missing access token");

        JsonFactory jsonFactory = new JacksonFactory();
        HttpTransport transport = new NetHttpTransport();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .setIssuer("accounts.google.com")
                .build();

        // (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(tokenId);
        } catch (GeneralSecurityException | IOException e) {
            Logger.debug(e.getMessage());
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (IllegalArgumentException e) {
            Logger.debug(e.getMessage());
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        if (idToken != null) {
            JsonNode credential = getTokenInfo(accessToken);
            if (credential == null) {
                SessionStore.getInstance().remove(accessToken);
                return unauthorized("Token info not obtained");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            if (!credential.get("sub").asText().equals(userId))
                return unauthorized("Id mismatch");

            IVFS vfs = new CommonsVFS(userId, "");
            if (!vfs.exists())
                vfs.mkdir();

            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            ObjectNode node = new ObjectMapper().createObjectNode();
            node.put("userId", userId);
            node.put("givenName", givenName);
            node.put("familyName", familyName);
            node.put("name", name);
            node.put("email", email);
            node.put("emailVerified", emailVerified);
            node.put("locale", locale);
            node.put("pictureUrl", pictureUrl);

            SessionStore.getInstance().set(accessToken, userId);

            return ok(node);
        } else {
            return unauthorized("Invalid ID token.");
        }
    }

    public Result signout() {
        String accessToken = ServerUtils.extractAccessToken(request());

        if (accessToken == null)
            return status(StatusCode.UnprocessableEntity, "Missing access token");

        if (!SessionStore.getInstance().exists(accessToken))
            return status(StatusCode.UnprocessableEntity, "Access token not found in store");

        SessionStore.getInstance().remove(accessToken);

        return ok();
    }

    private JsonNode getTokenInfo(String accessToken) {
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/tokeninfo");

        Credential credential = new Credential(BearerToken.queryParameterAccessMethod());
        credential.setAccessToken(accessToken);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential::initialize);

        try {
            HttpRequest request = requestFactory.buildGetRequest(url).setConnectTimeout(5000);
            HttpResponse response = request.execute();
            return new ObjectMapper().readTree(response.parseAsString());
        } catch (IOException e) {
            return null;
        }
    }
}
