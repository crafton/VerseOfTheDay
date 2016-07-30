package utilities;

import com.google.gson.*;
import com.google.inject.Inject;
import ninja.cache.NinjaCache;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
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

public class Utils {

    @Inject
    private Logger logger;

    public Utils() {

    }

    /**
     * Format a list of strings to an html list
     *
     * @param items
     * @return an html representation of a java list.
     */
    public String formatListToHtml(List<String> items) {

        if (items == null || items.isEmpty()) {
            return "";
        }

        String formattedList = "";

        for (String item : items) {
            formattedList += "<li>" + item + "</li>";
        }

        return "<ul>" + formattedList + "</ul>";
    }

}
