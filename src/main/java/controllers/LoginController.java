package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import org.h2.engine.User;
import repositories.UserRepository;
import services.UserService;
import filters.LoginFilter;
import filters.MemberFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;

import ninja.cache.NinjaCache;
import ninja.session.Session;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utilities.Config;
import utilities.Utils;

import java.util.Map;


public class LoginController {

    @Inject
    private Config config;

    @Inject
    private Logger logger;

    @Inject
    private Utils utils;

    @Inject
    private UserService userService;

    @Inject
    private NinjaCache ninjaCache;

    @Inject
    private UserRepository userRepository;

    /**
     * Direct to view with auth0 login lock.
     *
     * @return
     */
    public Result login() {

        logger.debug("Entered login controller...");

        return Results.ok()
                .render("clientid", config.getAuth0ClientId())
                .render("domain", config.getAuth0Domain())
                .render("callback", config.getAuth0Callback());
    }

    /**
     * Login callback. This action will be triggered after a user successfully authenticates.
     *
     * @param context
     * @param session
     * @return
     */
    public Result callback(Context context, Session session) {

        logger.debug("Entered login callback...");

        String code = context.getParameter("code");

        /*Make sure an authorization code is received before proceeding.*/
        if (StringUtils.isEmpty(code)) {
            logger.error("Authorization code not received.");
            Results.redirect("/servererror");
        }

        try {
            userService.createSession(session, code);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.error(e.getMessage());
            Results.redirect("/servererror");
        }

        return Results.redirect("/votd/list");
    }

    /**
     * Logout of the votd application.
     *
     * @param session
     * @return
     */
    @FilterWith({LoginFilter.class, MemberFilter.class})
    public Result logout(Session session) {

        String idToken = session.get("idToken");

        /*Delete user from cache and clear session*/
        ninjaCache.delete(idToken);
        session.clear();

        /*IdP logout and redirect*/
        return Results.redirect("https://" + config.getAuth0Domain() + "/v2/logout?returnTo=http://localhost:8080/");
    }
}
