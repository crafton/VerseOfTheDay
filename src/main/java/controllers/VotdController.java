package controllers;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import org.apache.commons.mail.EmailException;
import services.ThemeService;
import services.UserService;
import services.VotdService;
import exceptions.EntityDoesNotExistException;
import filters.ContributorFilter;
import filters.LoginFilter;
import filters.PublisherFilter;
import models.Theme;
import models.Votd;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import utilities.Config;
import utilities.Utils;

import javax.mail.internet.AddressException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@FilterWith(LoginFilter.class)
public class VotdController {

    @Inject
    private Utils utils;
    @Inject
    private VotdService votdService;
    @Inject
    private ThemeService themeService;
    @Inject
    private Logger logger;
    @Inject
    private Provider<Mail> mailProvider;
    @Inject
    private Postoffice postoffice;
    @Inject
    private UserService userService;
    @Inject
    private Config config;


    /**
     * Display all votds in the database
     *
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result viewVotds() {

        return Results
                .ok()
                .html();
    }

    /**
     * Display all votds in the database with an optional filter.
     *
     * @param context
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result displayVotdData(Context context) {

        try {
            Integer draw = Integer.parseInt(context.getParameter("draw"));
            Integer start = Integer.parseInt(context.getParameter("start"));
            Integer length = Integer.parseInt(context.getParameter("length"));
            String search = context.getParameter("search[value]");

            Integer recordsTotal = votdService.getTotalRecords().intValue();

        /*Retrieve records and build array of data to return*/
            List<Votd> votds = votdService.wildFind(search, start, length);
            Integer recordsFiltered = votdService.countFilteredRecords(search).intValue();
            List<String[]> votdData = votdService.generateDataTableResults(votds);

        /*Format data for ajax callback processing*/
            Map<String, Object> votdMap = new HashMap<>();
            votdMap.put("draw", draw);
            votdMap.put("recordsTotal", recordsTotal);
            votdMap.put("recordsFiltered", recordsFiltered);
            votdMap.put("data", votdData);


            return Results
                    .ok()
                    .json()
                    .render(votdMap);
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage());
            return Results.badRequest().json();
        }
    }


    /**
     * Render view to create a new votd
     *
     * @return
     */
    @FilterWith(ContributorFilter.class)
    public Result createVotd() {
        List<Theme> themes = themeService.findAll();

        return Results
                .ok()
                .html()
                .render("themes", themes);
    }

    /**
     * Retrieve the full versetext from the web service given a verse range
     *
     * @param verses verses to retrieve
     * @return
     */
    @FilterWith(ContributorFilter.class)
    public Result getVerse(@PathParam("verses") String verses) {

        String verificationErrorMessage = votdService.verifyVerses(verses);

        if (!verificationErrorMessage.isEmpty()) {
            return Results.badRequest().text().render(verificationErrorMessage);
        }

        String versesTrimmed = verses.trim();

        /*Call web service to retrieve verses.*/
        String versesRetrieved = votdService.restGetVerses(versesTrimmed);

        /*Find all verses that clash with what we're trying to add to the database*/
        List<String> verseClashes = votdService.findClashes(versesTrimmed);
        if (!verseClashes.isEmpty()) {
            versesRetrieved += "<h4 id='clash' class='text-danger'>Verse Clashes</h4>" +
                    "<small>Verses that already exist in the database which " +
                    "intersect with the verses being entered.</small><p></p>"
                    + utils.formatListToHtml(verseClashes);
        }

        return Results.ok().text().render(versesRetrieved);
    }

    /**
     * Save a new Votd to the database
     *
     * @param context
     * @param votd
     * @param flashScope
     * @return
     */
    @FilterWith(ContributorFilter.class)
    public Result saveVotd(Context context, Votd votd, FlashScope flashScope) {

        String verificationErrorMessage = votdService.verifyVerses(votd.getVerses());

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
            Theme theme = themeService.findById(Long.parseLong(themeId));
            themeList.add(theme);
        }
        try {
            votd.setThemes(themeList);
            votdService.save(votd);
            //If the votd was saved by a contributor, send a notification
            String idToken = context.getSession().get("idToken");
            if (userService.hasRole(idToken, config.getContributorRole())) {
                sendVotdContributedEmail();
            }
            flashScope.success("Successfully created a new VoTD entry.");
        } catch (IllegalArgumentException e) {
            flashScope.error("Something strange has happened. Contact the administrator.");
        }
        return Results.redirect("/votd/create");

    }

    /**
     * Update an existing votd associated with the supplied verseid
     *
     * @param verseid
     * @param flashScope
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result updateVotd(@PathParam("verseid") Long verseid, FlashScope flashScope) {

        if (verseid == null) {
            flashScope.error("You must supply a valid verse Id.");
            return Results.redirect("/votd/list");
        }

        Votd votd = votdService.findById(verseid);

        //Get all themes
        List<Theme> themes = themeService.findAll();

        if (votd == null) {
            flashScope.error("Tried to retrieve a Votd that doesn't exist.");
            return Results.redirect("/votd/list");
        }

        try {
            //Get verse text
            String verseText = votdService.restGetVerses(votd.getVerses());

            return Results
                    .ok()
                    .html()
                    .render("votd", votd)
                    .render("themes", themes)
                    .render("verseText", verseText);
        } catch (JsonSyntaxException e) {
            flashScope.error("Could not retrieve the requested votd.");
            logger.error("CFailed web service call to retrieve verses.");
            return Results.redirect("/votd/list");
        }
    }

    /**
     * Save an updated votd record
     *
     * @param context
     * @param flashScope
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result saveVotdUpdate(Context context, FlashScope flashScope) {

        //Retrieve the themeIDs selected and convert to list of theme objects
        List<String> themeIds = context.getParameterValues("themes");

        List<Theme> themeList = new ArrayList<>();
        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeService.findById(Long.parseLong(themeId));
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
            votdService.update(votdId, themeList, votdStatus);
        } catch (IllegalArgumentException | EntityDoesNotExistException e) {
            flashScope.error("The VOTD you're trying to update does not exist.");
            return Results.redirect("/votd/list");
        }

        flashScope.success("Verses successfully updated");
        return Results.redirect("/votd/list");
    }

    /**
     * Approve a submitted Votd
     *
     * @param votdId
     * @param flashScope
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result approveVotd(@PathParam("votdid") Long votdId, FlashScope flashScope) {

        try {
            votdService.approve(votdId);
            flashScope.success("Successfully approved VOTD.");
        } catch (IllegalArgumentException e) {
            flashScope.error("You must supply a valid votdid.");
        } catch (EntityDoesNotExistException e) {
            flashScope.error("You can't approve a votd that doesn't exist.");
        }

        return Results.redirect("/votd/list");
    }

    /**
     * Delete a votd
     *
     * @param verseid
     * @param flashScope
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result deleteVotd(@PathParam("verseid") Long verseid, FlashScope flashScope) {

        try {
            votdService.delete(verseid);
            flashScope.success("Successfully deleted Votd.");
        } catch (IllegalArgumentException e) {
            flashScope.error("You must supply a votd Id.");
        } catch (EntityDoesNotExistException e) {
            flashScope.error("Tried to delete a Votd that doesn't exist");
        }

        return Results.redirect("/votd/list");
    }

    private void sendVotdContributedEmail() {
        Mail mail = mailProvider.get();

        mail.setSubject(config.getContributedVotdMailSubject());
        mail.setFrom(config.getContributedVotdMailFrom());
        mail.addTo(config.getContributedVotdAddress());
        mail.setBodyHtml(config.getContributedVotdMailHtmlBody());
        mail.setBodyText(config.getContributedVotdMailTextBody());

        try {
            postoffice.send(mail);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

}