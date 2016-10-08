package controllers;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import exceptions.EntityDoesNotExistException;
import filters.ContributorFilter;
import filters.LoginFilter;
import filters.PublisherFilter;
import models.Theme;
import models.User;
import models.Votd;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ThemeRepository;
import services.UserService;
import services.VotdService;
import utilities.Config;
import utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@FilterWith(LoginFilter.class)
public class VotdController {

    private final static Logger logger = LoggerFactory.getLogger(VotdController.class);

    private Utils utils;
    private final VotdService votdService;
    private final ThemeRepository themeRepository;
    private final Provider<Mail> mailProvider;
    private final Postoffice postoffice;
    private final UserService userService;
    private final Config config;
    private static final String THEMES = "themes";
    private static final String listPath = "/votd/list";

    @Inject
    public VotdController(Utils utils, VotdService votdService,
                          ThemeRepository themeRepository,
                          Provider<Mail> mailProvider, Postoffice postoffice,
                          UserService userService, Config config) {
        this.utils = utils;
        this.votdService = votdService;
        this.themeRepository = themeRepository;
        this.mailProvider = mailProvider;
        this.postoffice = postoffice;
        this.userService = userService;
        this.config = config;
    }

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
        List<Theme> themes = themeRepository.findAll();

        return Results
                .ok()
                .html()
                .render(THEMES, themes);
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
        String versesRetrieved = votdService.restGetVerses(versesTrimmed, "");

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
    public Result saveVotd(Context context, Votd votd, FlashScope flashScope, Session session) {

        String verificationErrorMessage = votdService.verifyVerses(votd.getVerses());

        if (!verificationErrorMessage.isEmpty()) {
            flashScope.error(verificationErrorMessage);
            return Results.redirect("/votd/create");
        }

        User user = userService.getCurrentUser(session.get(config.IDTOKEN_NAME));

        votd.setCreatedBy(user.getName());

        //Retrieve the themeIDs selected and convert to list of themes
        List<String> themeIds = context.getParameterValues(THEMES);

        if (themeIds.isEmpty()) {
            votd.setThemes(new ArrayList<Theme>());
        }

        List<Theme> themeList = new ArrayList<>();
        for (String themeId : themeIds) {
            Theme theme = themeRepository.findById(Long.parseLong(themeId));
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
            return Results.redirect(listPath);
        }

        Votd votd = votdService.findById(verseid);

        //Get all themes
        List<Theme> themes = themeRepository.findAll();

        if (votd == null) {
            flashScope.error("Tried to retrieve a Votd that doesn't exist.");
            return Results.redirect(listPath);
        }

        try {
            //Get verse text
            String verseText = votdService.restGetVerses(votd.getVerses(), "");

            return Results
                    .ok()
                    .html()
                    .render("votd", votd)
                    .render(THEMES, themes)
                    .render("verseText", verseText);
        } catch (JsonSyntaxException e) {
            flashScope.error("Could not retrieve the requested votd.");
            logger.error("CFailed web service call to retrieve verses.");
            return Results.redirect(listPath);
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
    public Result saveVotdUpdate(Context context, FlashScope flashScope, Session session) {

        //Retrieve the themeIDs selected and convert to list of theme objects
        List<String> themeIds = context.getParameterValues(THEMES);

        List<Theme> themeList = new ArrayList<>();
        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeRepository.findById(Long.parseLong(themeId));
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

            User user = userService.getCurrentUser(session.get(config.IDTOKEN_NAME));

            votdService.update(votdId, themeList, votdStatus, user.getName());
        } catch (IllegalArgumentException | EntityDoesNotExistException e) {
            flashScope.error("The VOTD you're trying to update does not exist.");
            return Results.redirect(listPath);
        }

        flashScope.success("Verses successfully updated");
        return Results.redirect(listPath);
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

        return Results.redirect(listPath);
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

        return Results.redirect(listPath);
    }

    private void sendVotdContributedEmail() {


    }

}