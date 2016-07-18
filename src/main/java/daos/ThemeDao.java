package daos;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.persist.Transactional;

import models.Theme;

public class ThemeDao {
	@Inject
	private Provider<EntityManager> entityManagerProvider;

	@Transactional
	public List<Theme> getThemeList() {
		Query q = getEntityManager().createNamedQuery("Theme.findAll");
		return q.getResultList();
	}
	
	@Transactional
	public Theme getThemeById(Long themeId) throws IllegalArgumentException {

		if (themeId == null) {
			throw new IllegalArgumentException("Parameter must be of type 'Long'.");
		}

		return getEntityManager().find(Theme.class, themeId);
	}


	private EntityManager getEntityManager() {
		return entityManagerProvider.get();
	}
}
