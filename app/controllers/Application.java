package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.hello.hello;
import views.html.index;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result hello() {
        return ok(hello.render("Hello"));
    }
}
