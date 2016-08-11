package repositories;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import exceptions.EntityDoesNotExistException;
import models.Theme;
import models.Votd;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import utilities.Config;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VotdRepository {

    private final Config config;
    private final Provider<EntityManager> entityManagerProvider;

    @Inject
    public VotdRepository(Config config, Provider<EntityManager> entityManagerProvider) {
        this.config = config;
        this.entityManagerProvider = entityManagerProvider;
    }

    @Transactional
    public List<Votd> findAllVerses() {
        Query q = getEntityManager().createNamedQuery("Votd.findAll");
        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public Votd findVerseById(Long votdId){
        return getEntityManager().find(Votd.class, votdId);
    }

    @Transactional
    public String findVerseByVerseName(String verseName) throws NoResultException {
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
    public List<Votd> search(String param, Integer start, Integer length) {

        Query q = this.buildSearchQuery(param, "Votd.wildFind");

        q.setFirstResult(start);
        q.setMaxResults(length);

        return (List<Votd>) q.getResultList();
    }

    @Transactional
    public Long countRecordsWithFilter(String param) throws NoResultException {
        Query q = this.buildSearchQuery(param, "Votd.wildFindCount");

        return (Long) q.getSingleResult();
    }

    @Transactional
    public Long countTotalRecords() throws NoResultException {
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

        Votd votd = findVerseById(votdId);

        if (votd == null) {
            throw new EntityDoesNotExistException("The VOTD you're trying to update does not exist.");
        }

        votd.setThemes(themes);
        votd.setApproved(votdStatus);
        votd.setDateModified(new Timestamp(System.currentTimeMillis()));
        getEntityManager().persist(votd);
    }

    @Transactional
    public void approve(Long votdId) throws IllegalArgumentException, EntityDoesNotExistException {

        Votd votd = findVerseById(votdId);

        if (votd == null) {
            throw new EntityDoesNotExistException("You cannot approve a VOTD that does not exist.");
        }
        votd.setApproved(true);
        getEntityManager().persist(votd);
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
        Votd votd = findVerseById(votdId);
        if (votd == null) {
            throw new EntityDoesNotExistException("Cannot delete a VOTD that does not exist.");
        }
        getEntityManager().remove(votd);
    }

    /**
     * Initiate a web service client to retrieve the verses passed in.
     *
     * @param verseRange
     * @return Verse text.
     */
    public JsonObject findVersesByRange(String verseRange) throws JsonSyntaxException {
        HttpAuthenticationFeature authenticationFeature = HttpAuthenticationFeature.basic(config.getBibleSearchKey(), "");
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.register(authenticationFeature)
                .target("https://bibles.org/v2/passages.js");

        String verseTextJson = webTarget
                .queryParam("q[]", verseRange)
                .queryParam("version", "eng-ESV")
                .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        JsonObject verseJsonObject;

        JsonParser parser = new JsonParser();
        return parser.parse(verseTextJson).getAsJsonObject();
    }

    /**
     * Generic method to build a search query for a verse.
     *
     * @param param
     * @param queryName
     * @return
     */
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