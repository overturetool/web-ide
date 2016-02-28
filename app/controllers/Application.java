package controllers;

import actions.SecuredAction;
import play.mvc.Controller;
import play.mvc.With;

@With(SecuredAction.class)
public abstract class Application extends Controller {
}
