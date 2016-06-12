package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import ninja.NinjaDaoTestBase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Crafton Williams on 2/06/2016.
 */
public class ThemeDaoTest extends NinjaDaoTestBase {

    private VotdDao votdDao;
    private ThemeDao themeDao;
    private Theme theme;


    @Before
    public void setup() {
        themeDao = getInstance(ThemeDao.class);

        theme = new Theme();
        theme.setThemeName("Love");
        theme.setCreatedBy("John Smith");

        List<Theme> themes = new ArrayList<>();

        themes.add(theme);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByIdWhenIdIsNull() throws Exception{
        themeDao.findById(null);
    }

    @Test
    public void findAll() throws Exception {
        themeDao.save(theme);
        List<Theme> themesList = themeDao.findAll();

        assertEquals(1, themesList.size());
        assertEquals(themesList.get(0).getThemeName(), "Love");
    }

    @Test
    public void findAllWithEmptyDB() throws Exception {
        List<Theme> themeList = themeDao.findAll();

        assertEquals(0, themeList.size());
    }

    @Test
    public void findByName() throws Exception {
        themeDao.save(theme);
        String themeName = themeDao.findByName("Love");

        assertEquals("Love", themeName);
    }

    @Test(expected = NoResultException.class)
    public void findByNameWithBadName() throws Exception {
        themeDao.findByName("Some fictitious name");
    }

    @Test
    public void findById() throws Exception {
        Theme t = themeDao.findById(100L);

        assertNull(t);

        themeDao.save(theme);

        Theme t2 = themeDao.findById(1L);

        assertEquals(theme.getThemeName(), t2.getThemeName());
    }

    @Test
    public void delete() throws Exception {
        themeDao.save(theme);

        Theme t = themeDao.findById(1L);

        assertEquals(theme.getThemeName(), t.getThemeName());

        themeDao.delete(1L);

        Theme t1 = themeDao.findById(1L);

        assertNull(t1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfThemeIsNull() throws Exception{
        themeDao.save(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfThemeNameisEmpty() throws Exception{
        Theme theme = new Theme();
        theme.setThemeName("");
        theme.setCreatedBy("John Tom");
        themeDao.save(theme);
    }
    @Test(expected = EntityAlreadyExistsException.class)
    public void saveThemeAlreadyExists() throws Exception{
        themeDao.save(theme);

        themeDao.save(theme);
    }


    @Test(expected = IllegalArgumentException.class)
    public void deleteThemeWithNullID() throws Exception{
        themeDao.delete(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void deleteThemeWithNonExistent() throws Exception {
        themeDao.delete(1L);
    }

    @Test(expected = EntityBeingUsedException.class)
    public void deleteThemeBeingUsed() throws Exception{

        themeDao.save(theme);

        votdDao = getInstance(VotdDao.class);
        Votd votd = new Votd();
        votd.setVerses("Matthew 6:1-8");
        votd.setCreatedBy("John Smith");
        votd.setModifiedBy("Jack Thepumpkinking");
        List<Theme> themeList = new ArrayList<>();
        themeList.add(theme);

        votd.setThemes(themeList);
        votdDao.save(votd);

        themeDao.delete(1L);
    }

}