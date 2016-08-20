package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NamedQueries({
        @NamedQuery(name = "Votd.findVersesInChapter", query = "SELECT V.verses FROM Votd V WHERE V.verses LIKE :bookchapter"),
        @NamedQuery(name = "Votd.findExistingVerse", query = "SELECT V.verses FROM Votd V WHERE V.verses = :verse"),
        @NamedQuery(name = "Votd.findAll", query = "SELECT x FROM Votd x"),
        @NamedQuery(name = "Votd.findAllVerseIds", query = "SELECT x.id FROM Votd x"),
        @NamedQuery(name = "Votd.wildFind", query = "SELECT x FROM Votd x WHERE x.verses LIKE :verse " +
                "OR x.modifiedBy LIKE :modifiedby OR x.createdBy LIKE :createdby OR x.isApproved = :isapproved"),
        @NamedQuery(name = "Votd.wildFindCount", query = "SELECT COUNT(x) as total FROM Votd x WHERE x.verses LIKE :verse " +
                "OR x.modifiedBy LIKE :modifiedby OR x.createdBy LIKE :createdby OR x.isApproved = :isapproved"),
        @NamedQuery(name = "Votd.count", query = "SELECT COUNT(x) as total FROM Votd x"),
        @NamedQuery(name = "Votd.findUnusedVerses", query = "SELECT x FROM Votd x, VotdUsed y WHERE y.campaignId = :campaignid AND x.id <> y.votdId")
})
public class Votd {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String verses;

    private boolean isApproved = false;

    private String modifiedBy;

    private String createdBy;

    private Timestamp dateCreated;

    private Timestamp dateModified;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Theme> themes;

    public Votd() {
    }

    public Long getId() {
        return this.id;
    }

    public String getVerses() {
        return this.verses;
    }

    public void setVerses(String verses) {
        this.verses = verses;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public String getThemesAsString(){
        return themes.stream()
                .map(Theme::getThemeName)
                .collect(Collectors.joining(", "));
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isApproved() {
        return this.isApproved;
    }

    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Timestamp getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Timestamp dateModified) {
        this.dateModified = dateModified;
    }
}
