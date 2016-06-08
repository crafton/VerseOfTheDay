package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.List;

public class ThemeDao {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

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
    public void save(Theme theme) throws IllegalArgumentException {

        if (theme == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Theme'.");
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
