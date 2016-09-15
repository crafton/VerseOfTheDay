package controllers;


import com.google.inject.Inject;
import models.AdminSettings;
import ninja.Result;
import ninja.Results;
import repositories.AdminSettingsRepository;

public class AdminSettingsController {

    public final AdminSettingsRepository adminSettingsRepository;

    @Inject
    public AdminSettingsController(AdminSettingsRepository adminSettingsRepository){
        this.adminSettingsRepository = adminSettingsRepository;
    }

    public Result adminSettings(){

        AdminSettings adminSettings = adminSettingsRepository.findSettings();

        return Results.html().render("adminSettings", adminSettings);
    }

    public Result saveSettings(){

        return Results.html();
    }
}
