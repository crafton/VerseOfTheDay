package daos;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import utilities.Config;
import utilities.Utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crafton Williams on 20/06/2016.
 */
public class UserDao {

    @Inject
    NinjaCache ninjaCache;

    @Inject
    Config config;

    @Inject
    Utils utils;

    public UserDao() {
    }

    public void update() {

    }

    public String getCurrentUser(String idToken) {
        return (String) ninjaCache.get(idToken);
    }

    /**
     * Retrieve user records for the data table based on specified query
     *
     * @param start
     * @param length
     * @param search
     * @return JsonObject containing returned records
     * @throws JsonSyntaxException
     */
    public JsonObject getUserRecords(Integer start, Integer length, String search) throws JsonSyntaxException {

        String queryString = "name:" + search + "* OR user_metadata.name:" + search + "* OR email:" + search + "*";

        Client client = ClientBuilder.newClient();
        String response = client.target("https://" + config.getAuth0Domain() + "/api/v2/users")
                .queryParam("per_page", length)
                .queryParam("page", start)
                .queryParam("include_totals", "true")
                .queryParam("fields", "name,user_metadata.name,email,last_login,created_at,user_id")
                .queryParam("include_fields", "true")
                .queryParam("search_engine", "v2")
                .queryParam("q", queryString)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getAuth0MgmtToken())
                .get(String.class);

        try {
            return utils.getJsonFromString(response);
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    /**
     * Retrieve the total number of users from auth0
     *
     * @return Integer. Total number of users
     * @throws JsonSyntaxException
     */
    public Integer getTotalRecords() throws JsonSyntaxException {
        Client client = ClientBuilder.newClient();
        String response = client.target("https://" + config.getAuth0Domain() + "/api/v2/users")
                .queryParam("per_page", "1")
                .queryParam("include_totals", "true")
                .queryParam("fields", "name")
                .queryParam("include_fields", "true")
                .queryParam("search_engine", "v2")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getAuth0MgmtToken())
                .get(String.class);

        try {
            return utils.getJsonFromString(response)
                    .get("total")
                    .getAsInt();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    /**
     * Reformat query results to what DataTables expects. Ensure fields are added to the
     * array in the same order as the columns headings are displayed.
     *
     * @param usersJsonList
     * @return
     */
    public List<String[]> generateDataTableResults(JsonArray usersJsonList) {
        List<String[]> usersData = new ArrayList<>();
        String[] userFields = new String[0];

        for (JsonElement user : usersJsonList) {
            String name;
            try {
                name = user.getAsJsonObject().get("user_metadata").getAsJsonObject().get("name").getAsString();
            } catch (NullPointerException npe) {
                name = user.getAsJsonObject().get("name").getAsString();
            }

            userFields = new String[]{name,
                    user.getAsJsonObject().get("email").getAsString(),
                    user.getAsJsonObject().get("last_login").getAsString(),
                    user.getAsJsonObject().get("created_at").getAsString(), "", ""};

            usersData.add(userFields);
        }

        return usersData;
    }

    /**
     * @param accessToken
     * @return
     */
    public JsonObject auth0GetUser(String accessToken) throws JsonSyntaxException {
        Client client = ClientBuilder.newClient();
        String auth0User = client.target("https://" + config.getAuth0Domain() + "/userinfo")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject userJsonObject;
        try {
            userJsonObject = parser.parse(auth0User).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
        return userJsonObject;
    }

    private void refreshCache() {

    }
}
