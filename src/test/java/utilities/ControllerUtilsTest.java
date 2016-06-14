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
public class ControllerUtilsTest {

    @Inject
    ControllerUtils controllerUtils;
    String verses = "Matthew 6:1-5";
    String singleVerse = "Matthew 6:1";
    String badVerses = "Random String";
    String versesTooLong = "Matthew 6:1-20";
    String goodFormatBadVerse = "Bob 1:4-8";

    @Test
    public void verifyVerses() throws Exception {
        String result = controllerUtils.verifyVerses(verses);

        assertEquals("", result);

        String result2 = controllerUtils.verifyVerses(badVerses);

        assertTrue(result2.contains("Verse format of"));

        String result3 = controllerUtils.verifyVerses(versesTooLong);

        assertTrue(result3.contains("You can only select a maximum of"));

        String result4 = controllerUtils.verifyVerses("");

        assertTrue(result4.contains("A verse range must be submitted to proceed."));

        String result5 = controllerUtils.verifyVerses(goodFormatBadVerse);

        assertTrue(result5.contains("Verse(s) not found. Please ensure Book, Chapter and Verse are valid."));

        //Check if votd exists
        Votd votd = new Votd();
        votd.setVerses(verses);

        controllerUtils.votdDao.save(votd);

        String result6 = controllerUtils.verifyVerses(verses);

        assertTrue(result6.contains("already exists in the database."));

        controllerUtils.votdDao.delete(votd.getId());
    }

    @Test
    public void restGetVerses() throws Exception {
        String result = controllerUtils.restGetVerses(verses);

        assertNotNull(result);
    }

    @Test
    public void findClashes() throws Exception {
        Votd votd = new Votd();
        votd.setVerses(verses);

        controllerUtils.votdDao.save(votd);

        List<String> clashes = controllerUtils.findClashes(verses);

        assertEquals(1, clashes.size());

        controllerUtils.votdDao.delete(votd.getId());

        Votd votd1 = new Votd();
        votd.setVerses(singleVerse);

        controllerUtils.votdDao.save(votd1);

        List<String> clashes1 = controllerUtils.findClashes(singleVerse);

        assertEquals(1, clashes.size());


        controllerUtils.votdDao.delete(votd1.getId());

    }

    @Test
    public void formatListToHtml() throws Exception {
        List<String> items = new ArrayList<>();
        items.add("Spoon");
        items.add("fork");

        String html = controllerUtils.formatListToHtml(items);

        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>"));
    }

}