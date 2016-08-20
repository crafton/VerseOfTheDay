package models;


import org.eclipse.jetty.util.annotation.Name;

import javax.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "Used.findByCampaign", query = "SELECT x FROM VotdUsed x WHERE x.campaignId = :campaignid"),
    @NamedQuery(name = "Used.findByVotd", query = "SELECT x FROM VotdUsed x WHERE x.votdId = :votdid"),
    @NamedQuery(name = "Used.flushVotds", query = "DELETE FROM VotdUsed x WHERE x.campaignId = :campaignid")
})
public class VotdUsed {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long campaignId;
    private Long votdId;

    public VotdUsed(){}

    public Long getId() {
        return id;
    }
    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getVotdId() {
        return votdId;
    }

    public void setVotd(Long votdId) {
        this.votdId = votdId;
    }
}
