package actions;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class SecuredAction extends Action.Simple {
    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        return delegate.call(ctx);
//        String receivedToken = getTokenFromHeader(ctx);
//        if (receivedToken != null) {
//            String userId = SessionStore.getInstance().get(receivedToken);
//            if (userId != null) {
//                return delegate.call(ctx);
//            }
//        }
//        Result unauthorized = Results.unauthorized("unauthorized: token may have expired");
//        return CompletableFuture.completedFuture(unauthorized);
    }

    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("Authorization");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            return authTokenHeaderValues[0].substring("Bearer ".length());
        }
        return null;
    }
}
