package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import ninja.cache.NinjaCache;
import ninja.session.Session;
import ninja.session.SessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repositories.UserRepository;
import utilities.Config;
import utilities.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NinjaCache ninjaCache;
    @Mock
    private Config config;
    @Mock
    private Utils utils;
    @Mock
    private Session session;

    public UserServiceTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() throws Exception {
        userService = new UserService(ninjaCache, config, utils, userRepository);
    }

    @Test
    public void generateDataTableResults_with_6_items() throws Exception {

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(generateJsonUserObject());

        List<String[]> tableData = userService.generateDataTableResults(jsonArray);
        assertTrue(tableData.get(0)[1].contentEquals("someemail@gmail.com"));
    }

    @Test
    public void generateDataTableResults_without_name_in_usermetadata(){

        JsonPrimitive email = new JsonPrimitive("someemail@gmail.com");
        JsonPrimitive lastLogin = new JsonPrimitive("lastlogin date");
        JsonPrimitive createdat = new JsonPrimitive("createdat");
        JsonPrimitive userId = new JsonPrimitive("sdfhiuhdfus78");
        JsonPrimitive name = new JsonPrimitive("crafton");


        JsonArray jsonArray2 = new JsonArray();
        JsonPrimitive element = new JsonPrimitive("member");
        jsonArray2.add(element);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("roles", jsonArray2);

        JsonObject outerObject = new JsonObject();
        outerObject.add("name", name);
        outerObject.add("app_metadata", jsonObject2);
        outerObject.add("email", email);
        outerObject.add("last_login", lastLogin);
        outerObject.add("created_at", createdat);
        outerObject.add("user_id", userId);

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(outerObject);

        List<String[]> tableData = userService.generateDataTableResults(jsonArray);

        String nameAsString = tableData.get(0)[0];
        assertTrue(nameAsString.contentEquals("crafton"));

    }

    @Test
    public void generateRolesCheckboxes() throws Exception {

        String checkboxesToReturn = "<div class=\"checkbox\">\n" +
                "                <label>\n" +
                "                    <input id=\"member\" type=\"checkbox\" value=\"member\" checked>\n" +
                "                    <b>member</b><br/>\n" +
                "                    <i>somedescription</i>\n" +
                "                </label>\n" +
                "            </div><div class=\"checkbox\">\n" +
                "                <label>\n" +
                "                    <input id=\"publisher\" type=\"checkbox\" value=\"publisher\" checked>\n" +
                "                    <b>publisher</b><br/>\n" +
                "                    <i>somedescription</i>\n" +
                "                </label>\n" +
                "            </div><div class=\"checkbox\">\n" +
                "                <label>\n" +
                "                    <input id=\"contributor\" type=\"checkbox\" value=\"contributor\" >\n" +
                "                    <b>contributor</b><br/>\n" +
                "                    <i>somedescription</i>\n" +
                "                </label>\n" +
                "            </div>";

        when(userRepository.findRolesByUserId("someid")).thenReturn(Arrays.asList("member", "publisher"));
        when(config.getMemberRole()).thenReturn("member");
        when(config.getMemberDescription()).thenReturn("somedescription");
        when(config.getPublisherRole()).thenReturn("publisher");
        when(config.getPublisherDescription()).thenReturn("somedescription");
        when(config.getContributorRole()).thenReturn("contributor");
        when(config.getContributorDescription()).thenReturn("somedescription");
        when(config.getRolesList()).thenReturn(Arrays.asList("member", "publisher", "contributor"));

        String checkboxes = userService.generateRolesCheckboxes("someid");

        assertTrue(checkboxes.contentEquals(checkboxesToReturn));

        verify(userRepository).findRolesByUserId("someid");
        verify(config, times(3)).getMemberRole();
        verify(config, times(1)).getPublisherRole();
        verify(config, times(2)).getContributorRole();
        verify(config).getRolesList();
        verify(config).getMemberDescription();
        verify(config).getPublisherDescription();
        verify(config).getContributorDescription();
    }

    @Test
    public void hasRole() throws Exception {

        when(ninjaCache.get("somekey")).thenReturn(generateJsonUserObject().toString());

        boolean result = userService.hasRole("somekey", "member");

        assertTrue(result);

        verify(ninjaCache).get("somekey");
    }

    @Test
    public void findEmailsByRole() throws Exception {

    }

    private JsonObject generateJsonUserObject(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "crafton");

        JsonPrimitive email = new JsonPrimitive("someemail@gmail.com");
        JsonPrimitive lastLogin = new JsonPrimitive("lastlogin date");
        JsonPrimitive createdat = new JsonPrimitive("createdat");
        JsonPrimitive userId = new JsonPrimitive("sdfhiuhdfus78");


        JsonArray jsonArray2 = new JsonArray();
        JsonPrimitive element = new JsonPrimitive("member");
        jsonArray2.add(element);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("roles", jsonArray2);

        JsonObject outerObject = new JsonObject();
        outerObject.add("user_metadata", jsonObject);
        outerObject.add("app_metadata", jsonObject2);
        outerObject.add("email", email);
        outerObject.add("last_login", lastLogin);
        outerObject.add("created_at", createdat);
        outerObject.add("user_id", userId);

        return outerObject;
    }

}