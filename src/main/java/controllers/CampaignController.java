package controllers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.google.inject.Singleton;

import models.Campaign;
import ninja.Result;
import ninja.Results;
import ninja.jpa.UnitOfWork;

@Singleton
public class CampaignController {
	@Inject 
	Provider<EntityManager> entitiyManagerProvider;
	
//	public Result campaign(){
//		return Results.html();
//	}
//	
	public Result campaignDetails(){
		return Results.html();
	}
	
	@UnitOfWork
	public Result campaign() {
	    EntityManager entityManager = entitiyManagerProvider.get();
	   // Query q = entityManager.createQuery("SELECT campaignId, startDate, endDate, themeId, campaignDays, campaignName FROM Campaign");
	    TypedQuery<Campaign> q = entityManager.createQuery("from Campaign", Campaign.class);
	    List<Campaign> campaignList = q.getResultList();
	    if(campaignList!=null){
	    	System.out.println("Size of campaign := "+campaignList.size()+ campaignList.get(0).getThemeList().size());
	    }

	    return Results
	            .html()
	            .render("campaignList", campaignList);


	}
}
