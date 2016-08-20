package repositories;


import com.google.inject.Inject;
import models.Campaign;
import models.Votd;
import models.VotdUsed;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class VotdUsedRepository {

    private final Provider<EntityManager> entityManagerProvider;

    @Inject
    public VotdUsedRepository(Provider<EntityManager> entityManagerProvider){
        this.entityManagerProvider = entityManagerProvider;
    }

    public void save(VotdUsed votdUsed){
        getEntityManager().persist(votdUsed);
    }

    public List<VotdUsed> findVotdUsedByCampaign(Campaign campaign){
        Query q = getEntityManager().createNamedQuery("Used.findByCampaign");
        q.setParameter("campaignid", campaign.getCampaignId());

        return (List<VotdUsed>) q.getResultList();
    }

    public List<VotdUsed> findVotdUsedByVotd(Votd votd){
        Query q = getEntityManager().createNamedQuery("Used.findByVotd");
        q.setParameter("votdid", votd.getId());

        return (List<VotdUsed>) q.getResultList();
    }

    public void flushVotds(Campaign campaign){
        Query q = getEntityManager().createNamedQuery("Used.flushVotds");
        q.setParameter("campaignid", campaign.getCampaignId());

        q.executeUpdate();
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
