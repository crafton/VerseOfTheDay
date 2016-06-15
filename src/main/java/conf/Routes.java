/**
 * Copyright (C) 2012 the original author or authors.
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

package conf;


import controllers.LoginController;
import controllers.ThemeController;
import controllers.VotdController;
import models.Theme;
import models.Votd;
import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.ApplicationController;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {

        router.GET().route("/").with(ApplicationController.class, "index");
        router.GET().route("/hello_world.json").with(ApplicationController.class, "helloWorldJson");

        /**
         * Routes for VOTD CRUD
         */
        router.GET().route("/votd/list").with(VotdController.class, "viewVotds");
        router.GET().route("/votd/create").with(VotdController.class, "createVotd");
        router.GET().route("/votd/getverse/{verses}").with(VotdController.class, "getVerse");
        router.POST().route("/votd/save").with(VotdController.class, "saveVotd");
        router.GET().route("/votd/update/{verseid}").with(VotdController.class, "updateVotd");
        router.POST().route("/votd/saveupdate").with(VotdController.class, "saveVotdUpdate");
        router.GET().route("/votd/delete/{verseid}").with(VotdController.class, "deleteVotd");
        router.GET().route("/votd/approve/{votdid}").with(VotdController.class, "approveVotd");

        /**
         * Routes for Theme CRUD
         */

        router.GET().route("/theme/list").with(ThemeController.class, "themes");
        router.POST().route("/theme/save").with(ThemeController.class, "saveTheme");
        router.GET().route("/theme/delete/{theme}").with(ThemeController.class, "deleteTheme");

        /**
         * Routes for Login
        */

        router.GET().route("/login").with(LoginController.class, "login");
        router.GET().route("/callback").with(LoginController.class, "callback");


        ///////////////////////////////////////////////////////////////////////
        // Assets (pictures / javascript)
        ///////////////////////////////////////////////////////////////////////    
        router.GET().route("/assets/webjars/{fileName: .*}").with(AssetsController.class, "serveWebJars");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");

        ///////////////////////////////////////////////////////////////////////
        // Index / Catchall shows index page
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/.*").with(ApplicationController.class, "index");
    }

}
