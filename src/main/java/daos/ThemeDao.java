package daos;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.persist.Transactional;

import models.Theme;

public class ThemeDao {
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Transactional
	public List<Theme> getThemeList() {
		TypedQuery<Theme> q = getEntityManager().createQuery("from Theme", Theme.class);
		return q.getResultList();
	}
	
	@Transactional
	public Theme getThemeById(String themeId) throws IllegalArgumentException {

		if (themeId == null) {
			throw new IllegalArgumentException("Parameter must be of type 'String'.");
		}

		return getEntityManager().find(Theme.class, themeId);
	}


	private EntityManager getEntityManager() {
		return entityManagerProvider.get();
	}
}
