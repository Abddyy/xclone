package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import static io.javalin.rendering.template.TemplateUtil.model;

public class HelloWorld {

    public static void main(String[] args) throws SQLException {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                    config.staticFiles.add("/static");
                })
/*
               .get("/", ctx -> {
                    ctx.render("templates/main.peb");
                })

                .get("/login", ctx -> ctx.render("templates/login.peb"))
                .post("/login", ctx -> {
                    String email = ctx.formParam("email");
                    String password = ctx.formParam("password");
                    String sql = "SELECT user_id, password FROM \"xcloneSchema\".\"user\" WHERE email = ?";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, email);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            String dbPassword = rs.getString("password");
                            if (BCrypt.checkpw(password, dbPassword)) {
                                ctx.sessionAttribute("email", email); // Store email in session
                                ctx.redirect("/homepage");
                            } else {
                                ctx.render("templates/login.peb", model("errorMessage", "Incorrect password."));
                            }
                        } else {
                            ctx.render("templates/login.peb", model("errorMessage", "No user found with that email."));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        ctx.render("templates/login.peb", model("errorMessage", "An error occurred. Please try again later."));
                    }
                })
                .get("/signup", ctx -> {
                    ctx.render("templates/signup.peb");
                })
                .post("/signup", ctx -> {
                    String email = ctx.formParam("email");
                    String password = ctx.formParam("password");
                    String username = ctx.formParam("username");
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    String sql = "SELECT * FROM \"xcloneSchema\".\"user\" WHERE email = ?";
                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, email);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            ctx.render("templates/signup.peb", model("errorMessage", "Email already exists. Please use a different email."));
                        } else {
                            String insertSql = "INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (?, ?, ?)";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setString(1, email);
                                insertStmt.setString(2, hashedPassword);
                                insertStmt.setString(3, username);
                                insertStmt.executeUpdate();
                                ctx.sessionAttribute("user", email);
                                ctx.redirect("/homepage");
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        ctx.render("templates/signup.peb", model("errorMessage", "An error occurred during signup. Please try again."));
                    }
                })
                .get("/homepage", ctx -> {
                    String sql = "SELECT t.tweet_id, u.username, t.content, t.timestamp, t.location, t.media, t.in_reply_to_tweet_id, " +
                            "COUNT(l.like_id) as like_count " +
                            "FROM \"xcloneSchema\".\"tweet\" t " +
                            "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
                            "LEFT JOIN \"xcloneSchema\".\"like\" l ON t.tweet_id = l.tweet_id " +
                            "GROUP BY t.tweet_id, u.username " +
                            "ORDER BY t.timestamp DESC";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql);
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
                })
                .post("/post", ctx -> {
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

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmtUserId = conn.prepareStatement(sqlUserId)) {
                        pstmtUserId.setString(1, email);
                        ResultSet rs = pstmtUserId.executeQuery();

                        if (rs.next()) {
                            int userId = rs.getInt("user_id");

                            try (PreparedStatement pstmtInsertTweet = conn.prepareStatement(sqlInsertTweet)) {
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
                                ctx.redirect("/homepage");
                            }
                        } else {
                            ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                        }
                    } catch (SQLException e) {
                        System.err.println("Error executing SQL: " + e.getMessage());
                        e.printStackTrace();
                        ctx.render("templates/homepage.peb", model("errorMessage", "Failed to post tweet. Error: " + e.getMessage()));
                    }
                })
                .post("/like", ctx -> {
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

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmtUserId = conn.prepareStatement(sqlUserId)) {
                        pstmtUserId.setString(1, email);
                        ResultSet rs = pstmtUserId.executeQuery();

                        if (rs.next()) {
                            int userId = rs.getInt("user_id");

                            try {
                                conn.setAutoCommit(false); // Start transaction
                                // Check if like exists
                                try (PreparedStatement pstmtCheckLike = conn.prepareStatement(sqlCheckLike)) {
                                    pstmtCheckLike.setInt(1, tweetId);
                                    pstmtCheckLike.setInt(2, userId);
                                    ResultSet rsLike = pstmtCheckLike.executeQuery();

                                    if (rsLike.next()) {
                                        // Like exists, perform unlike (delete)
                                        try (PreparedStatement pstmtDeleteLike = conn.prepareStatement(sqlDeleteLike)) {
                                            pstmtDeleteLike.setInt(1, tweetId);
                                            pstmtDeleteLike.setInt(2, userId);
                                            pstmtDeleteLike.executeUpdate();
                                        }
                                    } else {
                                        // Like does not exist, perform like (insert)
                                        try (PreparedStatement pstmtInsertLike = conn.prepareStatement(sqlInsertLike)) {
                                            pstmtInsertLike.setInt(1, tweetId);
                                            pstmtInsertLike.setInt(2, userId);
                                            pstmtInsertLike.executeUpdate();
                                        }
                                    }
                                    conn.commit(); // Commit transaction
                                }
                                ctx.redirect("/homepage");
                            } catch (Exception ex) {
                                conn.rollback(); // Rollback on error
                                throw ex;
                            } finally {
                                conn.setAutoCommit(true); // Reset auto-commit
                            }
                        } else {
                            ctx.render("templates/homepage.peb", model("errorMessage", "User not found."));
                        }
                    } catch (SQLException e) {
                        System.err.println("Error executing SQL: " + e.getMessage());
                        e.printStackTrace();
                        ctx.render("templates/homepage.peb", model("errorMessage", "Failed to toggle like. Error: " + e.getMessage()));
                    }
                })
                .get("logout", ctx -> {
                    ctx.sessionAttribute("email", null);
                    ctx.redirect("/");
                })
*/

                .start(8000);

        RouteConfig.setupRoutes(app);
    }
}

class Tweet {
    String tweetId, username, content, location, media, replyToTweetId, formattedTimestamp;
    java.sql.Timestamp timestamp;
    int likeCount;

    public Tweet(String tweetId, String username, String content, java.sql.Timestamp timestamp, String location, String media, String replyToTweetId, int likeCount) {
        this.tweetId = tweetId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.location = location;
        this.media = media;
        this.replyToTweetId = replyToTweetId;
        this.likeCount = likeCount;
        this.formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    // Getters
    public String getTweetId() { return tweetId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public java.sql.Timestamp getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
    public String getMedia() { return media; }
    public String getReplyToTweetId() { return replyToTweetId; }
    public String getFormattedTimestamp() { return formattedTimestamp; }
    public int getLikeCount() { return likeCount; }
}
