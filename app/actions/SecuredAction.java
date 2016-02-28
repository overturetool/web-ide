package actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class SecuredAction extends Action.Simple {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
//        String token = getTokenFromHeader(ctx);
//        if (token != null) {
//            User user = BIConversion.User.find.where().eq("authToken", token).findUnique();
//            if (user != null) {
//                ctx.request().setUsername(user.username);
//                return delegate.call(ctx);
//            }
//        }
//        Result unauthorized = Results.unauthorized("unauthorized");
//        return F.Promise.pure(unauthorized);
        return delegate.call(ctx);
    }

    private String getTokenFromHeader(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null))
            return authTokenHeaderValues[0];

        return null;
    }
}
