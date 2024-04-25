package org.xclone.controllers;

import io.javalin.http.Context;
import org.xclone.Tweet;
import org.xclone.services.AuthentcationServices;
import org.xclone.services.TweetServices;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

import static io.javalin.rendering.template.TemplateUtil.model;

public class TweetController {
    private final Jdbi jdbi;
    private final TweetServices tweetServices = new TweetServices();

    public TweetController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderHomepage(Context ctx) {
        List<Tweet> tweets = tweetServices.tweetlist(jdbi);
        String username = ctx.sessionAttribute("username");
        ctx.render("templates/homepage.peb", model("tweets", tweets, "username", username));
    }

    public void handlePostCreate(Context ctx) {
        String email = ctx.sessionAttribute("email");
        if (email == null) {
            ctx.render("templates/homepage.peb", model("errorMessage", "You must be logged in to post tweets."));
            return;
        }

        String content = ctx.formParam("content");
        String location = ctx.formParam("location");
        String media = ctx.formParam("media");
        int replyToTweetId = Optional.ofNullable(ctx.formParam("replyToTweetId"))
                .filter(val -> !val.isEmpty())
                .map(Integer::parseInt)
                .orElse(0);

        jdbi.useTransaction(handle -> {
            Integer userId = tweetServices.getUserID(handle, email);
            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return;
            }
            tweetServices.getNewTweet(handle, userId, content, location, media, replyToTweetId);
            ctx.redirect("/app/homepage");
        });
    }

    public void handleLikeAction(Context ctx) {
        String email = ctx.sessionAttribute("email");
        if (email == null) {
            ctx.render("templates/homepage.peb", model("errorMessage", "You must be logged in to like tweets."));
            return;
        }

        int tweetId = Integer.parseInt(ctx.formParam("tweetId"));
        jdbi.useTransaction(handle -> {
            Integer userId = tweetServices.getUserID(handle, email);
            if (userId == null) {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                return;
            }

            boolean exists = tweetServices.isLiked(handle, tweetId, userId);
            if (exists) {
                tweetServices.removeLike(handle, tweetId, userId);
            } else {
                tweetServices.addLike(handle, tweetId, userId);
            }

            ctx.redirect("/app/homepage");
        });
    }
}
