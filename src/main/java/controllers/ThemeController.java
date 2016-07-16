package controllers;

import com.google.inject.Inject;
import services.ThemeDao;
import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import filters.LoginFilter;
import filters.PublisherFilter;
import models.Theme;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import utilities.Config;

import java.util.List;

@FilterWith({LoginFilter.class, PublisherFilter.class})
public class ThemeController {

    @Inject
    private ThemeDao themeDao;
    @Inject
    private Logger logger;
    @Inject
    private Config config;

    /**
     * Retrieve list of themes from database as well as the maximum number of
     * columns to render.
     *
     * @return
     */
    public Result themes() {
        logger.debug("Generating themes list...");
        List<Theme> themes = themeDao.findAll();

        return Results
                .ok()
                .html()
                .render("themes", themes)
                .render("maxCols", config.getThemesMaxCols());
    }

    /**
     * Save a new theme to the database.
     *
     * @param theme
     * @param flashScope
     * @return
     */
    public Result saveTheme(Theme theme, FlashScope flashScope) {
        logger.debug("Entered saveTheme action...");

        try {
            themeDao.save(theme);
        } catch (IllegalArgumentException e) {
            logger.warn("User tried to access the save controller directly.");
            flashScope.error("A theme has not been submitted");
        } catch (EntityAlreadyExistsException e) {
            logger.warn(e.getMessage());
            flashScope.error("Cannot save, that theme already exists.");
        }

        return Results.redirect("/theme/list");
    }

    /**
     *Delete a theme given a themeId
     *
     * @param themeId
     * @param flashScope
     * @return
     */
    public Result deleteTheme(@PathParam("theme") Long themeId, FlashScope flashScope) {
        logger.debug("Entered deleteTheme action...");

        try {
            themeDao.delete(themeId);
            logger.info("Successfully deleted theme.");
            flashScope.success("Successfully deleted theme.");
        } catch (IllegalArgumentException e) {
            logger.warn("User tried to delete a theme by accessing the action directly.");
            flashScope.error("You must supply a theme Id");
        } catch (EntityDoesNotExistException e) {
            logger.warn("Tried to delete a theme that doesn't exist.");
            flashScope.error("No theme found with the supplied ID");
        } catch (EntityBeingUsedException e) {
            logger.warn(e.getMessage());
            flashScope.error("This theme is being used by other Votds. You cannot remove it until it is " +
                    "removed from those Votds.");
        }

        return Results.redirect("/theme/list");
    }
}
