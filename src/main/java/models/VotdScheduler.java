package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.scheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import services.VotdDispatchService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class VotdScheduler {

    @Inject
    private VotdDispatchService votdDispatchService;
    private static final Logger logger = LoggerFactory.getLogger(VotdScheduler.class);

    @Schedule(delay = 10, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
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
                logger.info("No subscribers to this campaign. Will no process anything else for this campaign");
                return;
            }
            
            logger.info("Retrieved subscribed users: " + votdDispatch.getTotalNumberOfUsers());

            votdDispatch.setCampaign(campaign);
            Votd verseToSend = votdDispatchService.getVerseToSend(campaign);

            if(verseToSend == null){
                logger.info("No applicable verse to send.");
                return;
            }

            votdDispatch.setVotdToBeDispatched(verseToSend);
            Integer pages = votdDispatch.getVotdDispatchUserPages(numberOfUsersPerPage);

            logger.info("Retrieved verse: " + votdDispatch.getVotdToBeDispatched().getVerses());
            for (Integer i = 0; i < pages; i++) {
                JsonObject users = votdDispatchService.getUsers(i, numberOfUsersPerPage, campaign.getCampaignId());
                JsonArray userJsonList = users.getAsJsonArray("users");
                for (JsonElement user : userJsonList) {
                    String email = user.getAsJsonObject().get("email").getAsString();
                    logger.info("Sending " + votdDispatch.getVotdToBeDispatched().getVerses() + " to " + email);
                }
            }

            votdDispatch.setTimeFinished();
        }

    }
}
