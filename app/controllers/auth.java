package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StatusCode;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

public class auth extends Application {
    public static final String githubClientId =  "e8c97a27f3858b4370ef";
    public static final String githubClientSecret = "3d45a6b5666c0f16d4fd04f3a2f03c9705f1da4f";
    public static final String githubBaseUrl = "https://api.github.com";

    public static final String facebookClientId =  "1028520697189440";
    public static final String facebookClientSecret = "c990b0559c1dc7a21c814ce6d968e9f2";
    public static final String facebookBaseUrl = "https://graph.facebook.com";

    public static final String clientId =  facebookClientId;
    public static final String clientSecret = facebookClientSecret;
    public static final String baseUrl = facebookBaseUrl;
    public static final OAuthProviderType providerType = OAuthProviderType.FACEBOOK;

    public static final String callbackUrl =  "http://localhost:9000/callback";

    public Result login() {
        OAuthClientRequest request;

        try {
            request = OAuthClientRequest
                    .authorizationProvider(providerType)
                    .setClientId(clientId)
                    .setRedirectURI(callbackUrl)
//                    .setScope("user,user:email")
//                    .setParameter("token_type", "bearer")
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            return status(StatusCode.UnprocessableEntity);
        }

        return redirect(request.getLocationUri());
    }

    public Result callback() {
        String code = request().getQueryString("code");
        OAuthClientRequest request;

        try {
            request = OAuthClientRequest
                    .tokenProvider(providerType)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectURI(callbackUrl)
                    .setCode(code)
                    .buildQueryMessage();

        } catch (OAuthSystemException e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
            return status(StatusCode.UnprocessableEntity);
        }

        String accessToken;
        Long expiresIn;
        String refreshToken;
        String scope;

        try {
            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            //Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
            //application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
            //Custom response classes are an easy way to deal with oauth providers that introduce modifications to
            //OAuth 2.0 specification
            GitHubTokenResponse oAuthResponse = oAuthClient.accessToken(request, GitHubTokenResponse.class);

            accessToken = oAuthResponse.getAccessToken();
            expiresIn = oAuthResponse.getExpiresIn();
            refreshToken = oAuthResponse.getRefreshToken();
            scope = oAuthResponse.getScope();
        } catch (OAuthSystemException e) {
            Logger.error(e.toString());
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (OAuthProblemException e) {
            Logger.error(e.toString());
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        ObjectNode json = Json.newObject();
        json.put("accessToken", accessToken);
        json.put("expiresIn", expiresIn);
        json.put("refreshToken", refreshToken);
        json.put("scope", scope);

        return ok(json);
    }

    public Result authorizations() {
        String accessToken = request().getQueryString("accessToken");
        String body;

        try {
            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            //OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(githubBaseUrl + "/user")
            OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(baseUrl + "/me")
                    .setAccessToken(accessToken).buildHeaderMessage();

            OAuthResourceResponse resourceResponse = oAuthClient
                    .resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

            body = resourceResponse.getBody();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (OAuthProblemException e) {
            Logger.error(e.toString());
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        return ok(body);
    }

    public Result access() {
        String githubBaseUrl = "https://github.com/settings/connections/applications/e8c97a27f3858b4370ef";
        String accessToken = request().getQueryString("accessToken");
        String body;

        try {
            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(githubBaseUrl)
                    .setAccessToken(accessToken).buildHeaderMessage();

            OAuthResourceResponse resourceResponse = oAuthClient
                    .resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

            body = resourceResponse.getContentType();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (OAuthProblemException e) {
            Logger.error(e.toString());
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        return ok(body);
    }
}
