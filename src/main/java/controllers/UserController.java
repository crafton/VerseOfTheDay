package controllers;

import com.google.gson.*;
import com.google.inject.Inject;
import daos.UserDao;
import filters.LoginFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.cache.NinjaCache;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import utilities.Config;
import utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crafton Williams on 20/06/2016.
 */
@FilterWith(LoginFilter.class)
public class UserController {

    @Inject
    NinjaCache ninjaCache;

    @Inject
    Logger logger;

    @Inject
    UserDao userDao;

    @Inject
    Config config;

    @Inject
    Utils utils;

    public Result viewUsers() {

        return Results
                .ok()
                .html();
    }

    public Result displayUserRoles(@PathParam("userid") String userId) {

        String checkBoxString = userDao.generateRolesCheckboxes(userId);

        return Results.ok()
                .text()
                .render(checkBoxString);
    }

    public Result updateUserRoles(@PathParam("userid") String userId, @PathParam("roles") String roles, FlashScope flashScope){

        if(StringUtils.isEmpty(userId)){
            return Results.badRequest().text();
        }

        userDao.updateUserRole(userId, Arrays.asList(roles.split(",")));

        return Results.ok().text();
    }

    public Result displayUserData(Context context) {
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
