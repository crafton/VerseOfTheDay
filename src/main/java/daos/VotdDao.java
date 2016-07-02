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

        Query q = this.buildSearchQuery(param, "Votd.wildFind");

        q.setFirstResult(start);
        q.setMaxResults(length);

        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public Long countFilteredRecords(String param) throws NoResultException {
        Query q = this.buildSearchQuery(param, "Votd.wildFindCount");

        return (Long) q.getSingleResult();
    }

    @Transactional
    public Long getTotalRecords() throws NoResultException {
        Query q = getEntityManager().createNamedQuery("Votd.count");

        return (Long) q.getSingleResult();
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

    /**
     * Reformat query results to what DataTables expects. Ensure fields are added to the
     * array in the same order as the columns headings are displayed.
     *
     * @param votds
     * @return
     */
    public List<String[]> generateDataTableResults(List<Votd> votds){

        String[] votdFields = new String[0];
        List<String[]> votdData = new ArrayList<>();

        for (Votd votd : votds) {
            String votdApproved = "";
            String shouldApproveVotd = "";
            if (votd.isApproved()) {
                votdApproved = config.APPROVED;
            } else {
                shouldApproveVotd = "<a class=\"fa fa-thumbs-up\" href=\"/votd/approve/" + votd.getId() + "\" aria-hidden=\"true\"></a>";
                votdApproved = config.PENDING;
            }

            votdFields = new String[]{votd.getVerses(), votd.getThemesAsString(), votdApproved,
                    shouldApproveVotd, votd.getCreatedBy(), votd.getModifiedBy(),
                    "<a class=\"fa fa-trash\" data-placement=\"top\" data-toggle=\"confirmation\" aria-hidden=\"true\" href=\"/votd/delete/" + votd.getId() + "\"></a>",
                    "<a class=\"fa fa-pencil\" aria-hidden=\"true\" href=\"/votd/update/" + votd.getId() + "\"></a>"};

            votdData.add(votdFields);
        }

        return votdData;
    }

    private Query buildSearchQuery(String param, String queryName) {
        Query q = getEntityManager().createNamedQuery(queryName);
        q.setParameter("verse", param + "%");
        q.setParameter("modifiedby", param + "%");
        q.setParameter("createdby", param + "%");

        if (param.contentEquals(config.APPROVED)) {
            q.setParameter("isapproved", true);
        } else if (param.contentEquals(config.PENDING)) {
            q.setParameter("isapproved", false);
        } else {
            q.setParameter("isapproved", null);
        }

        return q;
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}