package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import core.StatusCode;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class auth extends Controller {
    private static final HttpTransport httpTransport = new NetHttpTransport();

    private static final String authorizationServerUrl = "https://github.com/login/oauth/authorize";
    private static final String tokenServerUrl = "https://github.com/login/oauth/access_token";
    private static final String clientId = "915544938368-etbmhsu4bk7illn6eriesf60v6q059kh.apps.googleusercontent.com";
    private static final String clientSecret = "3d45a6b5666c0f16d4fd04f3a2f03c9705f1da4f";

    public Result verify(String tokenId) {
        String accessToken = request().getHeader("Authorization");
        String tokenPrefix = "Bearer ";

        if (accessToken.startsWith(tokenPrefix) && accessToken.length() > tokenPrefix.length())
            accessToken = accessToken.substring(tokenPrefix.length());

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
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (IllegalArgumentException e) {
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        if (idToken != null) {
            JsonNode credential = makeCall(accessToken);

            GoogleIdToken.Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            if (credential == null || !credential.get("sub").asText().equals(userId))
                return status(StatusCode.UnprocessableEntity);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            ObjectNode node = Json.newObject();
            node.put("userId", userId);
            node.put("givenName", givenName);
            node.put("familyName", familyName);
            node.put("name", name);
            node.put("email", email);
            node.put("emailVerified", emailVerified);
            node.put("locale", locale);
            node.put("pictureUrl", pictureUrl);

            session(accessToken, name);

            return ok(node);
        } else {
            return status(StatusCode.UnprocessableEntity, "Invalid ID token.");
        }
    }

    public Result login() {
        String callbackUrl = controllers.routes.auth.callback().absoluteURL(request());
        String url = new BrowserClientRequestUrl(authorizationServerUrl, clientId).setRedirectUri(callbackUrl).build();
        return redirect(url);
    }

    public Result callback() {
        String code = request().getQueryString("code");

        JsonFactory jsonFactory = new JacksonFactory();
        HttpTransport httpTransport = new NetHttpTransport();

        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                jsonFactory,
                new GenericUrl(tokenServerUrl),
                new ClientParametersAuthentication(clientId, clientSecret),
                clientId,
                authorizationServerUrl).build();

        TokenResponse tokenResponse;
        try {
            tokenResponse = flow
                    .newTokenRequest(code)
                    .setScopes(Collections.singletonList("user:email"))
                    .setRequestInitializer(request -> request.getHeaders().setAccept("application/json")).execute();
        } catch (IOException e) {
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        ObjectNode node = Json.newObject();
        node.put("accessToken", tokenResponse.getAccessToken());
        node.put("ExpiresInSeconds", tokenResponse.getExpiresInSeconds());
        node.put("RefreshToken", tokenResponse.getRefreshToken());
        node.put("Scope", tokenResponse.getScope());
        node.put("TokenType", tokenResponse.getTokenType());

        //String state = request().getQueryString("state");

        return ok(node);
    }

//    public Result user() {
//        String accessToken = request().getQueryString("accessToken");
//        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/tokeninfo");
//
//        Credential credential = new Credential(BearerToken.queryParameterAccessMethod());
//        credential.setAccessToken(accessToken);
//        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential::initialize);
//
//        String content;
//        try {
//            HttpRequest request = requestFactory.buildGetRequest(url);
//            HttpResponse response = request.execute();
//            content = response.parseAsString();
//        } catch (IOException e) {
//            return status(StatusCode.UnprocessableEntity, e.toString());
//        }
//
//        return ok(content);
//    }

    private JsonNode makeCall(String accessToken) {
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/tokeninfo");

        Credential credential = new Credential(BearerToken.queryParameterAccessMethod());
        credential.setAccessToken(accessToken);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential::initialize);

        try {
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();
            return Json.parse(response.parseAsString());
        } catch (IOException e) {
            return null;
        }
    }
}
