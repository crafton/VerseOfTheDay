package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.scheduler.Schedule;
import services.VotdDispatchService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class VotdScheduler {

    @Inject
    private VotdDispatchService votdDispatchService;

    @Schedule(delay = 60, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void dispatchAvailableCampaigns() {

        List<Campaign> activeCampaigns = votdDispatchService.getActiveCampaigns();
        Integer numberOfUsersPerPage = 40;

        for (Campaign campaign : activeCampaigns) {
            VotdDispatch votdDispatch = new VotdDispatch();
            votdDispatch.setTotalNumberOfUsers(votdDispatchService
                    .getUsers(0, numberOfUsersPerPage, campaign.getCampaignId())
                    .get("total").getAsInt());
            votdDispatch.setCampaign(campaign);
            votdDispatch.setVotdToBeDispatched(votdDispatchService.getVerseToSend(campaign));
            Integer pages = votdDispatch.getVotdDispatchUserPages(numberOfUsersPerPage);

            for (Integer i = 0; i < pages; i++) {
                JsonObject users = votdDispatchService.getUsers(i, numberOfUsersPerPage, campaign.getCampaignId());
                JsonArray userJsonList = users.getAsJsonArray("users");
                for (JsonElement user : userJsonList) {
                    String email = user.getAsJsonObject().get("email").getAsString();
                    System.out.println("Sending " + votdDispatch.getVotdToBeDispatched() + " to " + email);
                }
            }

            votdDispatch.setTimeFinished();
        }

    }
}
