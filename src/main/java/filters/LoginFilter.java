package filters;

import com.google.inject.Inject;
import ninja.*;
import ninja.cache.NinjaCache;


/**
 * Created by Crafton Williams on 17/06/2016.
 */
public class LoginFilter implements Filter {

    public final String accessToken = "accessToken";
    public final String idToken = "idToken";

    @Inject
    NinjaCache ninjaCache;

    @Override
    public Result filter(FilterChain chain, Context context){

        String idTokenString = context.getSession().get(idToken);

        if(context.getSession() == null
                || idTokenString  == null
                || ninjaCache.get(idTokenString) == null){

            return Results.redirect("/login");
        }else{
            return chain.next(context);
        }
    }

}
