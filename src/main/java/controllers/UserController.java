package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.cache.NinjaCache;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.slf4j.Logger;

/**
 * Created by Crafton Williams on 20/06/2016.
 */
public class UserController {

    @Inject
    NinjaCache ninjaCache;

    @Inject
    Logger logger;

    public Result users(){


        return Results.ok();
    }

    public Result updateUser(FlashScope flashScope, Session session) {

        String idTokenString = session.get("idToken");
        String userProfileString = ninjaCache.get(idTokenString).toString();

        try {
            JsonObject userProfile = new JsonParser().parse(userProfileString).getAsJsonObject();
            String email = userProfile.get("email").getAsString();
            return Results.ok().render("email", email);
        } catch (JsonSyntaxException e) {
            flashScope.error("Could not retrieve your details. Please contact the administrator for help.");
            logger.error("Could not retrieve user details.");
            return Results.redirect("/");
        }
    }

    public Result saveUpdate(Context context, Session session) {
        return Results.ok();
    }
}
