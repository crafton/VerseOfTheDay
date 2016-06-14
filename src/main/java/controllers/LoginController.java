package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.Result;
import ninja.Results;
import utilities.Config;
import utilities.ControllerUtils;

/**
 * Created by Crafton Williams on 13/06/2016.
 */
@Singleton
public class LoginController {

    @Inject
    Config config;

    public Result login(){

        return Results.ok()
                .render("clientid", config.getAuth0ClientId())
                .render("domain", config.getAuth0Domain())
                .render("callback", config.getAuth0Callback());
    }
}
