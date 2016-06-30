package utilities;

import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import daos.VotdDao;
import models.Votd;
import ninja.Results;
import ninja.jpa.UnitOfWork;
import ninja.utils.MimeTypes;
import ninja.utils.NinjaProperties;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.swing.text.html.Option;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by Crafton Williams on 27/03/2016.
 */
public class ControllerUtils {

    @Inject
    Config config;

    @Inject
    VotdDao votdDao;

    final static Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    public ControllerUtils() {

    }


    public String verifyVerses(String verseSubmitted) {

        Integer maxVerses = config.getMaxVerses();

        if (maxVerses == 0) {
            logger.error("Max verses has not been set in application.conf");
            return "An error has occurred. Contact the administrator to fix it.";
        }

        Optional<String> optionalVerses = Optional.ofNullable(verseSubmitted);

        if (!optionalVerses.isPresent() || optionalVerses.get().contentEquals("")) {
            logger.warn("Client didn't submit a verse range to retrieve.");
            return "A verse range must be submitted to proceed.";
        }
        String versesTrimmed = verseSubmitted.trim();

        if (!isVerseFormatValid(versesTrimmed)) {
            logger.warn("Verse format of '" + versesTrimmed + "' is incorrect.");
            return "Verse format of '" + versesTrimmed + "' is incorrect.";
        }

        if (!isVerseLengthValid(versesTrimmed)) {
            logger.warn("You can only select a maximum of " + maxVerses + " verses.");
            return "You can only select a maximum of " + maxVerses + " verses.";
        }

        if (restGetVerses(versesTrimmed).isEmpty()) {
            return "Verse(s) not found. Please ensure Book, Chapter and Verse are valid.";
        }

        if (doesVotdExist(versesTrimmed)) {
            return "The verse '" + versesTrimmed + "' already exists in the database.";
        }

        return "";
    }

    /**
     * Get the verse length based on a verse range.
     *
     * @param verseRange of the format 'Matthew 6:24' or 'Matthew 6:24-28'
     * @return true of the number of verses in the verse range is less than maxVerses, false otherwise.
     */
    private boolean isVerseLengthValid(String verseRange) {

        if (!verseRange.contains("-")) {
            return true;
        }
        String[] verseArray = getVerseNumbers(verseRange);
        String startVerseStr = verseArray[0];
        String endVerseStr = verseArray[1];

        /*Convert both numbers to integers and check if within range*/

        try {
            Integer startVerseInt = Integer.parseInt(startVerseStr);
            Integer endVerseInt = Integer.parseInt(endVerseStr);

            return config.getMaxVerses() > Math.abs(endVerseInt - startVerseInt);

        } catch (NumberFormatException ne) {
            logger.info("Invalid integer formats submitted within verse range.");
            return false;
        }
    }

    /**
     * Validate the verse range.
     *
     * @param verseRange of the format 'Matthew 6:24' or 'Matthew 6:24-28'
     * @return
     */
    private boolean isVerseFormatValid(String verseRange) {

        if (verseRange.contains("-")) {
            String[] verses = getVerseNumbers(verseRange);

            String verseStart = verses[0];
            String verseEnd = verses[1];

            try {
                Integer verseStartInt = Integer.parseInt(verseStart);
                Integer verseEndInt = Integer.parseInt(verseEnd);

                if (verseStartInt >= verseEndInt) {
                    return false;
                }

            } catch (NumberFormatException ne) {
                logger.info("Invalid integer formats submitted within verse range.");
                return false;
            }
        }

        return verseRange.matches("(\\d\\s)?\\w+\\s(\\d{1,2}):(\\d{1,3})(\\S?-\\S?\\d{1,3})?");
    }

    private String[] getVerseNumbers(String verseRange) {
        /*Get string after the colon*/
        String verses = verseRange.split(":")[1];

        /*Get verse numbers -*/
        return verses.split("-");
    }

    /**
     * Initiate a web service client to retrieve the verses passed in.
     *
     * @param verseRange
     * @return Verse text.
     */
    public String restGetVerses(String verseRange) throws JsonSyntaxException {
        HttpAuthenticationFeature authenticationFeature = HttpAuthenticationFeature.basic(config.getBibleSearchKey(), "");
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.register(authenticationFeature)
                .target("https://bibles.org/v2/passages.js");

        String verseTextJson = webTarget
                .queryParam("q[]", verseRange)
                .queryParam("version", "eng-ESV")
                .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        JsonObject verseJsonObject;
        try {
            JsonParser parser = new JsonParser();
            verseJsonObject = parser.parse(verseTextJson).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

        JsonArray passages = verseJsonObject
                .getAsJsonObject("response")
                .getAsJsonObject("search")
                .getAsJsonObject("result")
                .getAsJsonArray("passages");

        /*Ensure verses are retrieved before returning anything.*/
        if (passages.size() == 0) {
            logger.warn("Biblesearch could not find verses matching the range supplied.");
            return "";
        }

        String verseTitle = passages.get(0)
                .getAsJsonObject()
                .get("display")
                .getAsString();

        String verseText = passages.get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();

        return "<h3>" + verseTitle + "</h3>" + verseText;

    }

    /**
     * @param accessToken
     * @return
     */
    public JsonObject auth0GetUser(String accessToken) throws JsonSyntaxException {
        Client client = ClientBuilder.newClient();
        String auth0User = client.target("https://" + config.getAuth0Domain() + "/userinfo")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject userJsonObject;
        try {
            userJsonObject = parser.parse(auth0User).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
        return userJsonObject;
    }

    /**
     * @param code
     * @return
     */
    public Map<String, String> auth0GetToken(String code) throws JsonSyntaxException {
        Client client = ClientBuilder.newClient();

        Map<String, String> auth0TokenRequest = new HashMap<>();
        auth0TokenRequest.put("client_id", config.getAuth0ClientId());
        auth0TokenRequest.put("client_secret", config.getAuth0ClientSecret());
        auth0TokenRequest.put("redirect_uri", config.getAuth0Callback());
        auth0TokenRequest.put("code", code);
        auth0TokenRequest.put("grant_type", "authorization_code");

        String auth0TokenRequestString = new Gson().toJson(auth0TokenRequest);

        Response response = client.target("https://" + config.getAuth0Domain() + "/oauth/token")
                .request()
                .post(Entity.entity(auth0TokenRequestString, MediaType.APPLICATION_JSON));

        JsonObject jsonResponse;
        try {
            JsonParser parser = new JsonParser();
            jsonResponse = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

        String accessToken = jsonResponse
                .getAsJsonObject()
                .get("access_token")
                .getAsString();

        String idToken = jsonResponse
                .getAsJsonObject()
                .get("id_token")
                .getAsString();

        Map<String, String> userTokens = new HashMap<>();
        userTokens.put("access_token", accessToken);
        userTokens.put("id_token", idToken);

        return userTokens;
    }

    /**
     * Find all verses in the database that intersect with verses provided.
     *
     * @param verseToMatch
     * @return list of verses that intersect with the given range.
     */
    public List<String> findClashes(String verseToMatch) {
        String[] chapterVerseArray = verseToMatch.split(":");
        String bookChapter = chapterVerseArray[0];
        String verseRange = chapterVerseArray[1];
        List<String> potentialClashes = getMatchCandidates(bookChapter);
        List<String> actualClashes = new ArrayList<>();

        if (potentialClashes.isEmpty()) {
            return actualClashes;
        }

        for (String potentialClash : potentialClashes) {
            if (doesVerseRangeIntersect(verseRange, potentialClash.split(":")[1])) {
                actualClashes.add(potentialClash);
            }
        }

        return actualClashes;
    }

    /**
     * Format a list of strings to an html list
     *
     * @param items
     * @return an html representation of a java list.
     */
    public String formatListToHtml(List<String> items) {

        String formattedList = "";

        for (String item : items) {
            formattedList += "<li>" + item + "</li>";
        }

        return "<p><ul>" + formattedList + "</ul></p>";
    }

    /**
     * Get all verses in the database that match the chapter provided.
     *
     * @param bookChapter A chapter in the bible
     * @return a list of verse ranges that already exist for the given chapter.
     */
    private List<String> getMatchCandidates(String bookChapter) {

        return votdDao.findVersesInChapter(bookChapter);
    }

    /**
     * @param verse
     * @return
     */
    private boolean doesVotdExist(String verse) {

        try {
            votdDao.findByVerse(verse);
            logger.info("Verse already exists in the database.");
            return true;
        } catch (NoResultException nr) {
            return false;
        }
    }

    /**
     * Given two sets of verse ranges or individual verses, determine if they intersect.
     *
     * @param range1 A string representation of a verse range separated by a '-'. Or a single verse
     *               without a dash.
     * @param range2 A string representation of a verse range separated by a '-'. Or a single verse
     *               without a dash.
     * @return whether or not the ranges intersect.
     */
    private boolean doesVerseRangeIntersect(String range1, String range2) {
        String[] range1Array;
        String[] range2Array;
        Integer range1Lower;
        Integer range1Upper;
        Integer range2Lower;
        Integer range2Upper;

        if (range1.contains("-")) {
            range1Array = range1.split("-");
        } else {
            range1Array = new String[]{range1, range1};
        }

        if (range2.contains("-")) {
            range2Array = range2.split("-");
        } else {
            range2Array = new String[]{range2, range2};
        }

        try {
            range1Lower = Integer.parseInt(range1Array[0]);
            range1Upper = Integer.parseInt(range1Array[1]);
            range2Lower = Integer.parseInt(range2Array[0]);
            range2Upper = Integer.parseInt(range2Array[1]);

            if (range1Lower <= range2Upper && range2Lower <= range1Upper) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException nex) {
            logger.error("Problem with number range format. Verses don't appear to be integers.");
            return false;
        }

    }

    /**
     * Reformat query results to what DataTables expects. Ensure fields are added to the
     * array in the same order as the columns headings are displayed.
     *
     * @param votds
     * @return
     */
    public List<String[]> generateDataTableResults(List<Votd> votds){

        String[] votdFields = new String[0];
        List<String[]> votdData = new ArrayList<>();

        for (Votd votd : votds) {
            String votdApproved = "";
            String shouldApproveVotd = "";
            if (votd.isApproved()) {
                votdApproved = config.APPROVED;
            } else {
                shouldApproveVotd = "<a class=\"fa fa-thumbs-up\" href=\"/votd/approve/" + votd.getId() + "\" aria-hidden=\"true\"></a>";
                votdApproved = config.PENDING;
            }

            votdFields = new String[]{votd.getVerses(), votd.getThemesAsString(), votdApproved,
                    shouldApproveVotd, votd.getCreatedBy(), votd.getModifiedBy(),
                    "<a class=\"fa fa-trash\" data-placement=\"top\" data-toggle=\"confirmation\" aria-hidden=\"true\" href=\"/votd/delete/" + votd.getId() + "\"></a>",
                    "<a class=\"fa fa-pencil\" aria-hidden=\"true\" href=\"/votd/update/" + votd.getId() + "\"></a>"};

            votdData.add(votdFields);
        }

        return votdData;
    }

}
