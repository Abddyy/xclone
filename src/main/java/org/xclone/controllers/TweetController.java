package org.xclone.controllers;

import io.javalin.http.Context;
import org.xclone.Tweet;

import java.util.Objects;
import  java.util.Optional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;
import org.jdbi.v3.core.Jdbi;
import org.xclone.queries.TweetQuries;


public class TweetController {
    private Jdbi jdbi;
    private TweetQuries tweetQuries = new TweetQuries();


    public TweetController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderHomepage(Context ctx) {
        List<Tweet> tweets = tweetQuries.tweetlist(jdbi);
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
            Integer userId = tweetQuries.getUserID(handle, email);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null;
            }

         tweetQuries.getNewTweet(handle, userId, content, location, media, replyToTweetId);

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
            Integer userId = tweetQuries.getUserID(handle, email);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null; // Stop the transaction
            }

            boolean exists = tweetQuries.isLiked(handle, tweetId, userId);

            if (exists) {
                tweetQuries.removeLike(handle,tweetId,userId);
            } else {
                tweetQuries.addLike(handle,tweetId,userId);
            }

            ctx.redirect("/app/homepage");
            return null; // Complete the transaction
        });
    }
}