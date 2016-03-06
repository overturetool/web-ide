package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import core.StatusCode;
import core.auth.GoogleAuthentication;
import core.auth.SessionStore;
import core.utilities.ServerUtils;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class auth extends Controller {
    public Result verify(String tokenId) {
        String accessToken = ServerUtils.extractAccessToken(request());
        GoogleAuthentication googleAuthentication = new GoogleAuthentication();

        if (accessToken == null)
            return unauthorized("Missing access token");

        GoogleIdToken idToken;
        try {
            idToken = googleAuthentication.verify(tokenId);
        } catch (GeneralSecurityException | IOException e) {
            Logger.debug(e.getMessage());
            return status(StatusCode.UnprocessableEntity, e.toString());
        } catch (IllegalArgumentException e) {
            Logger.debug(e.getMessage());
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        if (idToken == null)
            return unauthorized("Invalid ID token.");

        JsonNode credential = googleAuthentication.getTokenInfo(accessToken);
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

        SessionStore.getInstance().set(accessToken, userId);

        ObjectNode node = googleAuthentication.extractUserNode(payload);

        return ok(node);
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
}
