package models;


import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class User {

    private String name;
    private String email;
    private String user_id;
    private String last_login;
    private String created_at;
    private Map<String, String> user_metadata;
    private Map<String, Object> app_metadata;
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public String getEmail() {
        return email;
    }

    public String getName() {

        if (user_metadata != null) {
            String realName = user_metadata.get("name");
            if (realName == null || realName.isEmpty()) {
                return name;
            }
            return realName;
        } else if (!StringUtils.isEmpty(name)) {
            return name;
        }

        return "Name not set";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Map<String, String> getUser_metadata() {
        return user_metadata;
    }

    public void setUser_metadata(Map<String, String> user_metadata) {
        this.user_metadata = user_metadata;
    }

    public Map<String, Object> getApp_metadata() {
        return app_metadata;
    }

    public void setApp_metadata(Map<String, Object> app_metadata) {
        this.app_metadata = app_metadata;
    }

    public List<Long> getSubscriptions() {
        List<Object> campaignIds = (List<Object>) app_metadata.get("subscriptions");

        if (campaignIds != null) {
            List<Long> campaignIdsAsLong = campaignIds
                    .stream()
                    .map(Object -> ((Double) Object).longValue()).collect(Collectors.toList());

            return campaignIdsAsLong;
        }

        return new ArrayList<Long>();
    }

    public Map<String, String> getSettings() {
        Map<String, String> settings = (Map<String, String>) this.getApp_metadata().get("settings");

        //if user doesn't have settings, set default version
        if (settings == null) {
            settings = new HashMap<>();
        }

        return settings;
    }

    public List<String> getRoles() {
        List<String> roles = (List<String>) this.getApp_metadata().get("roles");

        if (roles == null) {
            return new ArrayList<>();
        }

        return roles;
    }

    public String getLast_login() {
        return last_login;
    }

    public void setLast_login(String last_login) {
        this.last_login = last_login;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
