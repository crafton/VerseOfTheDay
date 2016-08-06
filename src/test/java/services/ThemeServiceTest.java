package services;

import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import ninja.NinjaDaoTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import repositories.ThemeRepository;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ThemeServiceTest {

    private ThemeService themeService;
    @Mock
    private VotdService votdService;
    @Mock
    private Theme theme;
    @Mock
    private ThemeRepository themeRepository;

    public ThemeServiceTest() {
        MockitoAnnotations.initMocks(this);
    }


    @Before
    public void setup() {
        themeService = new ThemeService(themeRepository);
    }

    @Test
    public void find_theme_by_name() throws Exception {
        when(themeRepository.findByName("somename")).thenReturn("somestring");

        String themeName = themeService.findThemeByName("somename");

        assertTrue(themeName.contentEquals("somestring"));

        verify(themeRepository).findByName("somename");
    }

    @Test(expected = NoResultException.class)
    public void find_theme_by_name_throws_NoresultException_when_theme_doesnt_exist() throws Exception {
        when(themeRepository.findByName("somenonexistentname")).thenThrow(new NoResultException());

        themeService.findThemeByName("somenonexistentname");

        verify(themeRepository).findByName("somenonexistentname");
    }

    @Test
    public void find_theme_by_id() throws Exception {
        when(themeRepository.findById(1L)).thenReturn(theme);

        Theme foundTheme = themeService.findThemeById(1L);

        assertNotNull(foundTheme);

        verify(themeRepository).findById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void find_theme_by_id_when_param_is_null() throws Exception {
        when(themeRepository.findById(null)).thenThrow(new IllegalArgumentException());

        themeService.findThemeById(null);

        verify(themeRepository).findById(null);

    }

    @Test
    public void save_theme() throws Exception {
        when(theme.getThemeName()).thenReturn("Love");
        doNothing().when(themeRepository).save(theme);

        themeService.saveTheme(theme);

        verify(themeRepository).save(theme);
    }

    @Test(expected = IllegalArgumentException.class)
    public void save_theme_when_param_is_null() throws Exception {
        themeService.saveTheme(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void save_theme_when_themename_is_empty() throws Exception{
        when(theme.getThemeName()).thenReturn("");

        themeService.saveTheme(theme);

        verify(theme).getThemeName();
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void save_theme_that_already_exists() throws Exception{
        when(theme.getThemeName()).thenReturn("Love");
        doThrow(EntityAlreadyExistsException.class).when(themeRepository).save(theme);

        themeService.saveTheme(theme);

        verify(themeRepository).save(theme);
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_theme_when_id_is_null() throws Exception{
        doThrow(IllegalArgumentException.class).when(themeRepository).delete(null);

        themeService.deleteTheme(null);

        verify(themeRepository).delete(null);

    }

    @Test(expected = EntityDoesNotExistException.class)
    public void delete_theme_when_theme_does_not_exist() throws Exception{
        doThrow(EntityDoesNotExistException.class).when(themeRepository).delete(10L);

        themeService.deleteTheme(10L);

        verify(themeRepository).delete(10L);

    }
    @Test(expected = EntityBeingUsedException.class)
    public void delete_theme_when_theme_is_being_used() throws Exception{
        doThrow(EntityBeingUsedException.class).when(themeRepository).delete(10L);

        themeService.deleteTheme(10L);

        verify(themeRepository).delete(10L);

    }



}