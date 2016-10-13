/**
 * Copyright (C) 2013 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import org.apache.commons.lang.StringUtils;
import services.UserService;
import utilities.Config;


@Singleton
public class ApplicationController {

    private final UserService userService;
    private final Config config;

    @Inject
    public ApplicationController(UserService userService, Config config){
        this.userService = userService;
        this.config = config;
    }

    /**
     * Display the application's main page
     *
     * @return
     */
    public Result index(Context context) {

        String idToken = context.getSession().get(config.IDTOKEN_NAME);

        if(!StringUtils.isEmpty(idToken)){
            String role = userService.getHighestRole(idToken);

            return Results.html()
                    .render("loggedIn", true)
                    .render("role", role);
        }

        return Results.html();

    }

    /**
     * Display an 'unauthorized' page if a user tries to access a resource
     * outside of his permissions.
     *
     * @return
     */
    public Result unauthorized(Context context) {

        String idToken = context.getSession().get(config.IDTOKEN_NAME);

        if(!StringUtils.isEmpty(idToken)){
            String role = userService.getHighestRole(idToken);

            return Results.html()
                    .render("loggedIn", true)
                    .render("role", role);
        }

        return Results.html();
    }

    /**
     * Display a server error page if something has gone horribly wrong.
     *
     * @return
     */
    public Result servererror(Context context) {

        String idToken = context.getSession().get(config.IDTOKEN_NAME);

        if(!StringUtils.isEmpty(idToken)){
            String role = userService.getHighestRole(idToken);

            return Results.html()
                    .render("loggedIn", true)
                    .render("role", role);
        }

        return Results.html();
    }

}
