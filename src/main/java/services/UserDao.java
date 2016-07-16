package services;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import utilities.Config;
import utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserDao {

    @Inject
    private NinjaCache ninjaCache;

    @Inject
    private Config config;

    @Inject
    private Utils utils;

    public UserDao() {
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
            return utils.auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi());
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

        JsonObject response = utils.auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi());

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
            List<String> rolesList = new ArrayList<>();
            try {
                name = user.getAsJsonObject().get("user_metadata").getAsJsonObject().get("name").getAsString();
            } catch (NullPointerException npe) {
                name = user.getAsJsonObject().get("name").getAsString();
            }

            try {
                JsonArray rolesArray = user.getAsJsonObject().get("app_metadata")
                        .getAsJsonObject()
                        .get("roles")
                        .getAsJsonArray();

                for (JsonElement role : rolesArray) {
                    rolesList.add(role.getAsString());
                }

            } catch (NullPointerException npe) {
                //do nothing data table will get empty list
            }

            userFields = new String[]{name,
                    user.getAsJsonObject().get("email").getAsString(),
                    utils.formatListToHtml(rolesList),
                    user.getAsJsonObject().get("last_login").getAsString(),
                    user.getAsJsonObject().get("created_at").getAsString(),
                    "<a id=\"editrole\" style=\"cursor:pointer;\" class=\"fa fa-pencil\" aria-hidden=\"true\" " +
                            "data-toggle=\"modal\" data-userid=\"" + user.getAsJsonObject().get("user_id").getAsString() + "\" data-username=\"" + name + "\"" +
                            " data-target=\"#updateRolesModal\"></a>"};

            usersData.add(userFields);
        }

        return usersData;
    }

    /**
     * For a given user id, generate a set of checkboxes representing all roles
     * with the existing roles pre-selected.
     *
     * @param userId
     * @return
     */
    public String generateRolesCheckboxes(String userId) {
        List<String> roles = getUserRoles(userId);

        String checkBoxString = "";
        String roleDescription = "";


        for (String roleType : config.getRolesList()) {
            if (roleType.contentEquals(config.getMemberRole())) {
                roleDescription = config.getMemberDescription();
            } else if (roleType.contentEquals(config.getContributorRole())) {
                roleDescription = config.getContributorDescription();
            } else if (roleType.contentEquals(config.getPublisherRole())) {
                roleDescription = config.getPublisherDescription();
            }
            if (roles.contains(roleType)) {
                checkBoxString += "<div class=\"checkbox\">\n" +
                        "                <label>\n" +
                        "                    <input id=\"" + roleType + "\" type=\"checkbox\" value=\"" + roleType + "\" checked>\n" +
                        "                    <b>" + roleType + "</b><br/>\n" +
                        "                    <i>" + roleDescription + "</i>\n" +
                        "                </label>\n" +
                        "            </div>";
            } else {
                checkBoxString += "<div class=\"checkbox\">\n" +
                        "                <label>\n" +
                        "                    <input id=\"" + roleType + "\" type=\"checkbox\" value=\"" + roleType + "\" >\n" +
                        "                    <b>" + roleType + "</b><br/>\n" +
                        "                    <i>" + roleDescription + "</i>\n" +
                        "                </label>\n" +
                        "            </div>";
            }

        }

        return checkBoxString;
    }

    /**
     * Retrieve a user from auth0
     *
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
    private List<String> getUserRoles(String userID) throws JsonSyntaxException {
        List<String> rolesList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("fields", "app_metadata");
        params.put("include_fields", "true");

        try {
            JsonObject response = utils.auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi() + "/" + userID);

            JsonArray rolesArray = response.get("app_metadata")
                    .getAsJsonObject()
                    .get("roles")
                    .getAsJsonArray();

            for (JsonElement role : rolesArray) {
                rolesList.add(role.getAsString());
            }
            return rolesList;

        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    /**
     * Add a role to user's profile
     *
     * @param userId
     * @param roles
     */
    public void updateUserRole(String userId, List<String> roles) {

        Gson gson = new Gson();
        String body = "{\"app_metadata\": { \"roles\": " + gson.toJson(roles) + "} }";

        utils.updateUserProfile(userId, body);

    }

}
