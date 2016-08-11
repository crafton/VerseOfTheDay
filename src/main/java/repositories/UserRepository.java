package repositories;


import com.google.gson.*;
import com.google.inject.Inject;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.message.internal.StringBuilderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.Config;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private final Config config;
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    @Inject
    public UserRepository(Config config) {
        this.config = config;
    }

    /**
     * Retrieve users based on a search string. The search will be executed against; name, email, role
     *
     * @param search
     * @return
     * @throws JsonSyntaxException
     */
    public JsonObject findUsers(String search) throws JsonSyntaxException {
        return findUsersWithPaging(0, 50, search);
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
    public JsonObject findUsersWithPaging(Integer start, Integer length, String search) throws JsonSyntaxException {
        String queryString = "name:" + search + "* OR user_metadata.name:" + search + "* OR email:" + search + "* " +
                "OR app_metadata.roles:" + search + "* OR";

        Map<String, Object> params = new HashMap<>();
        params.put("per_page", length);
        params.put("page", start);
        params.put("include_totals", "true");
        params.put("fields", "name,user_metadata.name,email,last_login,created_at,user_id,app_metadata.roles,app_metadata.subscriptions");
        params.put("include_fields", "true");
        params.put("search_engine", "v2");
        params.put("q", queryString);

        return auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi());
    }

    /**
     * Retrieve the total number of users from auth0
     *
     * @return Integer. Total number of users
     * @throws JsonSyntaxException
     */
    public Integer getTotalUserRecords() throws JsonSyntaxException {
        Map<String, Object> params = new HashMap<>();
        params.put("per_page", "1");
        params.put("include_totals", "true");
        params.put("fields", "name");
        params.put("include_fields", "true");
        params.put("search_engine", "v2");

        JsonObject response = auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi());

        return response
                .get("total")
                .getAsInt();
    }

    /**
     * Retrieve a user from auth0
     *
     * @param accessToken
     * @return
     */
    public JsonObject findUserByToken(String accessToken) throws JsonSyntaxException {
        return auth0ApiQuery(null, "/userinfo", accessToken);
    }

    /**
     * Get a list of all roles assigned to the given user
     *
     * @param userID
     * @return
     */
    public List<String> findRolesByUserId(String userID) throws JsonSyntaxException {
        List<String> rolesList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("fields", "app_metadata");
        params.put("include_fields", "true");

        JsonObject response = auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi() + "/" + userID);

        JsonArray rolesArray = response.get("app_metadata")
                .getAsJsonObject()
                .get("roles")
                .getAsJsonArray();

        for (JsonElement role : rolesArray) {
            rolesList.add(role.getAsString());
        }
        return rolesList;
    }


    /**
     *
     * @param userID
     * @return
     * @throws JsonSyntaxException
     */
    public JsonObject findUserByUserId(String userID) throws JsonSyntaxException {
        Map<String, Object> params = new HashMap<>();
        params.put("fields", "app_metadata");
        params.put("include_fields", "true");

        JsonObject response = auth0ApiQueryWithMgmtToken(params, config.getAuth0UserApi() + "/" + userID);

        return response;
    }


    /**
     * Update user profile
     *
     * @param userId
     * @param body
     */
    public void updateUser(String userId, String body) {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://" + config.getAuth0Domain() + config.getAuth0UserApi() + "/" + userId);
        String response = target.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getAuth0MgmtToken())
                .method("PATCH", Entity.entity(body, MediaType.APPLICATION_JSON), String.class);

        logger.info("Received the following response after updating user profile:" + response);

    }

    /**
     * Retrieve an authentication token given a login code
     *
     * @param code
     * @return
     */
    public Map<String, String> getAuthToken(String code) throws JsonSyntaxException, IllegalStateException {
        Client client = ClientBuilder.newClient();

        Map<String, String> auth0TokenRequest = new HashMap<>();
        auth0TokenRequest.put("client_id", config.getAuth0ClientId());
        auth0TokenRequest.put("client_secret", config.getAuth0ClientSecret());
        auth0TokenRequest.put("redirect_uri", config.getAuth0Callback());
        auth0TokenRequest.put("code", code);
        auth0TokenRequest.put("grant_type", "authorization_code");

        String auth0TokenRequestString = new Gson().toJson(auth0TokenRequest);

        Response response = client.target("https://" + config.getAuth0Domain() + "/oauth/token")
                .request()
                .post(Entity.entity(auth0TokenRequestString, MediaType.APPLICATION_JSON));

        JsonObject jsonResponse;

        JsonParser parser = new JsonParser();
        jsonResponse = parser.parse(response.readEntity(String.class)).getAsJsonObject();

        String accessToken = jsonResponse
                .getAsJsonObject()
                .get("access_token")
                .getAsString();

        String idToken = jsonResponse
                .getAsJsonObject()
                .get("id_token")
                .getAsString();

        Map<String, String> userTokens = new HashMap<>();
        userTokens.put("access_token", accessToken);
        userTokens.put("id_token", idToken);

        return userTokens;
    }

    /**
     * Generic method to query the auth0 web api using the management token
     *
     * @param params
     * @param apiPath
     * @return
     * @throws JsonSyntaxException
     */
    private JsonObject auth0ApiQueryWithMgmtToken(Map<String, Object> params, String apiPath) throws JsonSyntaxException {
        return auth0ApiQuery(params, apiPath, config.getAuth0MgmtToken());
    }

    /**
     * Generic method to query the auth0 web api
     *
     * @param params
     * @param apiPath
     * @param token
     * @return
     */
    private JsonObject auth0ApiQuery(Map<String, Object> params, String apiPath, String token) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://" + config.getAuth0Domain() + apiPath);

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue());
            }
        }

        String response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get(String.class);

        return getJsonFromString(response);
    }

    /**
     * Get a JsonObject from a json formatted String
     *
     * @param someString
     * @return JsonObject
     * @throws JsonSyntaxException
     */
    private JsonObject getJsonFromString(String someString) throws JsonSyntaxException {
        JsonParser parser = new JsonParser();

        return parser.parse(someString).getAsJsonObject();
    }


}
