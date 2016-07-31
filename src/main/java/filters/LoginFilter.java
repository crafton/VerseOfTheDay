package filters;

import com.google.inject.Inject;
import ninja.*;
import ninja.cache.NinjaCache;


public class LoginFilter implements Filter {

    @Inject
    private NinjaCache ninjaCache;

    @Override
    public Result filter(FilterChain chain, Context context){

        String idToken = "idToken";
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
