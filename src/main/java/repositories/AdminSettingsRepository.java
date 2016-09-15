package repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import models.AdminSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class AdminSettingsRepository {

    private final Provider<EntityManager> entityManagerProvider;
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsRepository.class);

    @Inject
    public AdminSettingsRepository(Provider<EntityManager> entityManagerProvider){
        this.entityManagerProvider = entityManagerProvider;
    }

    public AdminSettings findSettings(){
        return getEntityManager().find(AdminSettings.class, 1L);
    }

    public void save(AdminSettings newAdminSettings){
        if(newAdminSettings.getId() == null || newAdminSettings.getId() != 1){
            throw new IllegalArgumentException("AdminSettings ID not set");
        }

        getEntityManager().persist(newAdminSettings);
    }

    private EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
