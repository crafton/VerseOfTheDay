package daos;

import models.Theme;
import models.Votd;
import ninja.NinjaDaoTestBase;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Crafton Williams on 2/06/2016.
 */
public class VotdDaoTest extends NinjaDaoTestBase {

    private VotdDao votdDao;
    private Votd votd;
    private List<Theme> themeList;
    private Theme theme;

    @Before
    public void setup() {
        votdDao = getInstance(VotdDao.class);

        votd = new Votd();
        votd.setVerses("Matthew 6:1-8");
        votd.setCreatedBy("John Smith");
        votd.setApprovedBy("Jack Thepumpkinking");

        Theme theme = new Theme();
        theme.setThemeName("Faith");
        theme.setCreatedBy("John Smith");
    }

    @Test
    public void findAll() throws Exception {
        votdDao.save(votd);

        List<Votd> votdList = votdDao.findAll();

        assertEquals(1, votdList.size());
        assertEquals(votd.getVerses(), votdList.get(0).getVerses());
    }

    @Test
    public void findById() throws Exception {
        votdDao.save(votd);

        Votd v = votdDao.findById(1L);

        assertEquals(votd.getVerses(), v.getVerses());

        Votd v1 = votdDao.findById(2L);

        assertNull(v1);
    }

    @Test
    public void findByVerse() throws Exception {
        votdDao.save(votd);

        String verse = votdDao.findByVerse(votd.getVerses());

        assertEquals(verse, votd.getVerses());
    }

    @Test(expected = NoResultException.class)
    public void findByBadVerse() throws Exception {
        votdDao.findByVerse("Some random verse");
    }

    @Test
    public void findVersesInChapter() throws Exception {
        votdDao.save(votd);

        List<String> versesInChapter = votdDao.findVersesInChapter("Matthew 6");

        assertEquals(versesInChapter.get(0), votd.getVerses());

        List<String> versesNotInChapter = votdDao.findVersesInChapter("Random");

        assertEquals(0, versesNotInChapter.size());
    }

    @Test
    public void update() throws Exception {

        votdDao.save(votd);

        assertEquals(0, votd.getThemes().size());

        themeList.add(theme);

        votdDao.update(1L, themeList);

        assertEquals(1, votd.getThemes().size());
    }

    @Test
    public void save() throws Exception {

    }

    @Test
    public void delete() throws Exception {

    }

}