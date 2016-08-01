package services;

import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
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


public class ThemeServiceTest extends NinjaDaoTestBase {

    private VotdService votdService;
    private ThemeService themeService;
    private Theme theme;


    @Before
    public void setup() {
        themeService = getInstance(ThemeService.class);

        theme = new Theme();
        theme.setThemeName("Love");
        theme.setCreatedBy("John Smith");

        List<Theme> themes = new ArrayList<>();

        themes.add(theme);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByIdWhenIdIsNull() throws Exception{
        themeService.findThemeById(null);
    }

    @Test
    public void findAll() throws Exception {
        themeService.saveTheme(theme);
        List<Theme> themesList = themeService.findAllThemes();

        assertEquals(1, themesList.size());
        assertEquals(themesList.get(0).getThemeName(), "Love");
    }

    @Test
    public void findAllWithEmptyDB() throws Exception {
        List<Theme> themeList = themeService.findAllThemes();

        assertEquals(0, themeList.size());
    }

    @Test
    public void findByName() throws Exception {
        themeService.saveTheme(theme);
        String themeName = themeService.findThemeByName("Love");

        assertEquals("Love", themeName);
    }

    @Test(expected = NoResultException.class)
    public void findByNameWithBadName() throws Exception {
        themeService.findThemeByName("Some fictitious name");
    }

    @Test
    public void findById() throws Exception {
        Theme t = themeService.findThemeById(100L);

        assertNull(t);

        themeService.saveTheme(theme);

        Theme t2 = themeService.findThemeById(1L);

        assertEquals(theme.getThemeName(), t2.getThemeName());
    }

    @Test
    public void delete() throws Exception {
        themeService.saveTheme(theme);

        Theme t = themeService.findThemeById(1L);

        assertEquals(theme.getThemeName(), t.getThemeName());

        themeService.deleteTheme(1L);

        Theme t1 = themeService.findThemeById(1L);

        assertNull(t1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfThemeIsNull() throws Exception{
        themeService.saveTheme(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveIfThemeNameisEmpty() throws Exception{
        Theme theme = new Theme();
        theme.setThemeName("");
        theme.setCreatedBy("John Tom");
        themeService.saveTheme(theme);
    }
    @Test(expected = EntityAlreadyExistsException.class)
    public void saveThemeAlreadyExists() throws Exception{
        themeService.saveTheme(theme);

        themeService.saveTheme(theme);
    }


    @Test(expected = IllegalArgumentException.class)
    public void deleteThemeWithNullID() throws Exception{
        themeService.deleteTheme(null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void deleteThemeWithNonExistent() throws Exception {
        themeService.deleteTheme(1L);
    }

    @Test(expected = EntityBeingUsedException.class)
    public void deleteThemeBeingUsed() throws Exception{

        themeService.saveTheme(theme);

        votdService = getInstance(VotdService.class);
        Votd votd = new Votd();
        votd.setVerses("Matthew 6:1-8");
        votd.setCreatedBy("John Smith");
        votd.setModifiedBy("Jack Thepumpkinking");
        List<Theme> themeList = new ArrayList<>();
        themeList.add(theme);

        votd.setThemes(themeList);
        votdService.save(votd);

        themeService.deleteTheme(1L);
    }

}