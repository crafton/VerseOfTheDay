package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import models.Theme;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;
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

    @UnitOfWork
    public Result themes(){
        EntityManager entityManager = entityManagerProvider.get();

        Query q = entityManager.createQuery("SELECT themeName FROM Theme");
        List<Theme> themes = (List<Theme>)q.getResultList();

        return Results
                .ok()
                .html()
                .render("themes", themes)
                .render("maxCols", controllerUtils.getThemesMaxCols());
    }

    public Result createTheme(){
        return null;
    }

    public Result saveTheme(){
        return null;
    }

    public Result deleteTheme(){
        return null;
    }
}
