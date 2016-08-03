package controllers;

import com.google.gson.*;
import com.google.inject.Inject;
import services.UserService;
import filters.LoginFilter;
import filters.PublisherFilter;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FilterWith({LoginFilter.class, PublisherFilter.class})
public class UserController {

    private final NinjaCache ninjaCache;
    private final Logger logger;
    private final UserService userService;

    @Inject
    public UserController(NinjaCache ninjaCache, Logger logger, UserService userService) {
        this.ninjaCache = ninjaCache;
        this.logger = logger;
        this.userService = userService;
    }

    /**
     *Render view to display all users
     *
     * @return
     */
    public Result viewUsers() {

        return Results
                .ok()
                .html();
    }

    /**
     * Display all roles available with roles assigned already selected.
     *
     * @param userId
     * @return html checkboxes associated with available roles
     */
    public Result displayUserRoles(@PathParam("userid") String userId) {

        if(StringUtils.isEmpty(userId)){
            logger.warn("User tried to query 'displayUserRoles' without a userid.");
            return Results.badRequest().text();
        }

        String checkBoxString = userService.generateRolesCheckboxes(userId);

        return Results.ok()
                .text()
                .render(checkBoxString);
    }

    /**
     * Update a user's roles
     *
     * @param userId
     * @param roles
     * @return
     */
    public Result updateUserRoles(@PathParam("userid") String userId, @PathParam("roles") String roles){

        if(StringUtils.isEmpty(userId)){
            logger.warn("User tried to 'updateUserRoles' without supplying a userid.");
            return Results.badRequest().text();
        }

        userService.updateUserRole(userId, Arrays.asList(roles.split(",")));

        return Results.ok().text();
    }

    /**
     * Display all registered users
     *
     * @param context
     * @return a map containing all query results
     */
    public Result displayUserData(Context context) {
        try {
            Integer draw = Integer.parseInt(context.getParameter("draw"));
            Integer start = Integer.parseInt(context.getParameter("start"));
            Integer length = Integer.parseInt(context.getParameter("length"));
            String search = context.getParameter("search[value]");

            Integer recordsTotal = userService.getTotalRecords();

            JsonObject usersJson = userService.findUserRecordsWithPaging(start, length, search);
            Integer recordsFiltered = usersJson.get("total")
                    .getAsInt();
            List<String[]> usersData = userService.generateDataTableResults(usersJson.getAsJsonArray("users"));

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
        }catch (JsonSyntaxException e){
            logger.error(e.getMessage());
            return Results.internalServerError().json();
        }
    }

    /**
     *THIS MIGHT GO AWAY
     *
     * @param flashScope
     * @param session
     * @return
     */
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

    /**
     *THIS MIGHT GO AWAY
     *
     * @param context
     * @param session
     * @return
     */
    public Result saveUpdate(Context context, Session session) {
        return Results.ok();
    }
}
