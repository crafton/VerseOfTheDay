package repositories;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityAlreadyExistsException;
import exceptions.EntityBeingUsedException;
import exceptions.EntityDoesNotExistException;
import models.Theme;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.List;

public class ThemeRepository {

    private final Provider<EntityManager> entityManagerProvider;

    @Inject
    public ThemeRepository(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
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
    public Theme findById(Long id) {
        return getEntityManager().find(Theme.class, id);
    }

    @Transactional
    public void save(Theme theme) throws EntityAlreadyExistsException {

        try {
            findByName(theme.getThemeName());
            throw new EntityAlreadyExistsException("Cannot save a theme that already exists.");
        }catch(NoResultException e){
            //Theme does not exist, go ahead and save
            theme.setDateCreated(new Timestamp(System.currentTimeMillis()));
            getEntityManager().persist(theme);
        }
    }

    @Transactional
    public void delete(Long themeId) throws EntityDoesNotExistException, EntityBeingUsedException {
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
