package models;


import java.sql.Timestamp;

public class VotdDispatch {

    private Campaign campaign;
    private Integer totalNumberOfUsers;
    private Votd votdToBeDispatched;
    private boolean shouldRandomize;
    private Timestamp timeStarted;
    private Timestamp timeFinished;

    public VotdDispatch() {
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Integer getTotalNumberOfUsers() {
        return totalNumberOfUsers;
    }

    public void setTotalNumberOfUsers(Integer totalNumberOfUsers) {
        this.totalNumberOfUsers = totalNumberOfUsers;
    }

    public Votd getVotdToBeDispatched() {
        return votdToBeDispatched;
    }

    public void setVotdToBeDispatched(Votd votdToBeDispatched) {
        this.votdToBeDispatched = votdToBeDispatched;
    }

    public boolean isShouldRandomize() {
        return shouldRandomize;
    }

    public void setShouldRandomize(boolean shouldRandomize) {
        this.shouldRandomize = shouldRandomize;
    }

    public Timestamp getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted() {
        this.timeStarted = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished() {
        this.timeFinished = new Timestamp(System.currentTimeMillis());
    }

    public Integer getVotdDispatchUserPages(Integer length) {
        Double lengthAsDouble = length.doubleValue();
        Double pages = this.getTotalNumberOfUsers() / lengthAsDouble;

        return (int) Math.ceil(pages);
    }
}
