package repositories;


import com.google.inject.persist.Transactional;
import exceptions.CampaignException;
import models.Campaign;
import models.Theme;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CampaignRepository {


    private final Provider<EntityManager> entityManagerProvider;

    @Inject
    public CampaignRepository(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    /**
     * @return
     */
    @Transactional
    public List<Campaign> findAll() {
        Query q = getEntityManager().createNamedQuery("Campaign.findAll");
        return q.getResultList();
    }

    @Transactional
    public List<Campaign> findActiveCampaigns() {
        Query q = getEntityManager().createNamedQuery("Campaign.findActive");
        q.setParameter("now", new Timestamp(System.currentTimeMillis()));

        return (List<Campaign>) q.getResultList();
    }

    /**
     * @param campaignId
     * @return
     * @throws IllegalArgumentException
     */
    @Transactional
    public Campaign findCampaignById(Long campaignId) {
        return getEntityManager().find(Campaign.class, campaignId);
    }

    /**
     * @param campaign
     * @throws CampaignException
     */
    @Transactional
    public void save(Campaign campaign) throws CampaignException {
        getEntityManager().persist(campaign);
    }

    /**
     *
     * @param campaignId
     * @param campaignName
     * @param startDate
     * @param endDate
     * @param themeList
     * @throws CampaignException
     */
    @Transactional
    public void update(Long campaignId, String campaignName, String campaignDescription, Timestamp startDate, Timestamp endDate,
                       List<Theme> themeList) throws CampaignException {

        if (themeList == null) {
            themeList = new ArrayList<>();
        }

        Campaign campaign = findCampaignById(campaignId);

        if (campaign == null) {
            throw new CampaignException("The campaign you're trying to update does not exist.");
        }
        campaign.setCampaignName(campaignName);
        campaign.setCampaignDescription(campaignDescription);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setThemeList(themeList);
        getEntityManager().persist(campaign);
    }

    /**
     * @param campaignId
     */
    @Transactional
    public void deleteCampaign(Long campaignId) throws CampaignException {
        Campaign campaign = findCampaignById(campaignId);

        if (campaign == null) {
            throw new CampaignException("The campaign you're trying to update does not exist.");
        }
        getEntityManager().remove(campaign);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
