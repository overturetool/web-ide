package actions;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context ctx) {
        String receivedToken = getTokenFromHeader(ctx);
        String token = ctx.session().get(receivedToken);
        String s = Http.Context.current().session().get(receivedToken);
        return token;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return Results.unauthorized("unauthorized");
    }

    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("Authorization");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null))
            return authTokenHeaderValues[0].substring("Bearer ".length());
        return null;
    }
}
