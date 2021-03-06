package repositories;


import com.google.inject.persist.Transactional;
import exceptions.CampaignException;
import models.Campaign;
import models.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.Config;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CampaignRepository {

    private static final Logger logger = LoggerFactory.getLogger(CampaignRepository.class);
    private final Provider<EntityManager> entityManagerProvider;
    private final Config config;

    @Inject
    public CampaignRepository(Provider<EntityManager> entityManagerProvider, Config config) {
        this.entityManagerProvider = entityManagerProvider;
        this.config = config;
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
        LocalTime localTime = LocalTime.now();
        String currentTime = localTime.format(DateTimeFormatter.ofPattern(config.TIME_FORMAT));
        q.setParameter("currentTime", currentTime);

        logger.info("query time "+ currentTime);

        return (List<Campaign>) q.getResultList();
    }

    /**
     * @param campaignId
     * @return
     * @throws IllegalArgumentException
     */
    @Transactional
    public Campaign findCampaignById(Long campaignId) throws IllegalArgumentException {

        if (campaignId == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

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
     * @param campaignId
     * @param campaignName
     * @param startDate
     * @param endDate
     * @param themeList
     * @throws CampaignException
     */
    @Transactional
    public void update(Long campaignId, String campaignName, String campaignDescription, Timestamp startDate,
                       Timestamp endDate, int days,
                       List<Theme> themeList, String sendTime) throws CampaignException {

        if (themeList == null) {
            themeList = new ArrayList<>();
            logger.info("No themes present");
        }

        Campaign campaign = findCampaignById(campaignId);

        if (campaign == null) {
            throw new CampaignException("The campaign you're trying to update does not exist.");
        }
        campaign.setCampaignName(campaignName);
        campaign.setCampaignDescription(campaignDescription);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setCampaignDays(days);
        campaign.setThemeList(themeList);
        campaign.setSendTime(sendTime);
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
