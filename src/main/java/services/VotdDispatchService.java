package services;


import com.google.gson.JsonObject;
import com.google.inject.Inject;
import models.Campaign;
import models.Theme;
import models.Votd;
import models.VotdUsed;
import repositories.CampaignRepository;
import repositories.UserRepository;
import repositories.VotdRepository;
import repositories.VotdUsedRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VotdDispatchService {

    private final UserRepository userRepository;
    private final VotdRepository votdRepository;
    private final CampaignRepository campaignRepository;
    private final VotdUsedRepository votdUsedRepository;

    @Inject
    public VotdDispatchService(UserRepository userRepository, VotdRepository votdRepository, CampaignRepository campaignRepository, VotdUsedRepository votdUsedRepository) {
        this.userRepository = userRepository;
        this.votdRepository = votdRepository;
        this.campaignRepository = campaignRepository;
        this.votdUsedRepository = votdUsedRepository;
    }

    public JsonObject getUsers(Integer start, Integer length, Long campaignId) {

        return userRepository.findSubscribedUsers(start, length, campaignId);
    }

    public Votd getVerseToSend(Campaign campaign) {
        List<Long> votdList = getPotentialVotdList(campaign.getThemeList());

        List<Long> usedVotdList = votdUsedRepository.findVotdUsedByCampaign(campaign);

        votdList.removeAll(usedVotdList);

        Votd votdToSend;

        if(!votdList.isEmpty()){
            votdToSend = votdRepository.findVerseById(votdList.get(0));
        }else{
            //All votds have been used, so flush the used table and start again
            votdUsedRepository.flushVotds(campaign);
            votdToSend = votdRepository.findVerseById(getPotentialVotdList(campaign.getThemeList()).get(0));
        }

        //Save votd used

        VotdUsed votdUsed = new VotdUsed();
        votdUsed.setCampaignId(campaign.getCampaignId());
        votdUsed.setVotd(votdToSend.getId());
        votdUsedRepository.save(votdUsed);

        return votdToSend;
    }

    private List<Long> getPotentialVotdList(List<Theme> themes) {

        List<Long> potentialVotds = new ArrayList<>();
        if (themes != null && !themes.isEmpty()) {
            for (Theme theme : themes) {
                List<Votd> votdList = theme.getVotds();

                Long[] idArray = (Long[]) votdList.stream().map(votd -> votd.getId()).toArray();
                List<Long> votdIdList = Arrays.asList(idArray);

                potentialVotds.addAll(votdIdList);
            }
        } else {
            potentialVotds = votdRepository.findAllVerseIds();
        }

        Collections.shuffle(potentialVotds);

        return potentialVotds;
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

}
