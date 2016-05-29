package models;

import com.google.inject.Inject;
import ninja.jpa.UnitOfWork;
import org.eclipse.jetty.util.annotation.Name;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

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

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

}
