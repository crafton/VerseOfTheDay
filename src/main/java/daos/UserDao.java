package daos;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import utilities.Config;
import utilities.Utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String queryString = "name:" + search + "* OR user_metadata.name:" + search + "* OR email:" + search + "* " +
                "OR app_metadata.roles:" + search + "*";

        Map<String, Object> params = new HashMap<>();
        params.put("per_page", length);
        params.put("page", start);
        params.put("include_totals", "true");
        params.put("fields", "name,user_metadata.name,email,last_login,created_at,user_id,app_metadata.roles");
        params.put("include_fields", "true");
        params.put("search_engine", "v2");
        params.put("q", queryString);

        try {
            return utils.auth0ApiQueryWithMgmtToken(params, config.USER_API);
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

        Map<String, Object> params = new HashMap<>();
        params.put("per_page", "1");
        params.put("include_totals", "true");
        params.put("fields", "name");
        params.put("include_fields", "true");
        params.put("search_engine", "v2");

        JsonObject response = utils.auth0ApiQueryWithMgmtToken(params, config.USER_API);

        try {
            return response
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
            String roles;
            try {
                name = user.getAsJsonObject().get("user_metadata").getAsJsonObject().get("name").getAsString();
            } catch (NullPointerException npe) {
                name = user.getAsJsonObject().get("name").getAsString();
            }

            try {
                roles = user.getAsJsonObject().get("app_metadata").getAsJsonObject().get("roles").getAsJsonArray().getAsString();
            } catch (NullPointerException npe) {
                roles = "";
            }

            userFields = new String[]{name,
                    user.getAsJsonObject().get("email").getAsString(),
                    roles,
                    user.getAsJsonObject().get("last_login").getAsString(),
                    user.getAsJsonObject().get("created_at").getAsString(), ""};

            usersData.add(userFields);
        }

        return usersData;
    }

    /**
     * @param accessToken
     * @return
     */
    public JsonObject auth0GetUser(String accessToken) throws JsonSyntaxException {
        try {
            return utils.auth0ApiQuery(null, "/userinfo", accessToken);
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

    }

    /**
     * Get a list of all roles assigned to the given user
     *
     * @param userID
     * @return
     */
    public List<String> getUserRoles(String userID) throws JsonSyntaxException {
        List<String> rolesList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("fields", "app_metadata");
        params.put("include_fields", "true");

        try {
            JsonObject response = utils.auth0ApiQueryWithMgmtToken(params, config.USER_API + "/" + userID);

            JsonArray rolesArray = response.get("app_metadata")
                    .getAsJsonArray();

            for (JsonElement role : rolesArray) {
                rolesList.add(role.getAsString());
            }
            return rolesList;

        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    private void refreshCache() {

    }
}
