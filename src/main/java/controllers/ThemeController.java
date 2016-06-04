package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import daos.ThemeDao;
import models.Theme;
import models.Votd;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import org.h2.jdbc.JdbcSQLException;
import org.slf4j.Logger;
import utilities.ControllerUtils;

import javax.persistence.*;
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
    ThemeDao themeDao;
    @Inject
    Logger logger;

    public Result themes() {
        List<Theme> themes = themeDao.findAll();

        return Results
                .ok()
                .html()
                .render("themes", themes)
                .render("maxCols", controllerUtils.getThemesMaxCols());
    }

    public Result saveTheme(Theme theme, FlashScope flashScope) {

        if (theme == null || theme.getThemeName().isEmpty()) {
            flashScope.error("A theme has not been submitted");
            return Results.redirect("/theme/list");
        }

        try{
            themeDao.findByName(theme.getThemeName());
            logger.warn("Tried to add theme that already exists.");
            flashScope.error("Cannot save, that theme already exists.");
            return Results.redirect("/theme/list");
        }catch (NoResultException nre){
            logger.info("Theme does not exist, proceeding to save...");
        }

        themeDao.save(theme);

        return Results.redirect("/theme/list");
    }

    public Result deleteTheme(@PathParam("theme") Long themeId, FlashScope flashScope) {
        if (themeId == null) {
            flashScope.error("You must supply a theme Id");
            return Results.redirect("/theme/list");
        }

        Theme theme = themeDao.findById(themeId);

        if (theme == null) {
            logger.warn("Tried to delete a theme that doesn't exist.");
            flashScope.error("No theme found with the supplied ID");
            return Results.redirect("/theme/list");
        }

        if (theme.getVotds().size() > 0) {
            logger.warn("Attempting to delete a theme that is being used.");
            flashScope.error("This theme is being used by other Votds. You cannot remove it until it is " +
                    "removed from those Votds.");
            return Results.redirect("/theme/list");
        }

        themeDao.delete(themeId);
        logger.info("Successfully deleted theme " + theme.getThemeName());
        flashScope.success("Successfully deleted theme: " + theme.getThemeName());

        return Results.redirect("/theme/list");
    }
}
