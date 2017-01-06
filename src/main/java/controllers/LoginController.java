package controllers;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
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
import org.slf4j.LoggerFactory;
import services.UserService;
import utilities.Config;


public class LoginController {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;
    private final NinjaCache ninjaCache;
    private final Config config;

    @Inject
    public LoginController(UserService userService, NinjaCache ninjaCache, Config config) {
        this.userService = userService;
        this.ninjaCache = ninjaCache;
        this.config = config;
    }

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
            return Results.redirect("/servererror");
        }

        try {
            userService.createSession(session, code);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            logger.error(e.getMessage());
            Results.redirect("/servererror");
        }

        return Results.redirect("/campaign/list");
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
        return Results.redirect("https://" + config.getAuth0Domain() + "/v2/logout?returnTo=http%3A%2F%2Fdailyverses.faith/");
    }
}
