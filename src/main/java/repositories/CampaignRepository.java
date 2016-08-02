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

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    @Transactional
    public List<Campaign> findAll() {
        Query q = getEntityManager().createNamedQuery("Campaign.findAll");
        return q.getResultList();
    }

    @Transactional
    public Campaign findCampaignById(Long campaignId) throws IllegalArgumentException {
        return getEntityManager().find(Campaign.class, campaignId);
    }

    @Transactional
    public void save(Campaign campaign) throws CampaignException {
        getEntityManager().persist(campaign);
    }

    @Transactional
    public void update(Campaign campaign) {
        getEntityManager().persist(campaign);
    }

    @Transactional
    public void deleteCampaign(Campaign campaign) {
        getEntityManager().remove(campaign);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
