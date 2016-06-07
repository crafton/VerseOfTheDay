package daos;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import models.Theme;
import models.Votd;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Crafton Williams on 1/06/2016.
 */
public class VotdDao {

    @Inject
    private Provider<EntityManager> entityManagerProvider;

    public VotdDao() {
    }

    @Transactional
    public List<Votd> findAll() {
        Query q = getEntityManager().createNamedQuery("Votd.findAll");
        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public Votd findById(Long votdId) {
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
    public void update(Long votdId, List<Theme> themes, boolean votdStatus) {
        Votd votd = findById(votdId);
        votd.setThemes(themes);
        votd.setApproved(votdStatus);
        votd.setDateModified(new Timestamp(System.currentTimeMillis()));
        getEntityManager().persist(votd);
    }

    @Transactional
    public void approve(Long votdId) {
        Votd votd = findById(votdId);
        votd.setApproved(true);
        getEntityManager().persist(votd);
    }

    @Transactional
    public void save(Votd votd) {
        votd.setDateCreated(new Timestamp(System.currentTimeMillis()));
        getEntityManager().persist(votd);
    }

    @Transactional
    public void delete(Long votdId) {
        Votd votd = findById(votdId);
        getEntityManager().remove(votd);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}