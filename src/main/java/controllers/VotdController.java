package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import daos.ThemeDao;
import daos.VotdDao;
import models.Theme;
import models.Votd;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ControllerUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Crafton Williams on 19/03/2016.
 */

@Singleton
public class VotdController {

    @Inject
    ControllerUtils controllerUtils;
    @Inject
    VotdDao votdDao;
    @Inject
    ThemeDao themeDao;
    @Inject
    Logger logger;

    public Result viewVotds() {

        List<Votd> votds = votdDao.findAll();

        return Results
                .ok()
                .html()
                .render("votds", votds);
    }

    public Result createVotd() {
        List<Theme> themes = themeDao.findAll();

        return Results
                .ok()
                .html()
                .render("themes", themes);
    }

    public Result getVerse(@PathParam("verses") String verses) {

        String verificationErrorMessage = controllerUtils.verifyVerses(verses);

        if (!verificationErrorMessage.isEmpty()) {
            return Results.badRequest().text().render(verificationErrorMessage);
        }

        String versesTrimmed = verses.trim();

        /*Call web service to retrieve verses.*/
        String versesRetrieved = controllerUtils.restGetVerses(versesTrimmed);

        /*Find all verses that clash with what we're trying to add to the database*/
        List<String> verseClashes = controllerUtils.findClashes(versesTrimmed);
        if (!verseClashes.isEmpty()) {
            versesRetrieved += "<h4 id='clash' class='text-danger'>Verse Clashes</h4>" +
                    "<small>Verses that already exist in the database which " +
                    "intersect with the verses being entered.</small>"
                    + controllerUtils.formatListToHtml(verseClashes);
        }

        return Results.ok().text().render(versesRetrieved);
    }

    public Result saveVotd(Context context, Votd votd, FlashScope flashScope) {

        String verificationErrorMessage = controllerUtils.verifyVerses(votd.getVerses());

        if (!verificationErrorMessage.isEmpty()) {
            flashScope.error(verificationErrorMessage);
            return Results.redirect("/votd/create");
        }

        //Retrieve the themeIDs selected and convert to list of themes
        List<String> themeIds = context.getParameterValues("themes");

        if (themeIds.isEmpty()) {
            votd.setThemes(new ArrayList<Theme>());
        }

        List<Theme> themeList = new ArrayList<>();
        for (String themeId : themeIds) {
            Theme theme = themeDao.findById(Long.parseLong(themeId));
            themeList.add(theme);
        }
        votd.setThemes(themeList);
        votdDao.save(votd);

        flashScope.success("Successfully created a new VoTD entry.");
        return Results.redirect("/votd/create");

    }

    public Result updateVotd(@PathParam("verseid") Long verseid, FlashScope flashScope){

        if (verseid == null) {
            flashScope.error("You must supply a valid verse Id.");
            return Results.redirect("/votd/list");
        }

        Votd votd = votdDao.findById(verseid);

        //Get all themes
        List<Theme> themes = themeDao.findAll();

        if (votd == null) {
            flashScope.error("Tried to update a Votd that doesn't exist.");
            return Results.redirect("/votd/list");
        }

        //Get verse text
        String verseText = controllerUtils.restGetVerses(votd.getVerses());

        return Results
                .ok()
                .html()
                .render("votd", votd)
                .render("themes", themes)
                .render("verseText", verseText);
    }

    public Result saveVotdUpdate(Context context, FlashScope flashScope){

        Long votdId = Long.parseLong(context.getParameter("verseid"));
        Votd votd = votdDao.findById(votdId);

        if(votd == null){
            flashScope.error("The VOTD you're trying to update does not exist.");
            return Results.redirect("/votd/list");
        }

        //Retrieve the themeIDs selected and convert to list of theme objects
        List<String> themeIds = context.getParameterValues("themes");

        List<Theme> themeList = new ArrayList<>();
        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeDao.findById(Long.parseLong(themeId));
                themeList.add(theme);
            }

        }

        votdDao.update(votdId, themeList);

        flashScope.success("Successfullt updated verse(s): "+ votd.getVerses());
        return Results.redirect("/votd/list");
    }

    public Result approveVotd(@PathParam("votdid") Long votdId, FlashScope flashScope){
        if (votdId == null){
            flashScope.error("You must supply a valid votdid.");
            return Results.redirect("/votd/list");
        }

        Votd votd = votdDao.findById(votdId);

        if(votd == null){
            flashScope.error("You can't approve a votd that doesn't exist.");
            return Results.redirect("/votd/list");
        }

        votdDao.approve(votdId);
        flashScope.success("Successfully approved: " + votd.getVerses());
        return Results.redirect("/votd/list");
    }

    public Result deleteVotd(@PathParam("verseid") Long verseid, FlashScope flashScope) {
        if (verseid == null) {
            flashScope.error("You must supply a votd Id.");
            return Results.redirect("/votd/list");
        }

        Votd votd = votdDao.findById(verseid);

        if (votd == null) {
            flashScope.error("Tried to delete a Votd that doesn't exist");
            return Results.redirect("/votd/list");
        }

        votdDao.delete(verseid);
        flashScope.success("Successfully deleted Votd: " + votd.getVerses());
        return Results.redirect("/votd/list");
    }

}