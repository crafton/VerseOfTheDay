package models;


import com.google.gson.Gson;

import java.util.*;
import java.util.stream.Collectors;

public class User {

    private String name;
    private String email;
    private String user_id;
    private Map<String, String> user_metadata;
    private Map<String, Object> app_metadata;

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getName() {

        String realName = user_metadata.get("name");

        if(realName == null || realName.isEmpty()){
            return name;
        }

        return realName;
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

        if (settings == null) {
            return new HashMap<>();
        }

        return settings;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
