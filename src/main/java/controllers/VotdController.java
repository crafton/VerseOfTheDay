package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
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
    Provider<EntityManager> entityManagerProvider;
    @Inject
    Logger logger;

    @Transactional
    public Result createVotd() {
        EntityManager entityManager = entityManagerProvider.get();

        Query q = entityManager.createNamedQuery("Theme.findAll");
        List<Theme> themes = (List<Theme>)q.getResultList();

        return Results
                .ok()
                .html()
                .render("themes", themes);
    }

    public Result getVerse(@PathParam("verses") String verses) {

        String verificationErrorMessage = controllerUtils.verifyVerses(verses);

        if(!verificationErrorMessage.isEmpty()){
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

    @Transactional
    public Result saveVotd(Context context, Votd votd, FlashScope flashScope) {

        String verificationErrorMessage = controllerUtils.verifyVerses(votd.getVerses());

        if(!verificationErrorMessage.isEmpty()){
            flashScope.error(verificationErrorMessage);
            return Results.redirect("/votd/create");
        }

        List<String> themeIds = context.getParameterValues("themes");

        if(themeIds.isEmpty()){
            votd.setThemes(new ArrayList<Theme>());
        }

        EntityManager entityManager = entityManagerProvider.get();

        List<Theme> themeList = new ArrayList<>();
        for(String themeId : themeIds){
            Theme theme = entityManager.find(Theme.class, Long.parseLong(themeId));
            themeList.add(theme);
        }
        votd.setThemes(themeList);
        entityManager.persist(votd);

        flashScope.success("Successfully created a new VoTD entry.");
        return Results.redirect("/votd/create");

    }

}