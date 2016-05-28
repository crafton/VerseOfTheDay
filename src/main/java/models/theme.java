package models;

import javax.persistence.*;

/**
 * Created by Crafton Williams on 28/05/2016.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Theme.findAll", query = "SELECT x FROM Theme x ")
})
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String themeName;

    public Theme(){}

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public Long getId(){
        return this.id;
    }

}
