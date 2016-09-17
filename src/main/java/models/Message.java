package models;

import com.google.inject.Inject;
import com.google.inject.Provider;
import ninja.postoffice.Mail;
import utilities.Config;

import java.util.List;

public class Message {

    private String sender;
    private List<String> recipients;
    private String subject;
    private String bodyHtml;
    private String bodyText;
    private Mail mail;
    private String salutation;
    private boolean ignoreSalutation = false;

    private final Provider<Mail> emailProvider;
    private final Config config;

    @Inject
    public Message(Provider<Mail> emailProvider, Config config) {
        this.emailProvider = emailProvider;
        this.mail = this.emailProvider.get();
        this.config = config;
        this.setSender(config.getMailFrom());
        this.salutation = "Hi,<p></p>";
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
        this.mail.setFrom(sender);
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
        for (String recipient : recipients) {
            this.mail.addBcc(recipient);
        }
    }

    public void setRecipient(String recipient) {
        this.mail.addTo(recipient);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        this.mail.setSubject(subject);
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        if(!ignoreSalutation){
            bodyHtml = this.salutation + bodyHtml;
        }

        this.bodyHtml = bodyHtml;
        this.mail.setBodyHtml(bodyHtml);
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        if(!ignoreSalutation) {
            bodyText = this.salutation + bodyText;
        }
        this.bodyText = bodyText;
        this.mail.setBodyText(bodyText);
    }

    public void setSalutation(String name) {
        this.salutation = "Hi " + name + ",<p></p>";
    }

    public void setIgnoreSalutation(boolean ignoreSalutation) {
        this.ignoreSalutation = ignoreSalutation;
    }

    public Mail getMail() {
        return this.mail;
    }
}
