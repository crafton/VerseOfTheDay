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


public class VotdServiceTest extends NinjaDaoTestBase {

    private VotdService votdService;
    private ThemeService themeService;
    private Votd votd;
    private List<Theme> themeList = new ArrayList<>();
    private Theme theme;

    @Before
    public void setup() {
        votdService = getInstance(VotdService.class);
        themeService = getInstance(ThemeService.class);

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
        votdService.save(votd);

        List<Votd> votdList = votdService.findAll();

        assertEquals(1, votdList.size());
        assertEquals(votd.getVerses(), votdList.get(0).getVerses());
    }

    @Test
    public void findById() throws Exception {
        votdService.save(votd);

        Votd v = votdService.findById(1L);

        assertEquals(votd.getVerses(), v.getVerses());

        Votd v1 = votdService.findById(2L);

        assertNull(v1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByIdWhenIdisNull() throws Exception{
        votdService.findById(null);
    }

    @Test
    public void findByVerse() throws Exception {
        votdService.save(votd);

        String verse = votdService.findByVerse(votd.getVerses());

        assertEquals(verse, votd.getVerses());
    }

    @Test(expected = NoResultException.class)
    public void findByBadVerse() throws Exception {
        votdService.findByVerse("Some random verse");
    }

    @Test
    public void findVersesInChapter() throws Exception {
        votdService.save(votd);

        List<String> versesInChapter = votdService.findVersesInChapter("Matthew 6");

        assertEquals(versesInChapter.get(0), votd.getVerses());

        List<String> versesNotInChapter = votdService.findVersesInChapter("Random");

        assertEquals(0, versesNotInChapter.size());
    }

    @Test
    public void update() throws Exception {

        themeService.saveTheme(theme);
        votdService.save(votd);

        assertNull(votd.getThemes());

        votdService.update(1L, themeList, true);

        Votd votd1 = votdService.findById(1L);

        assertEquals(1, votd1.getThemes().size());
    }

    @Test
    public void updateWithNullThemeList() throws Exception {

        themeService.saveTheme(theme);
        votdService.save(votd);

        assertNull(votd.getThemes());

        votdService.update(1L, null, true);

        Votd votd1 = votdService.findById(1L);

        assertEquals(0, votd1.getThemes().size());
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void updateWithFakeId() throws Exception {
        themeService.saveTheme(theme);
        votdService.save(votd);

        assertNull(votd.getThemes());

        votdService.update(100L, themeList, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateWithNullId() throws Exception {
        themeService.saveTheme(theme);
        votdService.save(votd);

        assertNull(votd.getThemes());

        votdService.update(null, themeList, true);
    }

    @Test
    public void delete() throws Exception {
        votdService.save(votd);

        assertNotNull(votdService.findById(1L));

        votdService.delete(1L);

        assertNull(votdService.findById(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteWithNullId() throws Exception {
        votdService.delete(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void deleteWithFakeId() throws Exception {
        votdService.delete(100L);
    }

    @Test
    public void approve() throws Exception {
        votdService.save(votd);

        Votd v = votdService.findById(1L);

        assertFalse(v.isApproved());

        votdService.approve(1L);

        v = votdService.findById(1L);

        assertTrue(v.isApproved());
    }

    @Test(expected = IllegalArgumentException.class)
    public void approveWithNullId() throws Exception {
        votdService.approve(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void approveWithFakeId() throws Exception {
        votdService.approve(100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfVotdIsNull() throws Exception{
        votdService.save(null);
    }


}