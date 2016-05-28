package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import models.Theme;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;
import org.slf4j.Logger;
import utilities.ControllerUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crafton Williams on 28/05/2016.
 */
@Singleton
public class ThemeController {

    @Inject
    ControllerUtils controllerUtils;
    @Inject
    Provider<EntityManager> entityManagerProvider;
    @Inject
    Logger logger;

    @UnitOfWork
    public Result themes(){
        EntityManager entityManager = entityManagerProvider.get();

        Query q = entityManager.createNamedQuery("Theme.findAll");
        List<Theme> themes = (List<Theme>)q.getResultList();

        return Results
                .ok()
                .html()
                .render("themes", themes)
                .render("maxCols", controllerUtils.getThemesMaxCols());
    }

    @Transactional
    public Result saveTheme(Theme theme){
        //TODO: null check

        EntityManager entityManager = entityManagerProvider.get();
        entityManager.persist(theme);

        return Results.redirect("/theme/list");
    }

    @Transactional
    public Result deleteTheme(@PathParam("theme") Long themeId){
        //TODO: null check

        EntityManager entityManager = entityManagerProvider.get();
        Theme theme = entityManager.find(Theme.class, themeId);
        entityManager.remove(theme);

        return Results.redirect("/theme/list");
    }
}
