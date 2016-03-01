package actions;

import play.libs.F;
import play.mvc.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Security {
    /**
     * Wraps the annotated action in an <code>AuthenticatedAction</code>.
     */
    @With(AuthenticatedAction.class)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Authenticated {
        Class<? extends Authenticator> value() default Authenticator.class;
    }

    /**
     * Wraps another action, allowing only authenticated HTTP requests.
     * <p>
     * The user name is retrieved from the session cookie, and added to the HTTP request's
     * <code>username</code> attribute.
     */
    public static class AuthenticatedAction extends Action<Authenticated> {

        public F.Promise<Result> call(Http.Context ctx) {
            try {
                Authenticator authenticator = configuration.value().newInstance();
                String username = authenticator.getUsername(ctx);
                if(username == null) {
                    Result unauthorized = authenticator.onUnauthorized(ctx);
                    return F.Promise.pure(unauthorized);
                }
                return delegate.call(ctx);
            } catch(RuntimeException e) {
                throw e;
            } catch(Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Handles authentication.
     */
    public static class Authenticator extends Results {

        /**
         * Retrieves the username from the HTTP context; the default is to read from the session cookie.
         *
         * @return null if the user is not authenticated.
         */
        public String getUsername(Http.Context ctx) {
            String tokenFromHeader = getTokenFromHeader(ctx);
            return ctx.session().get(tokenFromHeader);
        }

        /**
         * Generates an alternative result if the user is not authenticated; the default a simple '401 Not Authorized' page.
         */
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
}
