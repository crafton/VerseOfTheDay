package controllers;


import com.google.inject.Inject;
import filters.AdminFilter;
import filters.LoginFilter;
import models.AdminSettings;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AdminSettingsRepository;
import repositories.VotdRepository;
import services.UserService;
import utilities.Config;

import java.util.List;

@FilterWith({LoginFilter.class, AdminFilter.class})
public class AdminSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsController.class);
    private final AdminSettingsRepository adminSettingsRepository;
    private final VotdRepository votdRepository;
    private final UserService userService;
    private final Config config;

    @Inject
    public AdminSettingsController(AdminSettingsRepository adminSettingsRepository, VotdRepository votdRepository,
                                   UserService userService, Config config){
        this.adminSettingsRepository = adminSettingsRepository;
        this.votdRepository = votdRepository;
        this.userService = userService;
        this.config = config;
    }

    public Result adminSettings(Context context){

        AdminSettings adminSettings = adminSettingsRepository.findSettings();
        List<String> versions = votdRepository.findAllVersions();

        String role = userService.getHighestRole(context.getSession().get(config.IDTOKEN_NAME));

        return Results.html().render("adminSettings", adminSettings)
                .render("versions", versions)
                .render("loggedIn", true)
                .render("role", role);
    }

    public Result saveSettings(AdminSettings adminSettings, FlashScope flashScope){

        if(adminSettings == null){
            flashScope.error("Problem with savings the settings. Contact administrator.");
            logger.warn("Client tried to save a null settings object.");
            return Results.redirect("/admin/settings");
        }

        if(adminSettings.getId() == null || adminSettings.getId() != 1L){
            adminSettings.setId(1L);
        }

        adminSettingsRepository.save(adminSettings);
        logger.info("Successfully saved admin settings.");
        flashScope.success("Settings saved.");

        return Results.redirect("/admin/settings");
    }
}
