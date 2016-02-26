package controllers;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;

public class auth extends Application {

    public Result authCallback() {
        String userInfo = "";

        try {
            GitkitUser gitkitUser = null;
            GitkitClient gitkitClient = GitkitClient.createFromJson("gitkit-server-config.json");

            Http.Request request = request();

            gitkitUser = gitkitClient.validateToken(request().getHeader("gtoken"));
            if (gitkitUser != null) {
                userInfo = "Welcome back!<br><br> Email: " + gitkitUser.getEmail() + "<br> Id: "
                        + gitkitUser.getLocalId() + "<br> Provider: " + gitkitUser.getCurrentProvider();
            }
        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
        }

        return ok(userInfo);
    }
}
