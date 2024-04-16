package org.xclone.controllers;

import io.javalin.http.Context;
import org.xclone.Tweet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;


public class TweetController {
    private final Connection connection;

    public TweetController(Connection connection) {
        this.connection = connection;
    }

    public void renderHomepage(Context ctx){
        String sql = "SELECT t.tweet_id, u.username, t.content, t.timestamp, t.location, t.media, t.in_reply_to_tweet_id, " +
                "COUNT(l.like_id) as like_count " +
                "FROM \"xcloneSchema\".\"tweet\" t " +
                "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
                "LEFT JOIN \"xcloneSchema\".\"like\" l ON t.tweet_id = l.tweet_id " +
                "GROUP BY t.tweet_id, u.username " +
                "ORDER BY t.timestamp DESC";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            List<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                tweets.add(new Tweet(
                        rs.getString("tweet_id"),
                        rs.getString("username"), // Use username instead of user_id
                        rs.getString("content"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("location"),
                        rs.getString("media"),
                        rs.getString("in_reply_to_tweet_id"),
                        rs.getInt("like_count")
                ));
            }
            ctx.render("templates/homepage.peb", model("tweets", tweets));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.render("templates/homepage.peb", model("errorMessage", "Failed to load tweets."));
        }
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
        String replyToTweetIdString = ctx.formParam("replyToTweetId"); // Treat as null if not provided

        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());

        Integer replyToTweetId = null; // Default to null if not provided
        if (replyToTweetIdString != null && !replyToTweetIdString.isEmpty() && replyToTweetIdString.matches("\\d+")) {
            replyToTweetId = Integer.parseInt(replyToTweetIdString);
        }

        String sqlUserId = "SELECT user_id FROM \"xcloneSchema\".\"user\" WHERE email = ?";
        String sqlInsertTweet = "INSERT INTO \"xcloneSchema\".\"tweet\" (user_id, content, \"timestamp\", location, media, in_reply_to_tweet_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmtUserId = this.connection.prepareStatement(sqlUserId)) {
            pstmtUserId.setString(1, email);
            ResultSet rs = pstmtUserId.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");

                try (PreparedStatement pstmtInsertTweet = this.connection.prepareStatement(sqlInsertTweet)) {
                    pstmtInsertTweet.setInt(1, userId);
                    pstmtInsertTweet.setString(2, content);
                    pstmtInsertTweet.setTimestamp(3, timestamp);
                    pstmtInsertTweet.setString(4, location);
                    pstmtInsertTweet.setString(5, media);
                    if (replyToTweetId != null) {
                        pstmtInsertTweet.setInt(6, replyToTweetId);
                    } else {
                        pstmtInsertTweet.setNull(6, java.sql.Types.INTEGER);
                    }
                    pstmtInsertTweet.executeUpdate();
                    ctx.redirect("/app/homepage");
                }
            } else {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            e.printStackTrace();
            ctx.render("templates/homepage.peb", model("errorMessage", "Failed to post tweet. Error: " + e.getMessage()));
        }
    }

    public void handleLikeAction(Context ctx){
        String email = ctx.sessionAttribute("email");
        if (email == null) {
            ctx.render("templates/homepage.peb", model("errorMessage", "You must be logged in to like tweets."));
            return;
        }

        String tweetIdStr = ctx.formParam("tweetId");
        int tweetId;
        try {
            tweetId = Integer.parseInt(tweetIdStr);
        } catch (NumberFormatException e) {
            ctx.render("templates/homepage.peb", model("errorMessage", "Invalid tweet ID."));
            return;
        }

        String sqlUserId = "SELECT user_id FROM \"xcloneSchema\".\"user\" WHERE email = ?";
        String sqlCheckLike = "SELECT * FROM \"xcloneSchema\".\"like\" WHERE tweet_id = ? AND user_id = ?";
        String sqlInsertLike = "INSERT INTO \"xcloneSchema\".\"like\" (tweet_id, user_id, \"timestamp\") VALUES (?, ?, NOW())";
        String sqlDeleteLike = "DELETE FROM \"xcloneSchema\".\"like\" WHERE tweet_id = ? AND user_id = ?";

        try (PreparedStatement pstmtUserId = this.connection.prepareStatement(sqlUserId)) {
            pstmtUserId.setString(1, email);
            ResultSet rs = pstmtUserId.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");

                try {
                    this.connection.setAutoCommit(false); // Start transaction
                    // Check if like exists
                    try (PreparedStatement pstmtCheckLike = this.connection.prepareStatement(sqlCheckLike)) {
                        pstmtCheckLike.setInt(1, tweetId);
                        pstmtCheckLike.setInt(2, userId);
                        ResultSet rsLike = pstmtCheckLike.executeQuery();

                        if (rsLike.next()) {
                            // Like exists, perform unlike (delete)
                            try (PreparedStatement pstmtDeleteLike = this.connection.prepareStatement(sqlDeleteLike)) {
                                pstmtDeleteLike.setInt(1, tweetId);
                                pstmtDeleteLike.setInt(2, userId);
                                pstmtDeleteLike.executeUpdate();
                            }
                        } else {
                            // Like does not exist, perform like (insert)
                            try (PreparedStatement pstmtInsertLike = this.connection.prepareStatement(sqlInsertLike)) {
                                pstmtInsertLike.setInt(1, tweetId);
                                pstmtInsertLike.setInt(2, userId);
                                pstmtInsertLike.executeUpdate();
                            }
                        }
                        this.connection.commit(); // Commit transaction
                    }
                    ctx.redirect("/app/homepage");
                } catch (Exception ex) {
                    this.connection.rollback(); // Rollback on error
                    throw ex;
                } finally {
                    this.connection.setAutoCommit(true); // Reset auto-commit
                }
            } else {
                ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            e.printStackTrace();
            ctx.render("templates/homepage.peb", model("errorMessage", "Failed to toggle like. Error: " + e.getMessage()));
        }
    }



}