package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by Crafton Williams on 28/05/2016.
 */
@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String themeName;

    public Theme(){}

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
}
