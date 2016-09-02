package utilities;

import com.google.inject.Inject;
import ninja.utils.NinjaProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Config {

    private String bibleSearchKey;
    private Integer maxVerses;
    private Integer themesMaxCols;
    private String auth0ClientId;
    private String auth0Domain;
    private String auth0Callback;
    private String auth0Logout;
    private String auth0ClientSecret;
    private String auth0MgmtToken;
    private String auth0UserApi;

    private String memberRole;
    private String contributorRole;
    private String publisherRole;
    private String memberDescription;
    private String contributorDescription;
    private String publisherDescription;
    private List<String> rolesList;

    private String contributedVotdMailFrom;
    private String contributedVotdMailHtmlBody;
    private String contributedVotdMailTextBody;
    private String contributedVotdMailSubject;
    private String contributedVotdAddress;

    public final String APPROVED = "Approved";
    public final String PENDING = "Pending";
    public final String DATE_FORMAT = "EEEEE, MMMMM d, yyyy hh:mm aaa";
    public final String IDTOKEN_NAME = "idToken";

    @Inject
    private Config(NinjaProperties ninjaProperties) {
        this.maxVerses = ninjaProperties.getIntegerWithDefault("votd.maxverses", 0);

        this.bibleSearchKey = ninjaProperties.getWithDefault("biblesearch.key", "");
        this.themesMaxCols = ninjaProperties.getIntegerWithDefault("themes.maxcols", 7);
        this.auth0ClientId = ninjaProperties.getWithDefault("auth0.clientid", "");
        this.auth0Domain = ninjaProperties.getWithDefault("auth0.domain", "");
        this.auth0Callback = ninjaProperties.getWithDefault("auth0.callback", "");
        this.auth0Logout = ninjaProperties.getWithDefault("auth0.logout", "");
        this.auth0ClientSecret = ninjaProperties.getWithDefault("auth0.clientsecret", "");
        this.auth0MgmtToken = ninjaProperties.getWithDefault("auth0.mgmttoken", "");
        this.auth0UserApi = ninjaProperties.getWithDefault("auth0.userapi", "");

        this.memberRole = ninjaProperties.getWithDefault("role.membername", "");
        this.memberDescription = ninjaProperties.getWithDefault("role.memberdescription", "");
        this.contributorRole = ninjaProperties.getWithDefault("role.contributorname", "");
        this.contributorDescription = ninjaProperties.getWithDefault("role.contributordescription", "");
        this.publisherRole = ninjaProperties.getWithDefault("role.publishername", "");
        this.publisherDescription = ninjaProperties.getWithDefault("role.publisherdescription", "");

        this.contributedVotdMailFrom = ninjaProperties.getWithDefault("mail.votdcontributed.from", "");
        this.contributedVotdMailSubject = ninjaProperties.getWithDefault("mail.votdcontributed.subject", "");
        this.contributedVotdMailHtmlBody = ninjaProperties.getWithDefault("mail.votdcontributed.htmlbody", "");
        this.contributedVotdMailTextBody = ninjaProperties.getWithDefault("mail.votdcontributed.textbody", "");
        this.contributedVotdAddress = ninjaProperties.getWithDefault("mail.votdcontributed.address", "");

        this.rolesList = Arrays.asList(this.memberRole, this.contributorRole, this.publisherRole);


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

    public String getAuth0MgmtToken() {
        return auth0MgmtToken;
    }

    public String getAuth0UserApi() {
        return auth0UserApi;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public String getContributorRole() {
        return contributorRole;
    }

    public String getPublisherRole() {
        return publisherRole;
    }

    public String getMemberDescription() {
        return memberDescription;
    }

    public String getContributorDescription() {
        return contributorDescription;
    }

    public String getPublisherDescription() {
        return publisherDescription;
    }

    public List<String> getRolesList() {
        return rolesList;
    }

    public String getContributedVotdMailFrom() {
        return contributedVotdMailFrom;
    }

    public String getContributedVotdMailHtmlBody() {
        return contributedVotdMailHtmlBody;
    }

    public String getContributedVotdMailTextBody() {
        return contributedVotdMailTextBody;
    }

    public String getContributedVotdMailSubject() {
        return contributedVotdMailSubject;
    }

    public String getContributedVotdAddress() {
        return contributedVotdAddress;
    }
}
