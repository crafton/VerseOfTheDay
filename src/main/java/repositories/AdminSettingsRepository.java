package repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import models.AdminSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class AdminSettingsRepository {

    private final Provider<EntityManager> entityManagerProvider;
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsRepository.class);

    @Inject
    public AdminSettingsRepository(Provider<EntityManager> entityManagerProvider){
        this.entityManagerProvider = entityManagerProvider;
    }

    @Transactional
    public AdminSettings findSettings(){
        return getEntityManager().find(AdminSettings.class, 1L);
    }

    @Transactional
    public void save(AdminSettings newAdminSettings){

        getEntityManager().merge(newAdminSettings);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
