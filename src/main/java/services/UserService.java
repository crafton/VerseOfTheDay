package services;

import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import models.Message;
import models.Messenger;
import models.User;
import ninja.cache.NinjaCache;
import ninja.session.Session;
import org.apache.commons.lang.StringUtils;
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
    private final Messenger messenger;
    private final Provider<Message> messageProvider;
    private static final String APP_METADATA = "app_metadata";

    @Inject
    public UserService(NinjaCache ninjaCache, Config config, Utils utils, UserRepository userRepository,
                       Messenger messenger, Provider<Message> messageProvider) {
        this.ninjaCache = ninjaCache;
        this.config = config;
        this.utils = utils;
        this.userRepository = userRepository;
        this.messenger = messenger;
        this.messageProvider = messageProvider;
    }

    public User getCurrentUser(String idToken) {
        Gson gson = new Gson();
        return gson.fromJson((String) ninjaCache.get(idToken), User.class);
    }

    public void refreshUserProfileInCache(Session session) {
        String accessToken = session.get(config.ACCESSTOKEN_NAME);
        String userAsString = findUser(accessToken).toString();

        ninjaCache.set(session.get(config.IDTOKEN_NAME), userAsString);
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

    public JsonObject findUserById(String id) throws JsonSyntaxException {
        return userRepository.findUserByUserId(id);
    }

    public void sendNotificationToUsers(Message message) {
        logger.info("Sending notifications with the following subject: " + message.getSubject());
        Integer start = 0;
        Integer length = 1;

        List<String> notificationRecipients = new ArrayList<>();
        JsonObject userAsJsonObject = userRepository.findUsersToBeNotified(start, length);
        Integer totalRecords = userAsJsonObject.get("total").getAsInt();

        length = 100;
        Double lengthAsDouble = length.doubleValue();
        Double pages = totalRecords / lengthAsDouble;
        Integer pagesAsInt = (int) Math.ceil(pages);

        logger.info("Pages of users to send notifications: " + pagesAsInt);

        for (int i = 0; i < pagesAsInt; i++) {
            JsonObject usersAsJsonObject = userRepository.findUsersToBeNotified(i, length);
            JsonArray userJsonList = usersAsJsonObject.getAsJsonArray("users");
            Gson gson = new Gson();

            for (JsonElement jsonElement : userJsonList) {
                User user = gson.fromJson(jsonElement, User.class);
                notificationRecipients.add(user.getEmail());
            }

            message.setRecipients(notificationRecipients);
            messenger.sendMessage(message);
        }
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
     * @param userList
     * @return
     */
    public List<String[]> generateDataTableResults(List<User> userList) throws IllegalArgumentException {

        if (userList == null || userList.isEmpty()) {
            logger.warn("User tried to generate a table data set without a json array.");
            throw new IllegalArgumentException("JsonArray must contain at least one element.");
        }

        List<String[]> usersData = new ArrayList<>();
        String[] userFields;

        for (User user : userList) {
            String name;
            name = user.getName();

            List<String> rolesList = user.getRoles();

            userFields = new String[]{name,
                    user.getEmail(),
                    utils.formatListToHtml(rolesList),
                    user.getLast_login(),
                    user.getCreated_at(),
                    "<a id=\"editrole\" style=\"cursor:pointer;\" class=\"material-icons\" aria-hidden=\"true\" " +
                            "data-toggle=\"modal\" data-userid=\"" + user.getUser_id() + "\" data-username=\"" + name + "\"" +
                            " data-target=\"#updateRolesModal\">mode_edit</a>"};

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

        logger.debug("Roles generated for " + userId + ": " + roles.toString());

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
                        "                    <span class=\"checkbox-material\"><span class=\"check\"></span></span>" +
                        "                    <b>" + roleType + "</b><br/>\n" +
                        "                    <i>" + roleDescription + "</i>\n" +
                        "                </label>\n" +
                        "            </div>";
            } else {
                checkBoxString += "<div class=\"checkbox\">\n" +
                        "                <label>\n" +
                        "                    <input id=\"" + roleType + "\" type=\"checkbox\" value=\"" + roleType + "\" >\n" +
                        "                    <span class=\"checkbox-material\"><span class=\"check\"></span></span>" +
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
     * @param userID user's unique id
     * @return List of roles assigned ot the user
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

        if (StringUtils.isEmpty(userJsonString)) {
            return false;
        }

        JsonParser jsonParser = new JsonParser();

        JsonObject userProfile = jsonParser.parse(userJsonString).getAsJsonObject();

        JsonArray rolesArray = userProfile.get(APP_METADATA)
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

    public String getHighestRole(String idToken) {
        if (hasRole(idToken, "admin")) {
            return "admin";
        }

        if (hasRole(idToken, config.getPublisherRole())) {
            return config.getPublisherRole();
        }

        if (hasRole(idToken, config.getContributorRole())) {
            return config.getContributorRole();
        } else {
            return config.getMemberRole();
        }
    }

    /**
     * Add a role to user's profile
     *
     * @param userId
     * @param roles
     */
    public void updateUserRole(String userId, List<String> roles) {

        if (userId == null || roles == null) {
            throw new IllegalArgumentException("updateUserRole parameters cannot be null.");
        }

        Gson gson = new Gson();
        String body = "{\"app_metadata\": { \"roles\": " + gson.toJson(roles) + "} }";

        userRepository.updateUser(userId, body);

    }

    /**
     * Add a new subscription record to the user profile
     *
     * @param userId
     * @param campaignId
     * @return
     * @throws IllegalArgumentException
     */
    public boolean subscribe(String userId, Long campaignId) throws IllegalArgumentException {
        if (campaignId == null) {
            throw new IllegalArgumentException("updateSubscription parameter cannot be null.");
        }

        JsonObject userObject = findUserById(userId);
        JsonElement subscriptionElement = userObject.get(APP_METADATA)
                .getAsJsonObject()
                .get("subscriptions");

        JsonArray subscriptionArray = new JsonArray();
        if (subscriptionElement != null) {
            subscriptionArray = subscriptionElement
                    .getAsJsonArray();

            //check if campaignId already exists in array
            for (JsonElement campaign : subscriptionArray) {
                if (campaign.getAsLong() == campaignId) {
                    return false;
                }
            }
        }

        JsonElement newSubscription = new JsonPrimitive(campaignId);
        subscriptionArray.add(newSubscription);
        Gson gson = new Gson();
        String body = "{\"app_metadata\": { \"subscriptions\": " + gson.toJson(subscriptionArray) + "} }";

        userRepository.updateUser(userId, body);

        return true;
    }

    /**
     * Remove a subscription record from the user profile
     *
     * @param userId
     * @param campaignId
     * @return
     */
    public boolean unsubscribe(String userId, Long campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("updateSubscription parameter cannot be null.");
        }

        JsonObject userObject = findUserById(userId);
        JsonElement subscriptionElement = userObject.get(APP_METADATA)
                .getAsJsonObject()
                .get("subscriptions");

        if (subscriptionElement == null) {
            logger.warn("Trying to unsubscribe using non-existent subscription.");
            return false;
        }

        JsonArray subscriptionArray = subscriptionElement.getAsJsonArray();

        //convert jsonarray to list of json elements
        List<JsonElement> subscriptionList = new ArrayList<>();
        subscriptionArray.forEach(subscriptionList::add);

        JsonElement campaignElement = new JsonPrimitive(campaignId);
        if (subscriptionList.contains(campaignElement)) {
            subscriptionList.remove(campaignElement);

            JsonArray remainingSubscriptionArray = new JsonArray();
            subscriptionList.forEach(remainingSubscriptionArray::add);

            Gson gson = new Gson();
            String body = "{\"app_metadata\": { \"subscriptions\": " + gson.toJson(remainingSubscriptionArray) + "} }";

            userRepository.updateUser(userId, body);

            return true;
        }

        return false;
    }

    public boolean unsubscribeAll(Long campaignId) {
        Integer length = 40;
        Integer totalSubscribers = userRepository.findSubscribedUsers(0, length, campaignId).get("total").getAsInt();

        if (totalSubscribers == 0) {
            logger.warn("No subscribers for campaign with id: " + campaignId + "...doing nothing.");
            return false;
        }

        Double lengthAsDouble = length.doubleValue();
        Double pagesAsDouble = totalSubscribers / lengthAsDouble;
        Integer pages = (int) Math.ceil(pagesAsDouble);

        for (int i = 0; i < pages; i++) {
            JsonObject usersAsObject = userRepository.findSubscribedUsers(i, length, campaignId);
            JsonArray userJsonList = usersAsObject.getAsJsonArray("users");
            for (JsonElement user : userJsonList) {
                String userId = user.getAsJsonObject().get("user_id").getAsString();
                if (!unsubscribe(userId, campaignId)) {
                    logger.warn("Failed to unsubscribe user: " + userId + " from campaign: " + campaignId);
                }
            }
        }

        return true;
    }

    /**
     * @param user
     */
    public void updateUserSettings(User user) {
        logger.debug("Updating user settings...");

        Object settingsObject = user.getApp_metadata().get("settings");

        Map<String, String> settings = (Map<String, String>) settingsObject;

        Gson gson = new Gson();

        String updateString = "{\"app_metadata\": { \"settings\": " + gson.toJson(settings) + "} }";

        logger.info("Sending the following update: " + updateString);

        userRepository.updateUser(user.getUser_id(), updateString);

    }

    /**
     * Create a user session after successful authentication
     *
     * @param session
     * @param code
     */
    public void createSession(Session session, String code) throws JsonSyntaxException, IllegalStateException {

        if (session == null) {
            throw new IllegalArgumentException("The session parameter in createSession cannot be null");
        }

        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("The code parameter in createSession cannot be null");
        }

        /*Retrieve authentication tokens from auth0*/
        Map<String, String> tokens = userRepository.getAuthToken(code);
        JsonObject userObject = findUser(tokens.get("access_token"));

            /*Cache user profile so we don't have to query information again for the session*/
        ninjaCache.set(tokens.get("id_token"), userObject.toString());

            /*Store only tokens in the session cookie*/
        session.put(config.IDTOKEN_NAME, tokens.get("id_token"));
        session.put(config.ACCESSTOKEN_NAME, tokens.get("access_token"));
    }

    /**
     * Find all email addresses for a given role
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
