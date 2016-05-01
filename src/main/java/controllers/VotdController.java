package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import models.Votd;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ControllerUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
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

    public Result createVotd() {

        Result result = Results.html();

        List<String> themes = new ArrayList<>();
        themes.add("Love");
        themes.add("Faith");
        themes.add("Kindness");
        themes.add("Grace");

        result.render("themes", themes);

        return result;
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
    public Result saveVotd(Votd votd) {

        String verificationErrorMessage = controllerUtils.verifyVerses(votd.getVerses());

        if(!verificationErrorMessage.isEmpty()){
            return Results.badRequest().text().render(verificationErrorMessage);
        }

        EntityManager entityManager = entityManagerProvider.get();
        entityManager.persist(votd);

        return Results.redirect("/votd/create");

    }

}