package services;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import ninja.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import utilities.Config;
import utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final NinjaCache ninjaCache;
    private final Config config;
    private final Utils utils;
    private final UserRepository userRepository;

    @Inject
    public UserService(NinjaCache ninjaCache, Config config, Utils utils, UserRepository userRepository) {
        this.ninjaCache = ninjaCache;
        this.config = config;
        this.utils = utils;
        this.userRepository = userRepository;
    }

    public String getCurrentUser(String idToken) {
        return (String) ninjaCache.get(idToken);
    }

    /**
     * @param start
     * @param length
     * @param search
     * @return
     * @throws JsonSyntaxException
     */
    public JsonObject findUserRecordsWithPaging(Integer start, Integer length, String search) throws JsonSyntaxException {
        return userRepository.findUsersWithPaging(start, length, search);
    }

    /**
     * @return
     * @throws JsonSyntaxException
     */
    public Integer getTotalRecords() throws JsonSyntaxException {
        return userRepository.getTotalUserRecords();
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
    public JsonObject findUser(String accessToken) throws JsonSyntaxException {
        return userRepository.findUserByToken(accessToken);
    }

    /**
     * Get a list of all roles assigned to the given user
     *
     * @param userID
     * @return
     */
    private List<String> getUserRoles(String userID) throws JsonSyntaxException {
        return userRepository.findRolesByUserId(userID);
    }

    /**
     * Check if the currently logged in user has a given role
     *
     * @param idTokenString
     * @param role
     * @return
     */
    public boolean hasRole(String idTokenString, String role) {

        String userJsonString = (String) ninjaCache.get(idTokenString);
        JsonParser jsonParser = new JsonParser();

        JsonObject userProfile = jsonParser.parse(userJsonString).getAsJsonObject();

        JsonArray rolesArray = userProfile.get("app_metadata")
                .getAsJsonObject()
                .get("roles")
                .getAsJsonArray();

        for (JsonElement roleElement : rolesArray) {
            if (roleElement.getAsString().contentEquals(role)) {
                return true;
            }
        }

        return false;
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

        userRepository.updateUser(userId, body);

    }

    /**
     * Create a user session after successful authentication
     *
     * @param session
     * @param code
     */
    public void createSession(Session session, String code) throws JsonSyntaxException, IllegalStateException {
        /*Retrieve authentication tokens from auth0*/
        Map<String, String> tokens = userRepository.getAuthToken(code);
        JsonObject userObject = findUser(tokens.get("access_token"));

            /*Cache user profile so we don't have to query information again for the session*/
        ninjaCache.set(tokens.get("id_token"), userObject.toString());

            /*Store only tokens in the session cookie*/
        session.put("idToken", tokens.get("id_token"));
        session.put("accessToken", tokens.get("access_token"));
    }

    /**
     * Find all email addresses for contributors
     *
     * @param role
     * @return
     * @throws JsonSyntaxException
     */
    public List<String> findEmailsByRole(String role) throws JsonSyntaxException {

        List<String> roleEmails = new ArrayList<>();

        JsonArray usersAsJson = userRepository.findUsers(role).getAsJsonArray("users");

        if (usersAsJson.isJsonNull() || usersAsJson.size() == 0) {
            return roleEmails;
        }

        for (JsonElement user : usersAsJson) {
            roleEmails.add(user.getAsJsonObject().get("email").getAsString());
        }

        logger.debug("Retrieved the following contributor email address: " + roleEmails.toString());

        return roleEmails;
    }

}
