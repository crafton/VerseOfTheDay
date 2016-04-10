package utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.utils.NinjaProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

/**
 * Created by Crafton Williams on 27/03/2016.
 */
public class ControllerUtils {

    private String bibleSearchKey;
    private Integer maxVerses;

    final static Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    @Inject
    private ControllerUtils(NinjaProperties ninjaProperties) {
        Optional<Integer> optionalMaxVerses = Optional.ofNullable(ninjaProperties.getIntegerWithDefault("votd.maxverses", 0));
        if (optionalMaxVerses.isPresent()) {
            this.maxVerses = optionalMaxVerses.get();
        } else {
            this.maxVerses = 0;
        }

        Optional<String> optionalBibleKey = Optional.ofNullable(ninjaProperties.get("biblesearch.key"));
        if (optionalBibleKey.isPresent()) {
            this.bibleSearchKey = optionalBibleKey.get();
        } else {
            this.bibleSearchKey = "";
        }
    }

    /**
     * Retrieve the maximum number of verses allowed.
     *
     * @return
     */
    public Integer getMaxVerses() {
        return this.maxVerses;
    }

    /**
     * Get the verse length based on a verse range.
     *
     * @param verseRange of the format 'Matthew 6:24' or 'Matthew 6:24-28'
     * @return true of the number of verses in the verse range is less than maxVerses, false otherwise.
     */
    public boolean isVerseLengthValid(String verseRange) {

        /*Get string after the colon*/
        String verses = verseRange.split(":")[1];

        if (!verses.contains("-")) {
            return true;
        }

        /*Get numbers on either side of the -*/
        String[] verseArray = verses.split("-");
        String startVerseStr = verseArray[0];
        String endVerseStr = verseArray[1];

        /*Convert both numbers to integers and check if within range*/

        try {
            Integer startVerseInt = Integer.parseInt(startVerseStr);
            Integer endVerseInt = Integer.parseInt(endVerseStr);

            return this.maxVerses > (endVerseInt - startVerseInt);

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
    public boolean isVerseFormatValid(String verseRange) {
        return verseRange.matches("(\\d\\s)?\\w+\\s(\\d{1,2}):(\\d{1,3})(\\s?-\\s?\\d{1,3})?");
    }

    /**
     * Initiate a web service client to retrieve the verses passed in.
     *
     * @param verseRange
     * @return Verse text.
     */
    public String restGetVerses(String verseRange) {
        HttpAuthenticationFeature authenticationFeature = HttpAuthenticationFeature.basic(this.bibleSearchKey, "");
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.register(authenticationFeature)
                .target("https://bibles.org/v2/passages.js");

        String verseTextJson = webTarget
                .queryParam("q[]", verseRange)
                .queryParam("version", "eng-ESV")
                .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject verseJsonObject = parser.parse(verseTextJson).getAsJsonObject();
        return "<h3>" + verseRange + "</h3>" + verseJsonObject
                .getAsJsonObject("response")
                .getAsJsonObject("search")
                .getAsJsonObject("result")
                .getAsJsonArray("passages").get(0)
                .getAsJsonObject().get("text").getAsString();

    }

}
