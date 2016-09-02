package filters;

import com.google.inject.Inject;
import ninja.*;
import ninja.cache.NinjaCache;
import utilities.Config;


public class LoginFilter implements Filter {

    @Inject
    private NinjaCache ninjaCache;

    @Inject
    private Config config;

    @Override
    public Result filter(FilterChain chain, Context context){

        String idTokenString = context.getSession().get(config.IDTOKEN_NAME);

        if(context.getSession() == null
                || idTokenString  == null
                || ninjaCache.get(idTokenString) == null){

            return Results.redirect("/login");
        }else{
            return chain.next(context);
        }
    }

}
