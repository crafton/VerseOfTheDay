package models;


import java.sql.Timestamp;

public class VotdDispatch {

    private Campaign campaign;
    private Integer totalNumberOfUsers;
    private Votd votdToBeDispatched;
    private boolean shouldRandomize;
    private Timestamp timeStarted;
    private Timestamp timeFinished;

}
