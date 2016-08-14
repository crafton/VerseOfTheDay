package models;

import com.google.inject.Singleton;
import ninja.scheduler.Schedule;

import java.util.concurrent.TimeUnit;

@Singleton
public class VotdScheduler {

    @Schedule(delay = 60, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void checkAvailableCampaigns(){
        System.out.println("holo");
    }
}
