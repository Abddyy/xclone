package org.xclone.controllers;

import io.javalin.http.Context;
import org.jdbi.v3.core.Jdbi;
import org.xclone.Tweet;
import org.xclone.services.ProfileServices;
import org.xclone.services.TweetServices;


import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

public class ProfileController {
    private final Jdbi jdbi;
    private final ProfileServices tweetProfileServices = new ProfileServices();

    public ProfileController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    public void handleProfile(Context ctx){

        String username = ctx.sessionAttribute("username");
        List<Tweet> tweets = jdbi.withHandle(handle -> tweetProfileServices.findTweetsByUsername(jdbi, username));

        ctx.render("templates/profile.peb", model("username", username, "tweets", tweets));
    }

}
