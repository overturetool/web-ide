package core.auth;

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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthentication {
    public static final String clientId = "915544938368-etbmhsu4bk7illn6eriesf60v6q059kh.apps.googleusercontent.com";
    public static final String tokenInfoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo";
    public static final String verificationUrl = "accounts.google.com";

    public ObjectNode extractUserNode(GoogleIdToken.Payload payload) {
        String userId = payload.getSubject();
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
        return node;
    }

    public GoogleIdToken verify(String tokenId) throws GeneralSecurityException, IOException {
        JsonFactory jsonFactory = new JacksonFactory();
        HttpTransport transport = new NetHttpTransport();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(GoogleAuthentication.clientId))
                .setIssuer(GoogleAuthentication.verificationUrl)
                .build();

        return verifier.verify(tokenId);
    }

    public JsonNode getTokenInfo(String accessToken) {
        GenericUrl url = new GenericUrl(GoogleAuthentication.tokenInfoUrl);

        Credential credential = new Credential(BearerToken.queryParameterAccessMethod());
        credential.setAccessToken(accessToken);
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential::initialize);

        try {
            HttpRequest request = requestFactory.buildGetRequest(url).setConnectTimeout(5000);
            HttpResponse response = request.execute();
            return new ObjectMapper().readTree(response.parseAsString());
        } catch (IOException e) {
            return null;
        }
    }
}
