package controllers;

import com.google.inject.Provider;
import exceptions.CampaignException;
import filters.LoginFilter;
import filters.MemberFilter;
import filters.PublisherFilter;
import models.*;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AdminSettingsRepository;
import repositories.CampaignRepository;
import repositories.ThemeRepository;
import services.UserService;
import utilities.Config;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FilterWith(LoginFilter.class)
public class CampaignController {

    private final static Logger logger = LoggerFactory.getLogger(CampaignController.class);

    private final CampaignRepository campaignRepository;
    private final ThemeRepository themeRepository;
    private final Config config;
    private final UserService userService;
    private final Provider<Message> messageProvider;
    private final AdminSettingsRepository adminSettingsRepository;
    private final Messenger messenger;
    private AdminSettings adminSettings;
    private static final String CAMPAIGN_ID = "campaignId";

    @Inject
    public CampaignController(CampaignRepository campaignRepository, ThemeRepository themeRepository,
                              Config config, UserService userService, Provider<Message> messageProvider,
                              AdminSettingsRepository adminSettingsRepository, Messenger messenger) {
        this.campaignRepository = campaignRepository;
        this.themeRepository = themeRepository;
        this.config = config;
        this.userService = userService;
        this.messageProvider = messageProvider;
        this.adminSettingsRepository = adminSettingsRepository;
        this.messenger = messenger;
        this.adminSettings = this.adminSettingsRepository.findSettings();
    }

    /**
     * Displaying list of campaigns
     **/
    @FilterWith(MemberFilter.class)
    public Result campaignList(Context context) {

        User user = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));

        //Re-sort campaign list to move subscribed items to the front
        List<Campaign> campaignList = campaignRepository.findAll();
        List<Long> subscribedCampaignIds = user.getSubscriptions();
        List<Campaign> subscribedCampaigns = new ArrayList<>();

        if (!subscribedCampaignIds.isEmpty()) {
            for (Long id : subscribedCampaignIds) {
                Optional<Campaign> optionalCampaign = campaignList.stream()
                        .filter(item -> item.getCampaignId().equals(id))
                        .findFirst();

                if (optionalCampaign.isPresent()) {
                    subscribedCampaigns.add(optionalCampaign.get());
                    campaignList.remove(optionalCampaign.get());
                }
            }
        }

        String role = userService.getHighestRole(context.getSession().get(config.IDTOKEN_NAME));

        return Results.html().render("campaignList", campaignList)
                .render("themeList", themeRepository.findAll())
                .render("dateFormat", config.DATE_FORMAT)
                .render("subscribedCampaignList", subscribedCampaigns)
                .render("loggedIn", true)
                .render("role", role);
    }

    @FilterWith(MemberFilter.class)
    public Result subscribe(@PathParam(CAMPAIGN_ID) Long campaignId, Context context, Session session) {
        if (campaignId == null) {
            return Results.badRequest().text();
        }

        User user = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));
        Campaign campaign = campaignRepository.findCampaignById(campaignId);

        if (user == null) {
            return Results.badRequest().text();
        }

        if (userService.subscribe(user.getUser_id(), campaignId)) {
            userService.refreshUserProfileInCache(session);
            if (adminSettings != null && adminSettings.getId() == 1L) {
                Message message = messageProvider.get();
                message.setRecipient(user.getEmail());
                message.setSubject(adminSettings.getSubscribedSubject());
                message.setSalutation(user.getName());
                message.setBodyHtml(adminSettings.getSubscribedMessage() +
                        "<p>Name: " + campaign.getCampaignName() + "<br />" +
                        "Description: " + campaign.getCampaignDescription() + "</p><p></p>" +
                        adminSettings.getGenericMessageFooter());
                messenger.sendMessage(message);
            } else {
                logger.error("Admin settings not setup, campaign emails not set.");
            }
            return Results.ok().text();
        }

        return Results.badRequest().text();
    }

    @FilterWith(MemberFilter.class)
    public Result unsubscribe(@PathParam(CAMPAIGN_ID) Long campaignId, Context context, Session session) {
        if (campaignId == null) {
            return Results.badRequest().text();
        }

        User user = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));
        Campaign campaign = campaignRepository.findCampaignById(campaignId);

        if (user == null) {
            return Results.badRequest().text();
        }

        if (userService.unsubscribe(user.getUser_id(), campaignId)) {
            userService.refreshUserProfileInCache(session);
            //this notification should actually be in unsubscribe function of the userservice
            if (adminSettings != null && adminSettings.getId() == 1L) {
                Message message = messageProvider.get();
                message.setRecipient(user.getEmail());
                message.setSubject(adminSettings.getUnsubscribedSubject());
                message.setSalutation(user.getName());
                message.setBodyHtml(adminSettings.getUnsubscribedMessage() + "<p>Name: " + campaign.getCampaignName() + "</p><p></p>" + adminSettings.getGenericMessageFooter());
                messenger.sendMessage(message);
            } else {
                logger.error("Admin settings not setup, campaign emails not set.");
            }
            return Results.ok().text();
        }

        return Results.badRequest().text();

    }

    /**
     * Adding new campaign
     **/
    @FilterWith(PublisherFilter.class)
    public Result addCampaign(Context context) {

        String role = userService.getHighestRole(context.getSession().get(config.IDTOKEN_NAME));

        return Results.html()
                .render("themes", themeRepository.findAll())
                .render("dateFormat", config.DATE_FORMAT)
                .render("loggedIn", true)
                .render("role", role);
    }

    /**
     * Saving new campaign
     **/
    @FilterWith(PublisherFilter.class)
    public Result saveCampaign(Context context, Campaign campaign, FlashScope flashScope) {
        DateFormat formatter = new SimpleDateFormat(config.DATE_FORMAT);

        List<String> themeIds = context.getParameterValues("themes");
        if (themeIds.isEmpty()) {
            campaign.setThemeList(new ArrayList<Theme>());
        }

        try {
            campaign.setStartDate(new Timestamp(formatter.parse(context.getParameter("startDate")).getTime()));
            campaign.setEndDate(new Timestamp(formatter.parse(context.getParameter("endDate")).getTime()));
        } catch (ParseException e) {
            flashScope.error("Error creating campaign. Contact the administrator.");
            logger.error("Error parsing date in save campaign" + e.getMessage());
        }

        long duration = campaign.getEndDate().getTime() - campaign.getStartDate().getTime();
        int days = (int) (duration / (1000 * 60 * 60 * 24));

        campaign.setCampaignDays(days);
        campaign.setSendTime(context.getParameter("sendTime"));

        List<Theme> themeList = new ArrayList<>();
        for (String themeId : themeIds) {
            Theme theme = themeRepository.findById(Long.parseLong(themeId));
            themeList.add(theme);
        }
        campaign.setThemeList(themeList);

        try {
            campaignRepository.save(campaign);

            if (adminSettings != null && adminSettings.getId() == 1L) {
                Message message = messageProvider.get();
                message.setSubject(adminSettings.getNewCampaignSubject());
                message.setBodyHtml(adminSettings.getNewCampaignMessage() +
                        "<p>Name: " + campaign.getCampaignName() + "<br />" +
                        "Description: " + campaign.getCampaignDescription() + "</p><p></p>" +
                        adminSettings.getGenericMessageFooter());
                userService.sendNotificationToUsers(message);
            } else {
                logger.error("Admin settings not setup, campaign emails not set.");
            }
            flashScope.success("Campaign successfully created");
        } catch (CampaignException e) {
            flashScope.error("Error creating campaign. Contact the administrator.");
            logger.error("Error in save campaign" + e.getMessage());
        }

        return Results.redirect("/campaign/list");
    }

    /**
     * Rendering campaign for a particular campaign Id which needs to be updated
     **/
    @FilterWith(PublisherFilter.class)
    public Result updateCampaign(@PathParam(CAMPAIGN_ID) Long campaignId, Context context) {
        logger.info("Updating campaign details of campaign: " + campaignId);

        String role = userService.getHighestRole(context.getSession().get(config.IDTOKEN_NAME));

        return Results.html()
                .render("campaign", campaignRepository.findCampaignById(campaignId))
                .render("themes", themeRepository.findAll())
                .render("dateFormat", config.DATE_FORMAT)
                .render("loggedIn", true)
                .render("role", role);
    }

    /**
     * Saving updated campaign
     *
     * @param context
     * @return
     */
    @FilterWith(PublisherFilter.class)
    public Result saveUpdatedCampaign(Context context, FlashScope flashScope) {
        DateFormat formatter = new SimpleDateFormat(config.DATE_FORMAT);
        Timestamp startDate = null, endDate = null;

        try {
            startDate = new Timestamp(formatter.parse(context.getParameter("startDate")).getTime());
            endDate = new Timestamp(formatter.parse(context.getParameter("endDate")).getTime());
        } catch (ParseException e) {
            flashScope.error("Error updating campaign. Contact the administrator.");
            logger.error("Error parsing date in save campaign" + e.getMessage());
        }

        long duration = endDate.getTime() - startDate.getTime();
        int days = (int) (duration / (1000 * 60 * 60 * 24));

        List<String> themeIds = context.getParameterValues("themeList");
        List<Theme> themeList = new ArrayList<>();

        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeRepository.findById(Long.parseLong(themeId));
                themeList.add(theme);
            }
        }
        logger.info("Themes: " + themeList.toString());
        try {
            campaignRepository.update(Long.parseLong(context.getParameter(CAMPAIGN_ID)), context.getParameter("campaignName"), context.getParameter("campaignDescription"),
                    startDate, endDate, days, themeList, context.getParameter("sendTime"));
            flashScope.success("Campaign updated");
        } catch (CampaignException e) {
            flashScope.error("Error updating campaign. Contact the administrator.");
            logger.error("Error in updating campaign" + e.getMessage());
        }
        return Results.redirect("/campaign/list");
    }

    @FilterWith(PublisherFilter.class)
    public Result deleteCampaign(@PathParam(CAMPAIGN_ID) Long campaignId, FlashScope flashScope) {

        try {
            campaignRepository.deleteCampaign(campaignId);
            flashScope.success("Campaign deleted successfully.");
            //TODO: Send notification

            if (userService.unsubscribeAll(campaignId)) {
                logger.info("Successfully unsubscribed all users from campaign: " + campaignId);
            } else {
                logger.info("No active subscriptions to remove from campaign: " + campaignId);
            }
        } catch (CampaignException e) {
            flashScope.error("Campaign, trying to delete, doesn't exist");
            logger.error("Error in deleting campaign" + e.getMessage());
        }

        return Results.redirect("/campaign/list");
    }

}
