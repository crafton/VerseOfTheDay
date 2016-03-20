package controllers;

import ninja.Result;
import ninja.Results;
import com.google.inject.Singleton;

/**
 * Created by Crafton Williams on 19/03/2016.
 */

@Singleton
public class VotdController {

    public Result createVotd() {
        return Results.html();
    }

}
