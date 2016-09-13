package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.StringUtils;

@Entity
@NamedQueries({
        @NamedQuery(name = "Theme.findAll", query = "SELECT x FROM Theme x "),
        @NamedQuery(name = "Theme.findByName", query = "SELECT T.themeName FROM Theme T WHERE T.themeName = :name")
})
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String themeName;

    private String createdBy;

    private Timestamp dateCreated;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "themes")
    private List<Votd> votds;

    public Theme() {
    }

    public Long getId() {
        return this.id;
    }

    public String getThemeName() {
        return this.themeName;
    }

    public void setThemeName(String themeName) {

        this.themeName = StringUtils.capitalize(themeName);
    }

    public List<Votd> getVotds() {
        return this.votds;
    }

    public void setVotds(List<Votd> votds) {
        this.votds = votds;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return this.getThemeName();
    }
}
