package services;


import com.google.gson.JsonObject;
import com.google.inject.Inject;
import models.Campaign;
import models.Theme;
import models.Votd;
import repositories.UserRepository;

import java.sql.Timestamp;
import java.util.List;

public class VotdDispatchService {

    private final UserRepository userRepository;

    @Inject
    public VotdDispatchService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public JsonObject getUsers(Integer start, Integer page){

        return null;
    }

    public Votd getVerseToSend(){
        return null;
    }

    public List<Votd> getPotentialVotdList(List<Theme> themes, boolean shouldRandomize){
        return null;
    }

    public List<Campaign> getCampaigns(Timestamp startTime, Timestamp endTime){
        return null;
    }

    public Integer getTotalNumberOfUsers(){
        return null;
    }

}
