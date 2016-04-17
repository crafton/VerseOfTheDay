package controllers;

import com.google.inject.Inject;
import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;
import ninja.params.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ControllerUtils;

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

        Result result = Results.html();

        Integer maxVerses = controllerUtils.getMaxVerses();

        if (maxVerses == 0) {
            logger.error("Max verses has not been set in application.conf");
            return result.text().render("An error has occurred. Contact the administrator to fix it.");
        }

        Optional<String> optionalVerses = Optional.ofNullable(verses);

        if (!optionalVerses.isPresent() || optionalVerses.get().contentEquals("")) {
            logger.warn("Client didn't submit a verse range to retrieve.");
            return result.text().render("A verse range must be submitted to proceed.");
        }
        String versesTrimmed = verses.trim();

        if (!controllerUtils.isVerseFormatValid(versesTrimmed)) {
            logger.warn("Verse format of '" + versesTrimmed + "' is incorrect.");

            return result.text().render("Verse format of '" + versesTrimmed + "' is incorrect.");
        }

        if (!controllerUtils.isVerseLengthValid(versesTrimmed)) {
            logger.info("You can only select a maximum of " + maxVerses + " verses.");
            return result.text().render("You can only select a maximum of " + maxVerses + " verses.");
        }

        /*Call web service to retrieve verses.*/
        String verseText = controllerUtils.restGetVerses(versesTrimmed);

        return result.text().render(verseText);
    }

    public Result saveVotd(){
        return Results.html();
    }

}