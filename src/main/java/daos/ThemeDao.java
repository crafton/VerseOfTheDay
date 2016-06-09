package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;

public class ThemeDao {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    @Inject
    Logger logger;

    public ThemeDao() {
    }

    @Transactional
    public List<Theme> findAll() {
        Query q = getEntityManager().createNamedQuery("Theme.findAll");
        return (List<Theme>) q.getResultList();
    }

    @Transactional
    public String findByName(String themeName) throws NoResultException {
        Query q = getEntityManager().createNamedQuery("Theme.findByName");
        q.setParameter("name", themeName);

        return (String) q.getSingleResult();
    }

    @Transactional
    public Theme findById(Long id) throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

        return getEntityManager().find(Theme.class, id);
    }

    @Transactional
    public void save(Theme theme) throws IllegalArgumentException, EntityAlreadyExistsException {

        if (theme == null || theme.getThemeName().isEmpty()) {
            throw new IllegalArgumentException("Parameter must be of type 'Theme'.");
        }

        try {
            findByName(theme.getThemeName());
            throw new EntityAlreadyExistsException("Cannot save a theme that already exists.");
        }catch(NoResultException e){
            logger.debug("Theme does not exist, proceeding to save...");
        }

        theme.setDateCreated(new Timestamp(System.currentTimeMillis()));
        getEntityManager().persist(theme);
    }

    @Transactional
    public void delete(Long themeId) throws IllegalArgumentException, EntityDoesNotExistException, EntityBeingUsedException {
        if (themeId == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

        Theme theme = findById(themeId);

        if (theme == null) {
            throw new EntityDoesNotExistException("Theme not found with the supplied themeId.");
        }

        if (theme.getVotds().size() > 0) {
            throw new EntityBeingUsedException("Cannot delete this theme, it is already being used.");
        }

        getEntityManager().remove(theme);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
