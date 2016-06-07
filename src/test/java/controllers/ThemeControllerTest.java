package controllers;

import com.google.inject.Inject;
import daos.ThemeDao;
import models.Theme;
import models.Votd;
import ninja.NinjaTest;
import ninja.Result;
import ninja.NinjaDocTester;
import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import utilities.ControllerUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;


/**
 * Created by Crafton Williams on 4/06/2016.
 */


public class ThemeControllerTest extends NinjaTest {


    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void themes() throws Exception {

    }

    @Test
    public void saveTheme() throws Exception {



    }

    @Test
    public void deleteTheme() throws Exception {

    }

}