package models;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Crafton Williams on 28/05/2016.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Theme.findAll", query = "SELECT x FROM Theme x "),
        @NamedQuery(name = "Theme.findByName", query = "SELECT themeName FROM Theme WHERE themeName = :name")
})
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String themeName;

    private String createdBy;

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

    @Override
    public String toString() {
        return this.getThemeName();
    }
}
