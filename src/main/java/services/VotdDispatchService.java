package services;


import com.google.gson.JsonObject;
import models.Campaign;
import models.Theme;
import models.Votd;

import java.sql.Timestamp;
import java.util.List;

public class VotdDispatchService {

    public JsonObject getUsers(Integer start, Integer length, Integer page){
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
