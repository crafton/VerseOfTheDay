package services;


import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class VotdDispatchService {

    private final UserRepository userRepository;
    private final VotdRepository votdRepository;
    private final CampaignRepository campaignRepository;
    private final VotdUsedRepository votdUsedRepository;
    private final VotdService votdService;

    @Inject
    public VotdDispatchService(UserRepository userRepository, VotdRepository votdRepository, CampaignRepository campaignRepository, VotdUsedRepository votdUsedRepository, VotdService votdService) {
        this.userRepository = userRepository;
        this.votdRepository = votdRepository;
        this.campaignRepository = campaignRepository;
        this.votdUsedRepository = votdUsedRepository;
        this.votdService = votdService;
    }

    public JsonObject getUsers(Integer start, Integer length, Long campaignId) {

        return userRepository.findSubscribedUsers(start, length, campaignId);
    }

    public Votd getVerseToSend(Campaign campaign) {
        List<Long> votdList = getPotentialVotdList(campaign.getThemeList());

        List<Long> usedVotdList = votdUsedRepository.findVotdUsedByCampaign(campaign);

        //no applicable verses found
        if (votdList.isEmpty() && usedVotdList.isEmpty()) {
            return null;
        }

        votdList.removeAll(usedVotdList);

        Votd votdToSend;

        if (!votdList.isEmpty()) {
            votdToSend = votdRepository.findVerseById(votdList.get(0));
        } else {
            //All votds have been used, so flush the used table and start again
            votdUsedRepository.flushVotds(campaign);
            List<Long> potentialList = getPotentialVotdList(campaign.getThemeList());
            votdToSend = votdRepository.findVerseById(potentialList.get(0));
        }

        //Save votd used

        VotdUsed votdUsed = new VotdUsed();
        votdUsed.setCampaignId(campaign.getCampaignId());
        votdUsed.setVotd(votdToSend.getId());
        votdUsedRepository.save(votdUsed);

        return votdToSend;
    }

    public String getVerseText(Votd verseToSend) throws JsonSyntaxException {
        return votdService.restGetVerses(verseToSend.getVerses());
    }

    private List<Long> getPotentialVotdList(List<Theme> themes) {

        List<Long> potentialVotds = new ArrayList<>();
        if (themes != null && !themes.isEmpty()) {
            for (Theme theme : themes) {
                List<Votd> votdList = theme.getVotds();

                List<Long> votdIdList = votdList.stream()
                        .filter(Votd::isApproved)
                        .map(Votd::getId)
                        .collect(Collectors.toList());

                potentialVotds.addAll(votdIdList);
            }
        } else {
            potentialVotds = votdRepository.findAllApprovedVerseIds();
        }

        Collections.shuffle(potentialVotds);

        return potentialVotds;
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

}
