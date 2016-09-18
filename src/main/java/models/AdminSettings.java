package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AdminSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String version;
    private String welcomeSubject;
    private String welcomeMessage;
    private String newCampaignSubject;
    private String newCampaignMessage;
    private String subscribedSubject;
    private String subscribedMessage;
    private String unsubscribedSubject;
    private String unsubscribedMessage;
    private String newVotdSubmittedSubject;
    private String newVotdSubmittedMessage;
    private String genericMessageFooter;

    public AdminSettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String translation) {
        this.version = translation;
    }

    public String getWelcomeSubject() {
        return welcomeSubject;
    }

    public void setWelcomeSubject(String welcomSubject) {
        this.welcomeSubject = welcomSubject;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getNewCampaignSubject() {
        return newCampaignSubject;
    }

    public void setNewCampaignSubject(String newCampaignSubject) {
        this.newCampaignSubject = newCampaignSubject;
    }

    public String getNewCampaignMessage() {
        return newCampaignMessage;
    }

    public void setNewCampaignMessage(String newCampaignMessage) {
        this.newCampaignMessage = newCampaignMessage;
    }

    public String getSubscribedSubject() {
        return subscribedSubject;
    }

    public void setSubscribedSubject(String subscribedSubject) {
        this.subscribedSubject = subscribedSubject;
    }

    public String getSubscribedMessage() {
        return subscribedMessage;
    }

    public void setSubscribedMessage(String subscribedMessage) {
        this.subscribedMessage = subscribedMessage;
    }

    public String getUnsubscribedSubject() {
        return unsubscribedSubject;
    }

    public void setUnsubscribedSubject(String unsubscribedSubject) {
        this.unsubscribedSubject = unsubscribedSubject;
    }

    public String getUnsubscribedMessage() {
        return unsubscribedMessage;
    }

    public void setUnsubscribedMessage(String unsubscribedMessage) {
        this.unsubscribedMessage = unsubscribedMessage;
    }

    public String getNewVotdSubmittedSubject() {
        return newVotdSubmittedSubject;
    }

    public void setNewVotdSubmittedSubject(String newVotdSubmittedSubject) {
        this.newVotdSubmittedSubject = newVotdSubmittedSubject;
    }

    public String getNewVotdSubmittedMessage() {
        return newVotdSubmittedMessage;
    }

    public void setNewVotdSubmittedMessage(String newVotdSubmittedMessage) {
        this.newVotdSubmittedMessage = newVotdSubmittedMessage;
    }

    public String getGenericMessageFooter() {
        return genericMessageFooter;
    }

    public void setGenericMessageFooter(String genericMessageFooter) {
        this.genericMessageFooter = genericMessageFooter;
    }

    @Override
    public String toString() {
        return "AdminSettings{" +
                "id=" + id +
                ", translation='" + version + '\'' +
                ", welcomeSubject='" + welcomeSubject + '\'' +
                ", welcomeMessage='" + welcomeMessage + '\'' +
                ", newCampaignSubject='" + newCampaignSubject + '\'' +
                ", newCampaignMessage='" + newCampaignMessage + '\'' +
                ", subscribedSubject='" + subscribedSubject + '\'' +
                ", subscribedMessage='" + subscribedMessage + '\'' +
                ", unsubscribedSubject='" + unsubscribedSubject + '\'' +
                ", unsubscribedMessage='" + unsubscribedMessage + '\'' +
                ", newVotdSubmittedSubject='" + newVotdSubmittedSubject + '\'' +
                ", newVotdSubmittedMessage='" + newVotdSubmittedMessage + '\'' +
                ", genericMessageFooter='" + genericMessageFooter + '\'' +
                '}';
    }
}
