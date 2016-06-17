package controllers;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.Context;
import ninja.Result;
import ninja.Results;

import ninja.cache.NinjaCache;
import ninja.session.Session;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utilities.Config;
import utilities.ControllerUtils;

import java.util.Map;


/**
 * Created by Crafton Williams on 13/06/2016.
 */

public class LoginController {

    @Inject
    Config config;

    @Inject
    Logger logger;

    @Inject
    ControllerUtils controllerUtils;

    @Inject
    NinjaCache ninjaCache;

    public Result login() {

        return Results.ok()
                .render("clientid", config.getAuth0ClientId())
                .render("domain", config.getAuth0Domain())
                .render("callback", config.getAuth0Callback());
    }

    public Result callback(Context context, Session session) {

        String code = context.getParameter("code");

        if (StringUtils.isEmpty(code)) {
            //TODO:send to some unauthenticated page
        }

        /*Retrieve authentication tokens from auth0*/
        Map<String, String> tokens = controllerUtils.auth0GetToken(code);
        JsonObject userObject = controllerUtils.auth0GetUser(tokens.get("access_token"));

        /*Cache user profile so we don't have to query information again for the session*/
        ninjaCache.set(tokens.get("id_token"), userObject.toString());

        /*Store only tokens in the session cookie*/
        session.put("idToken", tokens.get("id_token"));
        session.put("accessToken", tokens.get("access_token"));

        return Results.redirect("/votd/list");
    }

    public Result logout(Session session) {

        String idToken = session.get("idToken");

        /*Delete user from cache and clear session*/
        ninjaCache.delete(idToken);
        session.clear();

        /*IdP logout and redirect*/
        return Results.redirect("https://" + config.getAuth0Domain() + "/v2/logout?returnTo=http://localhost:8080");
    }
}
