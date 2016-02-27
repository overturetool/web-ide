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
    public Result login() {
        OAuthClientRequest request;

        try {
            request = OAuthClientRequest
                    .authorizationProvider(OAuthProviderType.GITHUB)
                    .setClientId("e8c97a27f3858b4370ef")
                    .setRedirectURI("http://localhost:9000/callback")
                    .setScope("user,user:email")
                    .setParameter("token_type", "bearer")
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
                    .tokenProvider(OAuthProviderType.GITHUB)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId("e8c97a27f3858b4370ef")
                    .setClientSecret("3d45a6b5666c0f16d4fd04f3a2f03c9705f1da4f")
                    .setRedirectURI("http://localhost:9000/callback")
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
        String githubBaseUrl = "https://api.github.com";
        String accessToken = request().getQueryString("accessToken");
        String body;

        try {
            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(githubBaseUrl + "/user")
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