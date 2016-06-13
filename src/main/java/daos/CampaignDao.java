package daos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.persist.Transactional;

import models.Campaign;
import models.Theme;

public class CampaignDao {
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Transactional
	public List<Campaign> getCampaignList() {
		TypedQuery<Campaign> q = getEntityManager().createQuery("from Campaign", Campaign.class);
		return q.getResultList();
	}

	@Transactional
	public Campaign getCampaignById(String campaignId) throws IllegalArgumentException {

		if (campaignId == null) {
			throw new IllegalArgumentException("Parameter must be of type 'String'.");
		}

		return getEntityManager().find(Campaign.class, campaignId);
	}

	@Transactional
	public void save(Campaign campaign) {

		try {
			getEntityManager().persist(campaign);
			
		} catch (Exception e) {
		}
	}

	@Transactional
	public void update(String campaignId, String campaignName, Date startDate, Date endDate, List<Theme> themeList)
			throws Exception {

		if (themeList == null) {
			themeList = new ArrayList<>();
		}

		try {
			Campaign campaign = getCampaignById(campaignId);

			if (campaign == null) {
				throw new Exception("The campaign you're trying to update does not exist.");
			}
			campaign.setCampaignName(campaignName);
			campaign.setStartDate(startDate);
			campaign.setEndDate(endDate);
			campaign.setThemeList(themeList);
			getEntityManager().persist(campaign);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	

    @Transactional
    public void deleteCampaign(String campaignId) throws Exception{
        try {
        	Campaign campaign = getCampaignById(campaignId);

			if (campaign == null) {
				throw new Exception("The campaign you're trying to update does not exist.");
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
