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
import org.slf4j.Logger;
import utilities.Config;
import utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

        List<String> roles = null;
        roles = userDao.getUserRoles(userId);

        /*try {
            roles = userDao.getUserRoles(URLDecoder.decode(userId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e){
            e.printStackTrace();
        }*/

        String checkBoxString = "";
        String roleDescription = "";


        for (String roleType : config.rolesList) {
            if (roleType.contentEquals(config.MEMBER_ROLE)) {
                roleDescription = config.MEMBER_DESCRIPTION;
            } else if (roleType.contentEquals(config.CONTRIBUTOR_ROLE)) {
                roleDescription = config.CONTRIBUTOR_DESCRIPTION;
            } else if (roleType.contentEquals(config.PUBLISHER_ROLE)) {
                roleDescription = config.PUBLISHER_DESCRIPTION;
            }
            if (roles.contains(roleType)) {
                checkBoxString += "<div class=\"checkbox\">\n" +
                        "                <label>\n" +
                        "                    <input type=\"checkbox\" value=\"" + roleType + "\" checked>\n" +
                        "                    <b>" + roleType + "</b><br/>\n" +
                        "                    <i>" + roleDescription + "</i>\n" +
                        "                </label>\n" +
                        "            </div>";
            } else {
                checkBoxString += "<div class=\"checkbox\">\n" +
                        "                <label>\n" +
                        "                    <input type=\"checkbox\" value=\"" + roleType + "\" >\n" +
                        "                    <b>" + roleType + "</b><br/>\n" +
                        "                    <i>" + roleDescription + "</i>\n" +
                        "                </label>\n" +
                        "            </div>";
            }

        }

        return Results.ok()
                .text()
                .render(checkBoxString);
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
