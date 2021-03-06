package models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ninja.scheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AdminSettingsRepository;
import services.UserService;
import services.VotdDispatchService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class VotdScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VotdScheduler.class);

    @Inject
    private VotdDispatchService votdDispatchService;
    @Inject
    private Messenger messenger;
    @Inject
    private Provider<Message> messageProvider;
    @Inject
    private UserService userService;
    @Inject
    private AdminSettingsRepository adminSettingsRepository;

    @Schedule(delay = 60, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void dispatchAvailableCampaigns() {

        LocalTime localTime = LocalTime.now();
        String currentTimeMinutes = localTime.format(DateTimeFormatter.ofPattern("mm"));

        //Only run every hour on the hour
        if (!currentTimeMinutes.contentEquals("00")) {
            return;
        }

        List<Campaign> activeCampaigns = votdDispatchService.getActiveCampaigns();

        if (activeCampaigns.isEmpty()) {
            logger.info("No active campaigns at this time.");
            return;
        }

        Integer numberOfUsersPerPage = 100;

        for (Campaign campaign : activeCampaigns) {
            logger.info("Processing: " + campaign.getCampaignName());

            VotdDispatch votdDispatch = new VotdDispatch();
            votdDispatch.setTotalNumberOfUsers(votdDispatchService
                    .getUsers(0, numberOfUsersPerPage, campaign.getCampaignId())
                    .get("total").getAsInt());

            if (votdDispatch.getTotalNumberOfUsers() == 0) {
                logger.info("No subscribers to this campaign. Will not process anything else for this campaign");
                return;
            }

            logger.info("Retrieved subscribed users: " + votdDispatch.getTotalNumberOfUsers());

            votdDispatch.setCampaign(campaign);
            Votd verseToSend = votdDispatchService.getVerseToSend(campaign);

            if (verseToSend == null) {
                logger.info("No applicable verse to send.");
                return;
            }

            //String messageSubject = verseToSend.getVerses();
            String messageSubject = "Daily Verse(s) from campaign: " + campaign.getCampaignName();

            votdDispatch.setVotdToBeDispatched(verseToSend);
            Integer pages = votdDispatch.getVotdDispatchUserPages(numberOfUsersPerPage);

            logger.info("Retrieved verse: " + votdDispatch.getVotdToBeDispatched().getVerses());

            try {
                List<Message> messages = new ArrayList<>();

                for (Integer i = 0; i < pages; i++) {
                    JsonObject users = votdDispatchService.getUsers(i, numberOfUsersPerPage, campaign.getCampaignId());
                    JsonArray userJsonList = users.getAsJsonArray("users");
                    Gson gson = new Gson();
                    User[] userArray = gson.fromJson(userJsonList, User[].class);
                    List<User> userList = Arrays.asList(userArray);

                    //Group users by the bible version they want
                    Map<String, List<User>> userVersion = userList.stream()
                            .collect(Collectors.groupingBy(this::getUserVersion));

                    //For each bible version send an email to all associated recipients
                    userVersion.forEach((k, v) -> {
                                List<String> recipients = new ArrayList<>();
                                Message message = messageProvider.get();
                                message.setIgnoreSalutation(true);
                                message.setSubject(messageSubject);
                                String verseToSendText = votdDispatchService.getVerseText(votdDispatch.getVotdToBeDispatched(), k);
                                message.setBodyHtml(verseToSendText);
                                recipients.addAll(v.stream().map(User::getEmail).collect(Collectors.toList()));
                                logger.debug("version: " + k + " users: " + recipients);
                                message.setRecipients(recipients);
                                messages.add(message);
                            }
                    );

                }
                messenger.setMessages(messages);
                messenger.sendMessages();

                votdDispatch.setTimeFinished();
            } catch (JsonSyntaxException je) {
                logger.error("Failed to retrieve the verse text.");
                List<String> admins = userService.findEmailsByRole("admin");
                Message message1 = messageProvider.get();
                message1.setSubject("Error retrieving verse text");
                message1.setRecipients(admins);
                message1.setBodyHtml(je.getMessage());
                messenger.sendMessage(message1);
            }

        }

    }

    /**
     * Get the bible version the user has set or the default version if not set.
     *
     * @param user
     * @return
     */
    private String getUserVersion(User user) {
        String defaultVersion = adminSettingsRepository.findSettings().getVersion();
        if (user.getSettings().isEmpty()) {
            return defaultVersion;
        } else if (user.getSettings().get("version") == null) {
            return defaultVersion;
        } else {
            return user.getSettings().get("version");
        }
    }

}
