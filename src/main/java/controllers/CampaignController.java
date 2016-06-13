package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import daos.CampaignDao;
import daos.ThemeDao;
import models.Campaign;
import models.Theme;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.PathParam;

@Singleton
public class CampaignController {
	@Inject
	Router router;
	public static int tt = 0;
	final static Logger logger = LoggerFactory.getLogger(CampaignController.class);
	@Inject
	CampaignDao campaignDao;

	@Inject
	ThemeDao themeDao;

	/** Displaying list of campaigns **/
	public Result campaignList() {
		return Results.html().render("campaignList", campaignDao.getCampaignList()).render("themeList",
				themeDao.getThemeList());
	}
	
	/**
	 * Adding new campaign
	 **/
	public Result addCampaign() {
		return Results.html().render("themes", themeDao.getThemeList());
	}

	/**
	 * Saving new campaign
	 **/
	public Result saveCampaign(Context context, Campaign campaign) {
		
		List<String> themeIds = context.getParameterValues("themes");
		if (themeIds.isEmpty()) {
			campaign.setThemeList(new ArrayList<Theme>());
		}

		List<Theme> themeList = new ArrayList<>();
		for (String themeId : themeIds) {
			Theme theme = themeDao.getThemeById(themeId);
			themeList.add(theme);
		}
		campaign.setThemeList(themeList);
		campaignDao.save(campaign);

		return Results.redirect("/campaign/list");
	}
	
	/**
	 * Rendering campaign for a particular campaign Id which needs to be updated
	 **/
	public Result updateCampaign(@PathParam("campaignId") String campaignId) {
		logger.info("Updating campaign details of campaign: =" + campaignId);
		System.out.println("Updating campaign details of campaign: =" + campaignId);
		return Results.html().render("campaign", campaignDao.getCampaignById(campaignId)).render("themes", themeDao.getThemeList());
	}

	/**
	 * Saving updated campaign
	 * @param context
	 * @return
	 */
	public Result saveUpdatedCampaign(Context context) {
		System.out.println(context.getParameter("campaignId"));
		System.out.println(context.getParameter("campaignName"));
		System.out.println(context.getParameter("startDate"));
		System.out.println(context.getParameter("endDate"));
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = null, endDate = null;
		try {
			startDate = formatter.parse(context.getParameter("startDate"));
			endDate = formatter.parse(context.getParameter("endDate"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> themeIds = context.getParameterValues("themeList");
		List<Theme> themeList = new ArrayList<>();

		if (!themeIds.isEmpty()) {
			for (String themeId : themeIds) {
				Theme theme = themeDao.getThemeById(themeId);
				themeList.add(theme);
			}
		}
		try {
			campaignDao.update(context.getParameter("campaignId"), context.getParameter("campaignName"), startDate,
					endDate, themeList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Results.redirect("/campaign/list");
	}
	
	  public Result deleteCampaign(@PathParam("campaignId") String campaignId) {

	        try {
	        	campaignDao.deleteCampaign(campaignId);;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return Results.redirect("/campaign/list");
	    }


}
