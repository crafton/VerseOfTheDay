package utilities;

import com.google.gson.*;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by Crafton Williams on 27/03/2016.
 */
public class Utils {

    @Inject
    Config config;

    final static Logger logger = LoggerFactory.getLogger(Utils.class);

    public Utils() {

    }

    /**
     * @param code
     * @return
     */
    public Map<String, String> auth0GetToken(String code) throws JsonSyntaxException {
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
        try {
            JsonParser parser = new JsonParser();
            jsonResponse = parser.parse(response.readEntity(String.class)).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

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
     * Format a list of strings to an html list
     *
     * @param items
     * @return an html representation of a java list.
     */
    public String formatListToHtml(List<String> items) {

        String formattedList = "";

        for (String item : items) {
            formattedList += "<li>" + item + "</li>";
        }

        return "<p><ul>" + formattedList + "</ul></p>";
    }

    /**
     * Generic method to query the auth0 web api using the management token
     *
     * @param params
     * @param apiPath
     * @return
     * @throws JsonSyntaxException
     */
    public JsonObject auth0ApiQueryWithMgmtToken(Map<String, Object> params, String apiPath) throws JsonSyntaxException {
        try {
            return auth0ApiQuery(params, apiPath, config.getAuth0MgmtToken());
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    /**
     * Generic method to query the auth0 web api
     *
     * @param params
     * @param apiPath
     * @param token
     * @return
     */
    public JsonObject auth0ApiQuery(Map<String, Object> params, String apiPath, String token){
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://" + config.getAuth0Domain() + apiPath);

        if(params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue());
            }
        }

        String response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get(String.class);

        try {
            return getJsonFromString(response);
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }
    }

    /**
     * Get a JsonObject from a json formatted String
     *
     * @param someString
     * @return JsonObject
     * @throws JsonSyntaxException
     */
    public JsonObject getJsonFromString(String someString) throws JsonSyntaxException {
        JsonParser parser = new JsonParser();

        try {
            return parser.parse(someString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException(e.getMessage());
        }

    }

}