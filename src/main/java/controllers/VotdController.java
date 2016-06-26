package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import daos.ThemeDao;
import daos.VotdDao;
import exceptions.EntityDoesNotExistException;
import filters.LoginFilter;
import models.Theme;
import models.Votd;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import utilities.Config;
import utilities.ControllerUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crafton Williams on 19/03/2016.
 */

@FilterWith(LoginFilter.class)
public class VotdController {

    @Inject
    ControllerUtils controllerUtils;
    @Inject
    VotdDao votdDao;
    @Inject
    ThemeDao themeDao;
    @Inject
    Logger logger;
    @Inject
    Config config;

    @Inject
    Provider<EntityManager> entityManagerProvider;

    public Result viewVotds() {

        return Results
                .ok()
                .html();
    }

    public Result allVotds(Context context) {

        String[] columns = new String[8];
        columns[0] = "verses";
        columns[1] = "themes";
        columns[2] = "status";
        columns[3] = "approve";
        columns[4] = "createdby";
        columns[5] = "modifiedby";
        columns[6] = "";
        columns[7] = "";

     /*   EntityManager entityManager = entityManagerProvider.get();
        Query q = entityManager.createNativeQuery("SELECT ");*/


        Integer draw = Integer.parseInt(context.getParameter("draw"));
        Integer start = Integer.parseInt(context.getParameter("start"));
        Integer length = Integer.parseInt(context.getParameter("length"));
        String search = context.getParameter("search[value]");

        List<Votd> votds = new ArrayList<>();
        Integer recordsTotal = 0;
        Integer recordsFiltered = 0;

        votds = votdDao.findAllWithLimit(start, length);
        List<String[]> votdData = controllerUtils.generateDataTableResults(votds);
        recordsTotal = votdData.size();
        recordsFiltered = recordsTotal;

        if(!StringUtils.isEmpty(search)){
            votds = votdDao.wildFind(search, start, length);
            votdData = controllerUtils.generateDataTableResults(votds);
            recordsFiltered = votdData.size();
        }

        Map<String, Object> votdMap = new HashMap<>();
        votdMap.put("draw", draw);
        votdMap.put("recordsTotal", recordsTotal);
        votdMap.put("recordsFiltered", recordsFiltered);
        votdMap.put("data", votdData);


        return Results
                .ok()
                .json()
                .render(votdMap);
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
        try {
            votd.setThemes(themeList);
            votdDao.save(votd);
            flashScope.success("Successfully created a new VoTD entry.");
        } catch (IllegalArgumentException e) {
            flashScope.error("Something strange has happened. Contact the administrator.");
        }
        return Results.redirect("/votd/create");

    }

    public Result updateVotd(@PathParam("verseid") Long verseid, FlashScope flashScope) {

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

    public Result saveVotdUpdate(Context context, FlashScope flashScope) {

        //Retrieve the themeIDs selected and convert to list of theme objects
        List<String> themeIds = context.getParameterValues("themes");

        List<Theme> themeList = new ArrayList<>();
        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeDao.findById(Long.parseLong(themeId));
                themeList.add(theme);
            }

        }

        String votdStatusString = context.getParameter("isApproved");

        //Set approval status
        boolean votdStatus = false;
        if (votdStatusString != null && votdStatusString.contentEquals("on")) {
            votdStatus = true;
        }

        try {
            Long votdId = Long.parseLong(context.getParameter("verseid"));
            votdDao.update(votdId, themeList, votdStatus);
        } catch (IllegalArgumentException | EntityDoesNotExistException e) {
            flashScope.error("The VOTD you're trying to update does not exist.");
            return Results.redirect("/votd/list");
        }

        flashScope.success("Verses successfully updated");
        return Results.redirect("/votd/list");
    }

    public Result approveVotd(@PathParam("votdid") Long votdId, FlashScope flashScope) {

        try {
            votdDao.approve(votdId);
            flashScope.success("Successfully approved VOTD.");
        } catch (IllegalArgumentException e) {
            flashScope.error("You must supply a valid votdid.");
        } catch (EntityDoesNotExistException e) {
            flashScope.error("You can't approve a votd that doesn't exist.");
        }

        return Results.redirect("/votd/list");
    }

    public Result deleteVotd(@PathParam("verseid") Long verseid, FlashScope flashScope) {

        try {
            votdDao.delete(verseid);
            flashScope.success("Successfully deleted Votd.");
        } catch (IllegalArgumentException e) {
            flashScope.error("You must supply a votd Id.");
        } catch (EntityDoesNotExistException e) {
            flashScope.error("Tried to delete a Votd that doesn't exist");
        }

        return Results.redirect("/votd/list");
    }

}