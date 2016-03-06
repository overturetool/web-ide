package actions;

import core.auth.SessionStore;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

public class SecuredAction extends Action.Simple {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        String receivedToken = getTokenFromHeader(ctx);
        if (receivedToken != null) {
            String userId = SessionStore.getInstance().get(receivedToken);
            if (userId != null) {
                return delegate.call(ctx);
            }
        }
        Result unauthorized = Results.unauthorized("unauthorized: token may have expired");
        return F.Promise.pure(unauthorized);
    }

    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("Authorization");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null))
            return authTokenHeaderValues[0].substring("Bearer ".length());

        return null;
    }
}
