package filters;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.*;
import ninja.cache.NinjaCache;
import utilities.Config;
import utilities.Utils;

/**
 * Created by Crafton Williams on 9/07/2016.
 */
public class MemberFilter implements Filter {

    @Inject
    private Config config;

    @Inject
    private Utils utils;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        String idTokenString = context.getSession().get("idToken");

        if (utils.hasRole(idTokenString, config.MEMBER_ROLE)) {
            return filterChain.next(context);
        } else {
            return Results.redirect("/");
        }
    }
}
