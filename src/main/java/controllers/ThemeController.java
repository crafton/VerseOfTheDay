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
import ninja.session.FlashScope;
import org.h2.jdbc.JdbcSQLException;
import org.slf4j.Logger;
import utilities.ControllerUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
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

    @Transactional
    public Result themes() {
        EntityManager entityManager = entityManagerProvider.get();

        Query q = entityManager.createNamedQuery("Theme.findAll");
        List<Theme> themes = (List<Theme>) q.getResultList();

        return Results
                .ok()
                .html()
                .render("themes", themes)
                .render("maxCols", controllerUtils.getThemesMaxCols());
    }

    @Transactional
    public Result saveTheme(Theme theme, FlashScope flashScope) {

        if (theme == null || theme.getThemeName().isEmpty()) {
            flashScope.error("A theme has not been submitted");
            return Results.redirect("/theme/list");
        }

        EntityManager entityManager = entityManagerProvider.get();
        Query q = entityManager.createNamedQuery("Theme.findByName");
        q.setParameter("name", theme.getThemeName());

        List<Theme> existingTheme = (List<Theme>) q.getResultList();

        if (!existingTheme.isEmpty()) {
            logger.warn("Tried to add theme that already exists.");
            flashScope.error("Cannot save, that theme already exists.");
            return Results.redirect("/theme/list");
        }

        entityManager.persist(theme);

        return Results.redirect("/theme/list");
    }

    @Transactional
    public Result deleteTheme(@PathParam("theme") Long themeId, FlashScope flashScope) {
        if (themeId == null) {
            flashScope.error("You must supply a theme Id");
            return Results.redirect("/theme/list");
        }

        EntityManager entityManager = entityManagerProvider.get();
        Theme theme = entityManager.find(Theme.class, themeId);

        if (theme == null) {
            flashScope.error("No theme found with the supplied ID");
            return Results.redirect("/theme/list");
        }

        entityManager.remove(theme);

        return Results.redirect("/theme/list");
    }
}
