package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;

/**
 * Created by Crafton Williams on 12/06/2016.
 */
@Singleton
public class MailController {

    @Inject
    Provider<Mail> mailProvider;

    @Inject
    Postoffice postoffice;
}
