package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
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

}