package models;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class User {

    private String email;
    private String user_id;
    private Map<String, String> user_metadata;
    private Map<String, List<Object>> app_metadata;

    public User() {
    }

    public String getEmail() {
        return email;
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

    public Map<String, List<Object>> getApp_metadata() {
        return app_metadata;
    }

    public void setApp_metadata(Map<String, List<Object>> app_metadata) {
        this.app_metadata = app_metadata;
    }

    public List<Long> getSubscriptions() {
        List<Object> campaignIds = app_metadata.get("subscriptions");

        if (campaignIds != null) {
            List<Long> campaignIdsAsLong = campaignIds
                    .stream()
                    .map(Object -> ((Double) Object).longValue()).collect(Collectors.toList());

            return campaignIdsAsLong;
        }

       return new ArrayList<Long>();
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
