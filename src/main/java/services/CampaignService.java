package services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.persist.Transactional;

import exceptions.CampaignException;
import models.Campaign;
import models.Theme;

public class CampaignService {
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Transactional
	public List<Campaign> getCampaignList() {
		Query q = getEntityManager().createNamedQuery("Campaign.findAll");
		return q.getResultList();
	}

	@Transactional
	public Campaign getCampaignById(Long campaignId) throws IllegalArgumentException {

		if (campaignId == null) {
			throw new IllegalArgumentException("Parameter must be of type 'Long'.");
		}

		return getEntityManager().find(Campaign.class, campaignId);
	}

	@Transactional
	public void save(Campaign campaign) throws CampaignException {
		getEntityManager().persist(campaign);
	}

	@Transactional
	public void update(Long campaignId, String campaignName, Timestamp startDate, Timestamp endDate,
			List<Theme> themeList) throws CampaignException {

		if (themeList == null) {
			themeList = new ArrayList<>();
		}

		Campaign campaign = getCampaignById(campaignId);

		if (campaign == null) {
			throw new CampaignException("The campaign you're trying to update does not exist.");
		}
		campaign.setCampaignName(campaignName);
		campaign.setStartDate(startDate);
		campaign.setEndDate(endDate);
		campaign.setThemeList(themeList);
		getEntityManager().persist(campaign);
	}

	@Transactional
	public void deleteCampaign(Long campaignId) throws CampaignException {
		try {
			Campaign campaign = getCampaignById(campaignId);

			if (campaign == null) {
				throw new CampaignException("The campaign you're trying to update does not exist.");
			}

			getEntityManager().remove(campaign);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private EntityManager getEntityManager() {
		return entityManagerProvider.get();
	}
}
