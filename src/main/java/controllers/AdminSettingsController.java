package controllers;


import com.google.inject.Inject;
import models.AdminSettings;
import ninja.Result;
import ninja.Results;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AdminSettingsRepository;
import repositories.VotdRepository;

import java.util.List;

public class AdminSettingsController {


    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsController.class);
    private final AdminSettingsRepository adminSettingsRepository;
    private final VotdRepository votdRepository;

    @Inject
    public AdminSettingsController(AdminSettingsRepository adminSettingsRepository, VotdRepository votdRepository){
        this.adminSettingsRepository = adminSettingsRepository;
        this.votdRepository = votdRepository;
    }

    public Result adminSettings(){

        AdminSettings adminSettings = adminSettingsRepository.findSettings();
        List<String> versions = votdRepository.findAllVersions();

        return Results.html().render("adminSettings", adminSettings)
                .render("versions", versions);
    }

    public Result saveSettings(AdminSettings adminSettings, FlashScope flashScope){

        if(adminSettings == null){
            flashScope.error("Problem with savings the settings. Contact administrator.");
            logger.warn("Client tried to save a null settings object.");
            return Results.redirect("/admin/settings");
        }

        adminSettingsRepository.save(adminSettings);
        logger.info("Successfully saved admin settings.");
        flashScope.success("Settings saved.");

        return Results.redirect("/admin/settings");
    }
}
