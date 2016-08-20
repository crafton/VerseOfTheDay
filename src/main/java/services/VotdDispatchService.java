package services;


import com.google.gson.JsonObject;
import com.google.inject.Inject;
import models.Campaign;
import models.Theme;
import models.Votd;
import repositories.CampaignRepository;
import repositories.UserRepository;
import repositories.VotdRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VotdDispatchService {

    private final UserRepository userRepository;
    private final VotdRepository votdRepository;
    private final CampaignRepository campaignRepository;

    @Inject
    public VotdDispatchService(UserRepository userRepository, VotdRepository votdRepository, CampaignRepository campaignRepository) {
        this.userRepository = userRepository;
        this.votdRepository = votdRepository;
        this.campaignRepository = campaignRepository;
    }

    public JsonObject getUsers(Integer start, Long campaignId) {

        return userRepository.findSubscribedUsers(start, campaignId);
    }

    public Votd getVerseToSend() {
        return null;
    }

    public List<Votd> getPotentialVotdList(List<Theme> themes, boolean shouldRandomize) {

        List<Votd> potentialVotds = new ArrayList<>();
        if (themes != null && !themes.isEmpty()) {
            for (Theme theme : themes) {
                potentialVotds.addAll(theme.getVotds());
            }
        } else {
            potentialVotds = votdRepository.findAllVerses();
        }

        if (shouldRandomize) {
            Collections.shuffle(potentialVotds);
        }

        return potentialVotds;
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

}
