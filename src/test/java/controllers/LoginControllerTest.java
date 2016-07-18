package controllers;

import com.google.inject.Inject;
import ninja.NinjaDocTester;
import org.doctester.testbrowser.Request;
import org.doctester.testbrowser.Response;
import org.junit.Test;
import utilities.Config;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Created by Crafton Williams on 11/07/2016.
 */
public class LoginControllerTest extends NinjaDocTester {
    String LOGIN_URL = "/login";
    String LOGOUT_URL = "/logout";
    String CALLBACK = "/callback";

    @Inject
    Config config;

    @Test
    public void login() throws Exception {
        Response response = makeRequest(
                Request.GET().url(
                        testServerUrl().path(LOGIN_URL)
                )
        );

        assertThat(response.payload, containsString(config.getAuth0ClientId()));
    }

    @Test
    public void callback() throws Exception {

    }

    @Test
    public void logout() throws Exception {

    }

}