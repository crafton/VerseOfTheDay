package controllers;

import models.Theme;
import ninja.NinjaTest;
import ninja.Result;
import ninja.session.FlashScope;
import ninja.session.FlashScopeImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.ThemeService;
import utilities.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class ThemeControllerTest {

    @Mock private ThemeController themeController;
    @Mock private ThemeService themeService;
    @Mock private Config config;
    @Mock private Theme theme1;
    @Mock private Theme theme2;
    @Mock private FlashScope flashScope;

    public ThemeControllerTest(){
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() throws Exception {
        themeController = new ThemeController(themeService, config);
    }

    @Test
    public void display_all_themes_when_2_themes_exist() throws Exception {
        List<Theme> themeList = new ArrayList<>();
        themeList.add(theme1);
        themeList.add(theme2);
        when(themeService.findAllThemes()).thenReturn(themeList);

        Result result = themeController.themes();
        assertTrue(result.getStatusCode() == 200);
        verify(themeService).findAllThemes();
    }

    @Test
    public void display_all_themes_when_no_themes_exist() throws Exception{
        List<Theme> themeList = new ArrayList<>();
        when(themeService.findAllThemes()).thenReturn(themeList);

        Result result = themeController.themes();
        assertTrue(result.getStatusCode() == 200);
        verify(themeService).findAllThemes();
    }

    @Test
    public void save_theme_and_redirect() throws Exception {
        doNothing().when(themeService).saveTheme(theme1);

        Result result = themeController.saveTheme(theme1, flashScope);

        assertTrue(result.getStatusCode() == 303);
        verify(themeService).saveTheme(theme1);

    }

    @Test
    public void save_theme_service_throws_illegal_argument_exception() throws Exception{
        doThrow(IllegalArgumentException.class).when(themeService).saveTheme(theme1);

        Result result = themeController.saveTheme(theme1, flashScope);

        assertTrue(result.getStatusCode() == 303);
        verify(themeService).saveTheme(theme1);
    }

    @Test
    public void deleteTheme() throws Exception {

    }

}