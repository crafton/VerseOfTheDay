package services;

import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import ninja.NinjaDaoTestBase;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Crafton Williams on 2/06/2016.
 */
public class VotdDaoTest extends NinjaDaoTestBase {

    private VotdDao votdDao;
    private ThemeDao themeDao;
    private Votd votd;
    private List<Theme> themeList = new ArrayList<>();
    private Theme theme;

    @Before
    public void setup() {
        votdDao = getInstance(VotdDao.class);
        themeDao = getInstance(ThemeDao.class);

        votd = new Votd();
        votd.setVerses("Matthew 6:1-8");
        votd.setCreatedBy("John Smith");
        votd.setModifiedBy("Jack Thepumpkinking");

        theme = new Theme();
        theme.setThemeName("Faith");
        theme.setCreatedBy("John Smith");

        themeList.add(theme);
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

    @Test(expected = IllegalArgumentException.class)
    public void findByIdWhenIdisNull() throws Exception{
        votdDao.findById(null);
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

        themeDao.save(theme);
        votdDao.save(votd);

        assertNull(votd.getThemes());

        votdDao.update(1L, themeList, true);

        Votd votd1 = votdDao.findById(1L);

        assertEquals(1, votd1.getThemes().size());
    }

    @Test
    public void updateWithNullThemeList() throws Exception {

        themeDao.save(theme);
        votdDao.save(votd);

        assertNull(votd.getThemes());

        votdDao.update(1L, null, true);

        Votd votd1 = votdDao.findById(1L);

        assertEquals(0, votd1.getThemes().size());
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void updateWithFakeId() throws Exception {
        themeDao.save(theme);
        votdDao.save(votd);

        assertNull(votd.getThemes());

        votdDao.update(100L, themeList, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateWithNullId() throws Exception {
        themeDao.save(theme);
        votdDao.save(votd);

        assertNull(votd.getThemes());

        votdDao.update(null, themeList, true);
    }

    @Test
    public void delete() throws Exception {
        votdDao.save(votd);

        assertNotNull(votdDao.findById(1L));

        votdDao.delete(1L);

        assertNull(votdDao.findById(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteWithNullId() throws Exception {
        votdDao.delete(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void deleteWithFakeId() throws Exception {
        votdDao.delete(100L);
    }

    @Test
    public void approve() throws Exception {
        votdDao.save(votd);

        Votd v = votdDao.findById(1L);

        assertFalse(v.isApproved());

        votdDao.approve(1L);

        v = votdDao.findById(1L);

        assertTrue(v.isApproved());
    }

    @Test(expected = IllegalArgumentException.class)
    public void approveWithNullId() throws Exception {
        votdDao.approve(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void approveWithFakeId() throws Exception {
        votdDao.approve(100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfVotdIsNull() throws Exception{
        votdDao.save(null);
    }


}