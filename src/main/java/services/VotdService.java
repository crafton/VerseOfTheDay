package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.VotdRepository;
import utilities.Config;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VotdService {

    private static final Logger logger = LoggerFactory.getLogger(VotdService.class);

    private final Config config;
    private final VotdRepository votdRepository;

    @Inject
    public VotdService(Config config, VotdRepository votdRepository) {
        this.config = config;
        this.votdRepository = votdRepository;
    }

    public List<Votd> findAll() {
        return votdRepository.findAllVerses();
    }

    public Votd findById(Long votdId) throws IllegalArgumentException {

        if (votdId == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }
        return votdRepository.findVerseById(votdId);
    }

    public String findByVerse(String verseName) throws NoResultException {
        return votdRepository.findVerseByVerseName(verseName);
    }

    public List<String> findVersesInChapter(String bookAndChapter) {
        return votdRepository.findVersesInChapter(bookAndChapter);
    }

    public List<Votd> wildFind(String param, Integer start, Integer length) {
        return votdRepository.search(param, start, length);
    }

    public Long countFilteredRecords(String param) throws NoResultException {
        return votdRepository.countRecordsWithFilter(param);
    }

    public Long getTotalRecords() throws NoResultException {
        return votdRepository.countTotalRecords();
    }

    public List<Votd> findAllWithLimit(Integer start, Integer length) {
        return votdRepository.findAllWithLimit(start, length);
    }

    public void update(Long votdId, List<Theme> themes, boolean votdStatus, String updatedBy)
            throws IllegalArgumentException, EntityDoesNotExistException {

        votdRepository.update(votdId, themes, votdStatus, updatedBy);
    }

    public void approve(Long votdId) throws IllegalArgumentException, EntityDoesNotExistException {

        votdRepository.approve(votdId);
    }

    public void save(Votd votd) throws IllegalArgumentException {

        votdRepository.save(votd);
    }

    public void delete(Long votdId) throws IllegalArgumentException, EntityDoesNotExistException {

        votdRepository.delete(votdId);
    }

    /**
     * Reformat query results to what DataTables expects. Ensure fields are added to the
     * array in the same order as the columns headings are displayed.
     *
     * @param votds
     * @return
     */
    public List<String[]> generateDataTableResults(List<Votd> votds) {

        String[] votdFields = new String[0];
        List<String[]> votdData = new ArrayList<>();

        for (Votd votd : votds) {
            String votdApproved = "";
            String shouldApproveVotd = "";
            if (votd.isApproved()) {
                votdApproved = config.APPROVED;
            } else {
                shouldApproveVotd = "<a class=\"material-icons\" href=\"/votd/approve/" + votd.getId() + "\" aria-hidden=\"true\">thumb_up</a>";
                votdApproved = config.PENDING;
            }

            votdFields = new String[]{votd.getVerses(), votd.getThemesAsString(), votdApproved,
                    shouldApproveVotd, votd.getCreatedBy(), votd.getModifiedBy(),
                    "<a class=\"material-icons\" data-placement=\"top\" data-toggle=\"confirmation\" aria-hidden=\"true\" href=\"/votd/delete/" + votd.getId() + "\">delete</a>",
                    "<a class=\"material-icons\" aria-hidden=\"true\" href=\"/votd/update/" + votd.getId() + "\">mode_edit</a>"};

            votdData.add(votdFields);
        }

        return votdData;
    }

    /**
     * Initiate a web service client to retrieve the verses passed in.
     *
     * @param verseRange
     * @return Verse text.
     */
    public String restGetVerses(String verseRange, String version) throws JsonSyntaxException {

        JsonObject verseJsonObject = votdRepository.findVersesByRange(verseRange, version);

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
     * Ensure verses supplied are valid.
     *
     * @param verseSubmitted
     * @return
     */
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

        if (restGetVerses(versesTrimmed, "").isEmpty()) {
            return "Verse(s) not found. Please ensure Book, Chapter and Verse are valid.";
        }

        if (doesVotdExist(versesTrimmed)) {
            return "The verse '" + versesTrimmed + "' already exists in the database.";
        }

        return "";
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
        List<String> potentialClashes = findVersesInChapter(bookChapter);
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
     * Checks the database to see if the VOTD already exists
     *
     * @param verse
     * @return
     */
    private boolean doesVotdExist(String verse) {

        try {
            findByVerse(verse);
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

            return range1Lower <= range2Upper && range2Lower <= range1Upper;

        } catch (NumberFormatException nex) {
            logger.error("Problem with number range format. Verses don't appear to be integers.");
            return false;
        }

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

        return verseRange.matches("(\\d\\s)?\\w+\\s(\\d{1,3}):(\\d{1,3})(\\S?-\\S?\\d{1,3})?");
    }

    /**
     * Given a verseRange get the verse numbers
     *
     * @param verseRange
     * @return
     */
    private String[] getVerseNumbers(String verseRange) {
        /*Get string after the colon*/
        String verses = verseRange.split(":")[1];

        /*Get verse numbers -*/
        return verses.split("-");
    }

}