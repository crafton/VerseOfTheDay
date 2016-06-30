package daos;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import utilities.Config;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Created by Crafton Williams on 20/06/2016.
 */
public class UserDao {

    @Inject
    NinjaCache ninjaCache;

    @Inject
    Config config;

    public UserDao() {
    }

    public void update() {

    }

    public String getCurrentUser(String idToken) {
        return (String) ninjaCache.get(idToken);
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

        JsonParser parser = new JsonParser();
        JsonObject responseJsonObject;
        try {
            responseJsonObject = parser.parse(response).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

        return responseJsonObject
                .get("total")
                .getAsInt();
    }

    private void refreshCache() {

    }
}
