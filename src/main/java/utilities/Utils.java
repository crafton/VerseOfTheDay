package utilities;

import com.google.inject.Inject;

import org.slf4j.Logger;
import java.util.*;

public class Utils {

    private final Logger logger;

    @Inject
    public Utils(Logger logger) {
        this.logger = logger;
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
