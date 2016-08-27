package controllers;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import services.CampaignService;
import services.ThemeService;
import exceptions.CampaignException;
import models.Campaign;
import models.Theme;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import utilities.Config;

public class CampaignController {

    private final static Logger logger = LoggerFactory.getLogger(CampaignController.class);

    private final CampaignService campaignService;
    private final ThemeService themeService;
    private final Config config;

    @Inject
    public CampaignController(CampaignService campaignService, ThemeService themeService, Config config) {
        this.campaignService = campaignService;
        this.themeService = themeService;
        this.config = config;
    }

    /**
     * Displaying list of campaigns
     **/
    public Result campaignList() {

        return Results.html().render("campaignList", campaignService.getCampaignList())
                .render("themeList", themeService.findAllThemes())
                .render("dateFormat", config.DATE_FORMAT);
    }

    /**
     * Adding new campaign
     **/
    public Result addCampaign() {
        return Results.html()
                .render("themes", themeService.findAllThemes())
                .render("dateFormat", config.DATE_FORMAT);
    }

    /**
     * Saving new campaign
     **/
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

        List<Theme> themeList = new ArrayList<>();
        for (String themeId : themeIds) {
            Theme theme = themeService.findThemeById(Long.parseLong(themeId));
            themeList.add(theme);
        }
        campaign.setThemeList(themeList);

        try {
            campaignService.save(campaign);
            flashScope.success("Campaign succesfully created");
        } catch (CampaignException e) {
            flashScope.error("Error creating campaign. Contact the administrator.");
            logger.error("Error in save campaign" + e.getMessage());
        }

        return Results.redirect("/campaign/list");
    }

    /**
     * Rendering campaign for a particular campaign Id which needs to be updated
     **/
    public Result updateCampaign(@PathParam("campaignId") Long campaignId) {
        logger.info("Updating campaign details of campaign: =" + campaignId);
        System.out.println("Updating campaign details of campaign: =" + campaignId);
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
    public Result saveUpdatedCampaign(Context context, FlashScope flashScope) {
        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        Timestamp startDate = null, endDate = null;
        try {
            startDate = new Timestamp(formatter.parse(context.getParameter("startDate")).getTime());
            endDate = new Timestamp(formatter.parse(context.getParameter("endDate")).getTime());
        } catch (ParseException e) {
            flashScope.error("Error updating campaign. Contact the administrator.");
            logger.error("Error parsing date in save campaign" + e.getMessage());
        }
        List<String> themeIds = context.getParameterValues("themeList");
        List<Theme> themeList = new ArrayList<>();

        if (!themeIds.isEmpty()) {
            for (String themeId : themeIds) {
                Theme theme = themeService.findThemeById(Long.parseLong(themeId));
                themeList.add(theme);
            }
        }
        try {
            campaignService.update(Long.parseLong(context.getParameter("campaignId")), context.getParameter("campaignName"),
                    startDate, endDate, themeList);
            flashScope.success("Campaign updated");
        } catch (CampaignException e) {
            flashScope.error("Error updating campaign. Contact the administrator.");
            logger.error("Error in updating campaign" + e.getMessage());
        }
        return Results.redirect("/campaign/list");
    }

    public Result deleteCampaign(@PathParam("campaignId") Long campaignId, FlashScope flashScope) {

        try {
            campaignService.deleteCampaign(campaignId);
            flashScope.success("Campaign deleted successfully.");
        } catch (CampaignException e) {
            flashScope.error("Campaign, trying to delete, doesn't exist");
            logger.error("Error in deleting campaign" + e.getMessage());
        }

        return Results.redirect("/campaign/list");
    }

}
