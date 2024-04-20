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


public class TweetController {
    private Jdbi jdbi;

    public TweetController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderHomepage(Context ctx) {
        List<Tweet> tweets = jdbi.withHandle(handle ->
                handle.createQuery("SELECT t.tweet_id, u.username, t.content, t.timestamp, t.location, t.media, t.in_reply_to_tweet_id, COUNT(l.like_id) as like_count " +
                                "FROM \"xcloneSchema\".\"tweet\" t " +
                                "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
                                "LEFT JOIN \"xcloneSchema\".\"like\" l ON t.tweet_id = l.tweet_id " +
                                "GROUP BY t.tweet_id, u.username " +
                                "ORDER BY t.timestamp DESC")
                        .map((rs, mapCtx) -> new Tweet(
                                rs.getString("tweet_id"),
                                rs.getString("username"),
                                rs.getString("content"),
                                rs.getTimestamp("timestamp"),
                                rs.getString("location"),
                                rs.getString("media"),
                                rs.getString("in_reply_to_tweet_id"),
                                rs.getInt("like_count")
                        ))
                        .list()
        );
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
            Integer userId = handle.createQuery("SELECT user_id FROM \"xcloneSchema\".\"user\" WHERE email = :email")
                    .bind("email", email)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(null);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null; // Stop the transaction
            }

            handle.createUpdate("INSERT INTO \"xcloneSchema\".\"tweet\" (user_id, content, timestamp, location, media, in_reply_to_tweet_id) VALUES (:userId, :content, NOW(), :location, :media, :replyToTweetId)")
                    .bind("userId", userId)
                    .bind("content", content)
                    .bind("location", location)
                    .bind("media", media)
                    .bind("replyToTweetId", replyToTweetId==0 ? null : replyToTweetId)
                    .execute();

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
            Integer userId = handle.createQuery("SELECT user_id FROM \"xcloneSchema\".\"user\" WHERE email = :email")
                    .bind("email", email)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(null);

            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return null; // Stop the transaction
            }

            boolean exists = handle.createQuery("SELECT COUNT(*) FROM \"xcloneSchema\".\"like\" WHERE tweet_id = :tweetId AND user_id = :userId")
                    .bind("tweetId", tweetId)
                    .bind("userId", userId)
                    .mapTo(Integer.class)
                    .one() > 0;

            if (exists) {
                handle.createUpdate("DELETE FROM \"xcloneSchema\".\"like\" WHERE tweet_id = :tweetId AND user_id = :userId")
                        .bind("tweetId", tweetId)
                        .bind("userId", userId)
                        .execute();
            } else {
                handle.createUpdate("INSERT INTO \"xcloneSchema\".\"like\" (tweet_id, user_id, timestamp) VALUES (:tweetId, :userId, NOW())")
                        .bind("tweetId", tweetId)
                        .bind("userId", userId)
                        .execute();
            }

            ctx.redirect("/app/homepage");
            return null; // Complete the transaction
        });
    }
}