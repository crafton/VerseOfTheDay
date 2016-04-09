package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.VotdControllerUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;

/**
 * Created by Crafton Williams on 19/03/2016.
 */

@Singleton
public class VotdController {

    @Inject
    NinjaProperties ninjaProperties;

    final static Logger logger = LoggerFactory.getLogger(VotdController.class);

    public Result createVotd() {
        return Results.html();
    }

    public Result getVerse(@PathParam("verses") String verses) {

        Integer maxVerses = ninjaProperties.getIntegerWithDefault("votd.maxverses", 0);

        String versesTrimmed = verses.trim();
        Result result = Results.html();

        logger.info(versesTrimmed);

        if (!VotdControllerUtils.isVerseLengthValid(versesTrimmed, maxVerses)) {
            return result.text().render("You can only select a maximum of " + maxVerses + " verses.");
        }

        HttpAuthenticationFeature authenticationFeature = HttpAuthenticationFeature.basic(ninjaProperties.get("biblesearch.key"), "");
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.register(authenticationFeature)
                .target("https://bibles.org/v2/passages.js");

        String verseTextJson = webTarget
                .queryParam("q[]", versesTrimmed)
                .queryParam("version", "eng-ESV")
                .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject verseJsonObject = parser.parse(verseTextJson).getAsJsonObject();
        String verseText = verseJsonObject
                .getAsJsonObject("response")
                .getAsJsonObject("search")
                .getAsJsonObject("result")
                .getAsJsonArray("passages").get(0)
                .getAsJsonObject().get("text").getAsString();

        return result.text().render(verseText);
    }

}