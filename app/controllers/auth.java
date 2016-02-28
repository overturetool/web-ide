package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import core.StatusCode;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;
import java.util.Collections;

public class auth extends Application {
    private static final HttpTransport httpTransport = new NetHttpTransport();

    private static final String authorizationServerUrl = "https://github.com/login/oauth/authorize";
    private static final String tokenServerUrl = "https://github.com/login/oauth/access_token";
    private static final String clientId = "e8c97a27f3858b4370ef";
    private static final String clientSecret = "3d45a6b5666c0f16d4fd04f3a2f03c9705f1da4f";
    private static final String callbackUrl = "http://localhost:9000/callback";

    public Result login() {
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

    public Result user() {
        String accessToken = request().getQueryString("accessToken");
        GenericUrl url = new GenericUrl("https://api.github.com/user");

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
        credential.setAccessToken(accessToken);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential::initialize);

        String content;
        try {
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();
            content = response.parseAsString();
        } catch (IOException e) {
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        return ok(content);
    }
}
