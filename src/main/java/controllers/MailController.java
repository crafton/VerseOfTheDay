package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;

public class MailController {

    @Inject
    Provider<Mail> mailProvider;

    @Inject
    Postoffice postoffice;

    public void sendMail(){

    }

}
