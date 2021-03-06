package filters;

import com.google.inject.Inject;
import ninja.*;
import services.UserService;
import utilities.Config;

public class AdminFilter implements Filter {
    @Inject
    private Config config;

    @Inject
    private UserService userService;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        String idTokenString = context.getSession().get(config.IDTOKEN_NAME);

        if (userService.hasRole(idTokenString, "admin")) {
            return filterChain.next(context);
        } else {
            return Results.redirect("/unauthorized");
        }
    }
}
