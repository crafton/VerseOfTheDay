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

    @Inject
    private CampaignRepository repository;

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
        repository.update(campaign);
    }

    /**
     *
     * @param campaignId
     * @throws CampaignException
     * @throws IllegalArgumentException
     */
    public void deleteCampaign(Long campaignId) throws CampaignException, IllegalArgumentException {
        Campaign campaign = getCampaignById(campaignId);

        if (campaign == null) {
            throw new CampaignException("The campaign you're trying to update does not exist.");
        }

        repository.deleteCampaign(campaign);
    }
}
