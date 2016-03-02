package core.utilities;

import play.mvc.Http;

public class ServerUtils {
    public synchronized static String extractAccessToken(Http.Request request) {
        String accessToken = request.getHeader("Authorization");
        String tokenPrefix = "Bearer ";

        if (accessToken == null)
            return null;

        if (accessToken.startsWith(tokenPrefix) && accessToken.length() > tokenPrefix.length())
            accessToken = accessToken.substring(tokenPrefix.length()); // remove token prefix 'Bearer'

        return accessToken;
    }
}
