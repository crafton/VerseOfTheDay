package services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import exceptions.CampaignException;
import models.Campaign;
import models.Theme;
import repositories.CampaignRepository;

public class CampaignService {

    private final CampaignRepository repository;

    @Inject
    public CampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    /**
     *
     * @return
     */
    public List<Campaign> getCampaignList() {
        return repository.findAll();
    }

    /**
     *
     * @param campaignId
     * @return
     * @throws IllegalArgumentException
     */
    public Campaign getCampaignById(Long campaignId) throws IllegalArgumentException {

        if (campaignId == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

        return repository.findCampaignById(campaignId);
    }

    /**
     *
     * @param campaign
     * @throws CampaignException
     */
    public void save(Campaign campaign) throws CampaignException {
        repository.save(campaign);
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
    public void update(Long campaignId, String campaignName, String campaignDescription, Timestamp startDate,
                       Timestamp endDate, int days,
                       List<Theme> themeList) throws CampaignException {
        repository.update(campaignId, campaignName, campaignDescription, startDate, endDate, days,  themeList);
    }

    /**
     *
     * @param campaignId
     * @throws CampaignException
     * @throws IllegalArgumentException
     */
    public void deleteCampaign(Long campaignId) throws CampaignException, IllegalArgumentException {

        repository.deleteCampaign(campaignId);
    }
}
