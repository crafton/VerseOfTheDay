package models;

import com.google.inject.Inject;
import ninja.jpa.UnitOfWork;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by Crafton Williams on 23/04/2016.
 */

@Entity
@NamedQueries({
        @NamedQuery(name = "Votd.findVersesInChapter", query = "SELECT verses FROM Votd WHERE verses LIKE :bookchapter"),
        @NamedQuery(name = "Votd.findExistingVerse", query = "SELECT verses FROM Votd WHERE verses = :verse")
})
public class Votd {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String verses;

    @ElementCollection
    private List<String> themes;

    public Votd() {
    }

    public String getVerses() {
        return verses;
    }

    public void setVerses(String verses) {
        this.verses = verses;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

}
