package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Crafton Williams on 23/04/2016.
 */

@Entity
@NamedQueries({
        @NamedQuery(name = "Votd.findVersesInChapter", query = "SELECT verses FROM Votd WHERE verses LIKE :bookchapter"),
        @NamedQuery(name = "Votd.findExistingVerse", query = "SELECT verses FROM Votd WHERE verses = :verse"),
        @NamedQuery(name = "Votd.findAll", query = "SELECT x FROM Votd x")
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
