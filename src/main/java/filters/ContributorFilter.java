package filters;

import com.google.inject.Inject;
import ninja.*;
import utilities.Config;
import utilities.Utils;

/**
 * Created by Crafton Williams on 9/07/2016.
 */
public class ContributorFilter implements Filter {
    @Inject
    private Config config;

    @Inject
    private Utils utils;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        String idTokenString = context.getSession().get("idToken");

        if (utils.hasRole(idTokenString, config.CONTRIBUTOR_ROLE) || utils.hasRole(idTokenString, config.PUBLISHER_ROLE)
                || utils.hasRole(idTokenString, "admin")) {
            return filterChain.next(context);
        } else {
            return Results.redirect("/");
        }
    }
}
