package filters;

import com.google.inject.Inject;
import ninja.*;
import services.UserService;
import utilities.Config;

/**
 * Created by Crafton Williams on 9/07/2016.
 */
public class ContributorFilter implements Filter {
    @Inject
    private Config config;

    @Inject
    private UserService userService;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        String idTokenString = context.getSession().get("idToken");

        if (userService.hasRole(idTokenString, config.getContributorRole()) || userService.hasRole(idTokenString, config.getPublisherRole())
                || userService.hasRole(idTokenString, "admin")) {
            return filterChain.next(context);
        } else {
            return Results.redirect("/unauthorized");
        }
    }
}
