package controllers;

import com.google.gson.*;
import com.google.inject.Inject;
import daos.UserDao;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.cache.NinjaCache;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.slf4j.Logger;
import utilities.Config;
import utilities.ControllerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crafton Williams on 20/06/2016.
 */
public class UserController {

    @Inject
    NinjaCache ninjaCache;

    @Inject
    Logger logger;

    @Inject
    UserDao userDao;

    @Inject
    ControllerUtils controllerUtils;

    public Result viewUsers() {

        return Results
                .ok()
                .html();
    }

    public Result displayUserData(Context context){
        Integer draw = Integer.parseInt(context.getParameter("draw"));
        Integer start = Integer.parseInt(context.getParameter("start"));
        Integer length = Integer.parseInt(context.getParameter("length"));
        String search = context.getParameter("search[value]");

        Integer recordsTotal = userDao.getTotalRecords();

        JsonObject usersJson = userDao.getUserRecords(start, length, search);
        Integer recordsFiltered = usersJson.get("total")
                .getAsInt();
        List<String[]> usersData = userDao.generateDataTableResults(usersJson.getAsJsonArray("users"));

        /*Format data for ajax callback processing*/
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("draw", draw);
        userMap.put("recordsTotal", recordsTotal);
        userMap.put("recordsFiltered", recordsFiltered);
        userMap.put("data", usersData);

        return Results
                .ok()
                .json()
                .render(userMap);
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
