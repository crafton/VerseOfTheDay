package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ninja.scheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import services.VotdDispatchService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class VotdScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VotdScheduler.class);

    @Inject
    private VotdDispatchService votdDispatchService;
    @Inject
    private Messenger messenger;
    @Inject
    private Provider<Message> messageProvider;

    @Schedule(delay = 60, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void dispatchAvailableCampaigns() {

        List<Campaign> activeCampaigns = votdDispatchService.getActiveCampaigns();

        if (activeCampaigns.isEmpty()) {
            logger.info("No active campaigns at this time.");
            return;
        }

        Integer numberOfUsersPerPage = 40;

        for (Campaign campaign : activeCampaigns) {
            logger.info("Processing: " + campaign.getCampaignName());

            VotdDispatch votdDispatch = new VotdDispatch();
            votdDispatch.setTotalNumberOfUsers(votdDispatchService
                    .getUsers(0, numberOfUsersPerPage, campaign.getCampaignId())
                    .get("total").getAsInt());

            if(votdDispatch.getTotalNumberOfUsers() == 0){
                logger.info("No subscribers to this campaign. Will not process anything else for this campaign");
                return;
            }

            logger.info("Retrieved subscribed users: " + votdDispatch.getTotalNumberOfUsers());

            votdDispatch.setCampaign(campaign);
            Votd verseToSend = votdDispatchService.getVerseToSend(campaign);

            if(verseToSend == null){
                logger.info("No applicable verse to send.");
                return;
            }

            Message message = messageProvider.get();
            message.setSubject(verseToSend.getVerses());

            votdDispatch.setVotdToBeDispatched(verseToSend);
            Integer pages = votdDispatch.getVotdDispatchUserPages(numberOfUsersPerPage);

            logger.info("Retrieved verse: " + votdDispatch.getVotdToBeDispatched().getVerses());

            try {
                //Retrieve the text related to the verse that will be sent
                String verseToSendText = votdDispatchService.getVerseText(votdDispatch.getVotdToBeDispatched());
                List<Message> messages = new ArrayList<>();
                message.setBodyHtml(verseToSendText);

                for (Integer i = 0; i < pages; i++) {
                    JsonObject users = votdDispatchService.getUsers(i, numberOfUsersPerPage, campaign.getCampaignId());
                    JsonArray userJsonList = users.getAsJsonArray("users");
                    List<String> recipients = new ArrayList<>();
                    for (JsonElement user : userJsonList) {
                        String email = user.getAsJsonObject().get("email").getAsString();
                        recipients.add(email);
                        logger.info("Sending " + votdDispatch.getVotdToBeDispatched().getVerses() + " to " + email);
                    }
                    message.setRecipients(recipients);
                    messages.add(message);
                }

                messenger.setMessages(messages);
                messenger.sendMessages();

                votdDispatch.setTimeFinished();
            }catch (JsonSyntaxException je){
                logger.error("Failed to retrieve the verse text.");
                //TODO: send notification to admin
            }

        }

    }
}
