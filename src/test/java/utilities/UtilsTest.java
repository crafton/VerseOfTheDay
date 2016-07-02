package utilities;

import com.google.inject.Inject;
import models.Votd;
import ninja.NinjaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Crafton Williams on 5/06/2016.
 */
@RunWith(NinjaRunner.class)
public class UtilsTest {

    @Inject
    Utils utils;
    String verses = "Matthew 6:1-5";
    String singleVerse = "Matthew 6:1";
    String badVerses = "Random String";
    String versesTooLong = "Matthew 6:1-20";
    String goodFormatBadVerse = "Bob 1:4-8";

    @Test
    public void verifyVerses() throws Exception {
        String result = utils.verifyVerses(verses);

        assertEquals("", result);

        String result2 = utils.verifyVerses(badVerses);

        assertTrue(result2.contains("Verse format of"));

        String result3 = utils.verifyVerses(versesTooLong);

        assertTrue(result3.contains("You can only select a maximum of"));

        String result4 = utils.verifyVerses("");

        assertTrue(result4.contains("A verse range must be submitted to proceed."));

        String result5 = utils.verifyVerses(goodFormatBadVerse);

        assertTrue(result5.contains("Verse(s) not found. Please ensure Book, Chapter and Verse are valid."));

        //Check if votd exists
        Votd votd = new Votd();
        votd.setVerses(verses);

        utils.votdDao.save(votd);

        String result6 = utils.verifyVerses(verses);

        assertTrue(result6.contains("already exists in the database."));

        utils.votdDao.delete(votd.getId());
    }

    @Test
    public void restGetVerses() throws Exception {
        String result = utils.restGetVerses(verses);

        assertNotNull(result);
    }

    @Test
    public void findClashes() throws Exception {
        Votd votd = new Votd();
        votd.setVerses(verses);

        utils.votdDao.save(votd);

        List<String> clashes = utils.findClashes(verses);

        assertEquals(1, clashes.size());

        utils.votdDao.delete(votd.getId());

        Votd votd1 = new Votd();
        votd.setVerses(singleVerse);

        utils.votdDao.save(votd1);

        List<String> clashes1 = utils.findClashes(singleVerse);

        assertEquals(1, clashes.size());


        utils.votdDao.delete(votd1.getId());

    }

    @Test
    public void formatListToHtml() throws Exception {
        List<String> items = new ArrayList<>();
        items.add("Spoon");
        items.add("fork");

        String html = utils.formatListToHtml(items);

        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>"));
    }

}