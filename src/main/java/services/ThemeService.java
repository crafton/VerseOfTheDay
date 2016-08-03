package services;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import repositories.ThemeRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.List;

public class ThemeService {

    private final ThemeRepository themeRepository;

    @Inject
    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public String findThemeByName(String themeName) throws NoResultException {
        return themeRepository.findByName(themeName);
    }

    public Theme findThemeById(Long id) throws IllegalArgumentException {

        if (id == null) {
            System.out.println("is it you?");
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

        return themeRepository.findById(id);
    }

    public void saveTheme(Theme theme) throws IllegalArgumentException, EntityAlreadyExistsException {

        if (theme == null || theme.getThemeName().isEmpty()) {
            throw new IllegalArgumentException("Parameter must be of type 'Theme'.");
        }

        themeRepository.save(theme);
    }

    public void deleteTheme(Long themeId) throws IllegalArgumentException, EntityDoesNotExistException, EntityBeingUsedException {

        themeRepository.delete(themeId);
    }

}
