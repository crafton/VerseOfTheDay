package models;


import com.google.inject.Inject;
import ninja.postoffice.Postoffice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Messenger {

    private final static Logger logger = LoggerFactory.getLogger(Messenger.class);

    private List<Message> messages;
    private final Postoffice postOffice;

    @Inject
    public Messenger(Postoffice postOffice) {
        this.postOffice = postOffice;
    }

    public void sendMessages() {
        for (Message message : messages) {
            sendMessage(message);
        }
    }

    public void sendMessage(Message message){
        try {
            postOffice.send(message.getMail());
        } catch (Exception e) {
            logger.error("Problem sending message: " + e.getMessage());
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

}
