package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import utilities.Config;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crafton Williams on 1/06/2016.
 */
public class VotdDao {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    @Inject
    Config config;

    public VotdDao() {
    }

    @Transactional
    public List<Votd> findAll() {
        Query q = getEntityManager().createNamedQuery("Votd.findAll");
        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public Votd findById(Long votdId) throws IllegalArgumentException {

        if (votdId == null) {
            throw new IllegalArgumentException("Parameter must be of type 'Long'.");
        }

        return getEntityManager().find(Votd.class, votdId);
    }

    @Transactional
    public String findByVerse(String verseName) throws NoResultException {
        Query q = getEntityManager().createNamedQuery("Votd.findExistingVerse");
        q.setParameter("verse", verseName);

        return (String) q.getSingleResult();
    }

    @Transactional
    public List<String> findVersesInChapter(String bookAndChapter) {

        Query q = getEntityManager().createNamedQuery("Votd.findVersesInChapter");
        q.setParameter("bookchapter", bookAndChapter + "%%");

        return (List<String>) q.getResultList();
    }

    @Transactional
    public List<Votd> wildFind(String param, Integer start, Integer length) {
        Query q = getEntityManager().createNamedQuery("Votd.wildFind");
        q.setParameter("verse", param + "%");
        q.setParameter("modifiedby", param + "%");
        q.setParameter("createdby", param + "%");

        if (param.contentEquals(config.APPROVED)) {
            q.setParameter("isapproved", true);
        } else if (param.contentEquals(config.PENDING)) {
            q.setParameter("isapproved", false);
        } else{
            q.setParameter("isapproved", null);
        }
        q.setFirstResult(start);
        q.setMaxResults(length);

        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public List<Votd> findAllWithLimit(Integer start, Integer length) {
        Query q = getEntityManager().createNamedQuery("Votd.findAll");
        q.setFirstResult(start);
        q.setMaxResults(length);

        return (List<Votd>) q.getResultList();
    }


    @Transactional
    public void update(Long votdId, List<Theme> themes, boolean votdStatus)
            throws IllegalArgumentException, EntityDoesNotExistException {

        if (themes == null) {
            themes = new ArrayList<>();
        }

        try {
            Votd votd = findById(votdId);

            if (votd == null) {
                throw new EntityDoesNotExistException("The VOTD you're trying to update does not exist.");
            }

            votd.setThemes(themes);
            votd.setApproved(votdStatus);
            votd.setDateModified(new Timestamp(System.currentTimeMillis()));
            getEntityManager().persist(votd);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Transactional
    public void approve(Long votdId) throws IllegalArgumentException, EntityDoesNotExistException {

        try {
            Votd votd = findById(votdId);

            if (votd == null) {
                throw new EntityDoesNotExistException("You cannot approve a VOTD that does not exist.");
            }
            votd.setApproved(true);
            getEntityManager().persist(votd);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Transactional
    public void save(Votd votd) throws IllegalArgumentException {

        if (votd == null) {
            throw new IllegalArgumentException("VOTD must be a valid entry.");
        }

        votd.setDateCreated(new Timestamp(System.currentTimeMillis()));
        getEntityManager().persist(votd);
    }

    @Transactional
    public void delete(Long votdId) throws IllegalArgumentException, EntityDoesNotExistException {
        try {
            Votd votd = findById(votdId);
            if (votd == null) {
                throw new EntityDoesNotExistException("Cannot delete a VOTD that does not exist.");
            }
            getEntityManager().remove(votd);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}