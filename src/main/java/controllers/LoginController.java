package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.Context;
import ninja.Result;
import ninja.Results;

import org.slf4j.Logger;
import utilities.Config;
import utilities.ControllerUtils;

import java.util.Map;


/**
 * Created by Crafton Williams on 13/06/2016.
 */
@Singleton
public class LoginController {

    @Inject
    Config config;

    @Inject
    Logger logger;

    @Inject
    ControllerUtils controllerUtils;

    public Result login(){

        return Results.ok()
                .render("clientid", config.getAuth0ClientId())
                .render("domain", config.getAuth0Domain())
                .render("callback", config.getAuth0Callback());
    }

    public Result callback(Context context){

        String code = context.getParameter("code");

        Map<String, String> tokens = controllerUtils.auth0GetToken(code);

        return Results.redirect("/votd/list");
    }
}
