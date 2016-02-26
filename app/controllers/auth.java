package controllers;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import core.StatusCode;
import play.mvc.Result;

import java.io.IOException;

public class auth extends Application {

    public Result authCallback(String token) {
        String userInfo = "";

        try {
            GitkitUser gitkitUser;
            GitkitClient gitkitClient = GitkitClient.createFromJson("gitkit-server-config.json");

            gitkitUser = gitkitClient.validateToken(token);
            if (gitkitUser == null)
                return status(StatusCode.UnprocessableEntity);

//            userInfo = "Welcome back!<br><br> Email: "
//                    + gitkitUser.getEmail() + "<br> Id: "
//                    + gitkitUser.getLocalId()
//                    + "<br> Provider: " + gitkitUser.getCurrentProvider();

            userInfo = gitkitUser.getName();

        } catch (GitkitClientException | IOException e) {
            e.printStackTrace();
        }

        return ok(userInfo);
    }
}
