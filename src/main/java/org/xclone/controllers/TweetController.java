package org.xclone.controllers;

import io.javalin.http.Context;
import org.xclone.Tweet;

import  java.util.Optional;

import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;
import org.jdbi.v3.core.Jdbi;
import org.xclone.services.TweetServices;


public class TweetController {
    private Jdbi jdbi;
    private TweetServices tweetServices = new TweetServices();


    public TweetController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderHomepage(Context ctx) {
        List<Tweet> tweets = tweetServices.tweetlist(jdbi);
        ctx.render("templates/homepage.peb", model("tweets", tweets));
    }

    public void handlePostCreate(Context ctx){


        String email = ctx.sessionAttribute("email");
        if (email == null) {
            ctx.render("templates/homepage.peb", model("errorMessage", "You must be logged in to post tweets."));
            return;
        }

        String content = ctx.formParam("content");
        String location = ctx.formParam("location");
        String media = ctx.formParam("media");
        // reply to tweet id can be empty string and I need it as int

        int replyToTweetId =Optional.ofNullable(ctx.formParam("replyToTweetId"))
                .filter(val->!val.equals(""))
                .map(val->Integer.parseInt(val))
                .orElse(0);

        jdbi.inTransaction(handle -> {
            Integer userId = tweetServices.getUserID(handle, email);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null;
            }

         tweetServices.getNewTweet(handle, userId, content, location, media, replyToTweetId);

            ctx.redirect("/app/homepage");
            return null; // Complete the transaction
        });
    }


    public void handleLikeAction(Context ctx){
        String email = ctx.sessionAttribute("email");
        if (email == null) {
            ctx.render("templates/homepage.peb", model("errorMessage", "You must be logged in to like tweets."));
            return;
        }

        int tweetId = Integer.parseInt(ctx.formParam("tweetId"));
        jdbi.inTransaction(handle -> {
            Integer userId = tweetServices.getUserID(handle, email);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null; // Stop the transaction
            }

            boolean exists = tweetServices.isLiked(handle, tweetId, userId);

            if (exists) {
                tweetServices.removeLike(handle,tweetId,userId);
            } else {
                tweetServices.addLike(handle,tweetId,userId);
            }

            ctx.redirect("/app/homepage");
            return null; // Complete the transaction
        });
    }
}