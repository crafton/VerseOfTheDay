package controllers;

import com.google.gson.Gson;
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
import services.CampaignService;
import services.ThemeService;
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

    private final CampaignService campaignService;
    private final ThemeService themeService;
    private final Config config;
    private final UserService userService;
    private final Provider<Message> messageProvider;
    private final AdminSettingsRepository adminSettingsRepository;
    private final Messenger messenger;
    private AdminSettings adminSettings;
    private static final String CAMPAIGN_ID = "campaignId";

    @Inject
    public CampaignController(CampaignService campaignService, ThemeService themeService,
                              Config config, UserService userService, Provider<Message> messageProvider,
                              AdminSettingsRepository adminSettingsRepository, Messenger messenger) {
        this.campaignService = campaignService;
        this.themeService = themeService;
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

        String userAsJsonString = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));
        Gson gson = new Gson();
        User user = gson.fromJson(userAsJsonString, User.class);

        //Re-sort campaign list to move subscribed items to the front
        List<Campaign> campaignList = campaignService.getCampaignList();
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

        return Results.html().render("campaignList", campaignList)
                .render("themeList", themeService.findAllThemes())
                .render("dateFormat", config.DATE_FORMAT)
                .render("subscribedCampaignList", subscribedCampaigns);
    }

    @FilterWith(MemberFilter.class)
    public Result subscribe(@PathParam(CAMPAIGN_ID) Long campaignId, Context context, Session session) {
        if (campaignId == null) {
            return Results.badRequest().text();
        }

        String userString = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));

        if (userString == null || userString.isEmpty()) {
            return Results.badRequest().text();
        }

        Gson gson = new Gson();
        User user = gson.fromJson(userString, User.class);

        if (userService.subscribe(user.getUser_id(), campaignId)) {
            userService.refreshUserProfileInCache(session);
            Message message = messageProvider.get();
            message.setRecipient(user.getEmail());
            message.setSubject(adminSettings.getSubscribedSubject());
            message.setSalutation(user.getName());
            message.setBodyHtml(adminSettings.getSubscribedMessage());
            messenger.sendMessage(message);
            return Results.ok().text();
        }

        return Results.badRequest().text();
    }

    @FilterWith(MemberFilter.class)
    public Result unsubscribe(@PathParam(CAMPAIGN_ID) Long campaignId, Context context, Session session) {
        if (campaignId == null) {
            return Results.badRequest().text();
        }

        String userAsString = userService.getCurrentUser(context.getSession().get(config.IDTOKEN_NAME));

        if (userAsString == null || userAsString.isEmpty()) {
            return Results.badRequest().text();
        }

        Gson gson = new Gson();
        User user = gson.fromJson(userAsString, User.class);

        if (userService.unsubscribe(user.getUser_id(), campaignId)) {
            userService.refreshUserProfileInCache(session);
            Message message = messageProvider.get();
            message.setRecipient(user.getEmail());
            message.setSubject(adminSettings.getUnsubscribedSubject());
            message.setSalutation(user.getName());
            message.setBodyHtml(adminSettings.getUnsubscribedMessage());
            messenger.sendMessage(message);
            return Results.ok().text();
        }

        return Results.badRequest().text();

    }

    /**
     * Adding new campaign
     **/
    @FilterWith(PublisherFilter.class)
    public Result addCampaign() {
        return Results.html()
                .render("themes", themeService.findAllThemes())
                .render("dateFormat", config.DATE_FORMAT);
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
            Theme theme = themeService.findThemeById(Long.parseLong(themeId));
            themeList.add(theme);
        }
        campaign.setThemeList(themeList);

        try {
            campaignService.save(campaign);
            Message message = messageProvider.get();
            message.setSubject(adminSettings.getNewCampaignSubject());
            message.setBodyHtml(adminSettings.getNewCampaignMessage());
            userService.sendNotificationToUsers(message);
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
    public Result updateCampaign(@PathParam(CAMPAIGN_ID) Long campaignId) {
        logger.info("Updating campaign details of campaign: " + campaignId);

        return Results.html()
                .render("campaign", campaignService.getCampaignById(campaignId))
                .render("themes", themeService.findAllThemes())
                .render("dateFormat", config.DATE_FORMAT);
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
                Theme theme = themeService.findThemeById(Long.parseLong(themeId));
                themeList.add(theme);
            }
        }
        try {
            campaignService.update(Long.parseLong(context.getParameter(CAMPAIGN_ID)), context.getParameter("campaignName"), context.getParameter("campaignDescription"),
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
            campaignService.deleteCampaign(campaignId);
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
