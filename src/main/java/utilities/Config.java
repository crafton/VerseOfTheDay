package utilities;

import com.google.inject.Inject;
import ninja.utils.NinjaProperties;

import java.util.Optional;

/**
 * Created by Crafton Williams on 14/06/2016.
 */
public class Config {

    private String bibleSearchKey;
    private Integer maxVerses;
    private Integer themesMaxCols;
    private String auth0ClientId;
    private String auth0Domain;
    private String auth0Callback;
    private String auth0Logout;
    private String auth0ClientSecret;

    @Inject
    private Config(NinjaProperties ninjaProperties) {
        Optional<Integer> optionalMaxVerses = Optional.of(ninjaProperties.getIntegerWithDefault("votd.maxverses", 0));
        this.maxVerses = optionalMaxVerses.get();

        Optional<String> optionalBibleKey = Optional.ofNullable(ninjaProperties.get("biblesearch.key"));
        if (optionalBibleKey.isPresent()) {
            this.bibleSearchKey = optionalBibleKey.get();
        } else {
            this.bibleSearchKey = "";
        }

        Optional<Integer> optionalThemeMaxCols = Optional.of(ninjaProperties.getIntegerWithDefault("themes.maxcols", 7));
        this.themesMaxCols = optionalThemeMaxCols.get();

        Optional<String> optionalClientId = Optional.of(ninjaProperties.get("auth0.clientid"));
        if (optionalClientId.isPresent()) {
            this.auth0ClientId = optionalClientId.get();
        } else {
            this.auth0ClientId = "";
        }

        Optional<String> optionalAuth0Domain = Optional.of(ninjaProperties.get("auth0.domain"));
        if (optionalAuth0Domain.isPresent()) {
            this.auth0Domain = optionalAuth0Domain.get();
        } else {
            this.auth0Domain = "";
        }

        Optional<String> optionalAuth0Callback = Optional.of(ninjaProperties.get("auth0.callback"));
        if (optionalAuth0Domain.isPresent()) {
            this.auth0Callback = optionalAuth0Callback.get();
        } else {
            this.auth0Callback = "";
        }

        Optional<String> optionalAuth0Logout = Optional.of(ninjaProperties.get("auth0.logout"));
        if (optionalAuth0Logout.isPresent()) {
            this.auth0Logout = optionalAuth0Logout.get();
        } else {
            this.auth0Logout = "";
        }
        Optional<String> optionalAuth0ClientSecret = Optional.of(ninjaProperties.get("auth0.clientsecret"));
        if (optionalAuth0ClientSecret.isPresent()) {
            this.auth0ClientSecret = optionalAuth0ClientSecret.get();
        } else {
            this.auth0ClientSecret = "";
        }

    }

    public String getBibleSearchKey() {
        return bibleSearchKey;
    }

    public Integer getMaxVerses() {
        return maxVerses;
    }

    public Integer getThemesMaxCols() {
        return themesMaxCols;
    }

    public String getAuth0ClientId() {
        return auth0ClientId;
    }

    public String getAuth0Domain() {
        return auth0Domain;
    }

    public String getAuth0Callback() {
        return auth0Callback;
    }

    public String getAuth0Logout() {
        return auth0Logout;
    }

    public String getAuth0ClientSecret() {
        return auth0ClientSecret;
    }
}
