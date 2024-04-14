package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import static io.javalin.rendering.template.TemplateUtil.model;

public class HelloWorld {

    public static void main(String[] args) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                    config.staticFiles.add("/static");
                })
                .get("/", ctx -> {
                    ctx.render("templates/main.peb");
                })
                .get("/login", ctx -> ctx.render("templates/login.peb"))
                .post("/login", ctx -> {
                    String email = ctx.formParam("email");
                    String password = ctx.formParam("password");
                    String sql = "SELECT * FROM \"xcloneSchema\".\"user\" WHERE email = ?";
                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, email);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                String dbPassword = rs.getString("password");
                                if (BCrypt.checkpw(password, dbPassword)) {
                                    ctx.sessionAttribute("user", email);
                                    ctx.redirect("/homepage");
                                } else {
                                    ctx.render("templates/login.peb", model("errorMessage", "Incorrect password."));
                                }
                            } else {
                                ctx.render("templates/login.peb", model("errorMessage", "No user found with that email."));
                            }
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
                    String sql = "SELECT tweet_id, user_id, content, timestamp, location, media, in_reply_to_tweet_id FROM \"xcloneSchema\".\"tweet\" ORDER BY timestamp DESC";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql);
                         ResultSet rs = pstmt.executeQuery()) {
                        List<Tweet> tweets = new ArrayList<>();

                        while (rs.next()) {
                            tweets.add(new Tweet(
                                    rs.getString("tweet_id"),
                                    rs.getString("user_id"),
                                    rs.getString("content"),
                                    rs.getTimestamp("timestamp"),
                                    rs.getString("location"),
                                    rs.getString("media"),
                                    rs.getString("in_reply_to_tweet_id")
                            ));
                        }
                        ctx.render("templates/homepage.peb", model("tweets", tweets));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        ctx.render("templates/homepage.peb", model("errorMessage", "Failed to load tweets."));
                    }
                })
                .start(8000);
    }
}

class Tweet {
    String tweetId, userId, content, location, media, replyToTweetId, formattedTimestamp;
    java.sql.Timestamp timestamp;

    public Tweet(String tweetId, String userId, String content, java.sql.Timestamp timestamp, String location, String media, String replyToTweetId) {
        this.tweetId = tweetId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.location = location;
        this.media = media;
        this.replyToTweetId = replyToTweetId;
        // Format the timestamp here
        this.formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    // Getters
    public String getTweetId() { return tweetId; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public java.sql.Timestamp getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
    public String getMedia() { return media; }
    public String getReplyToTweetId() { return replyToTweetId; }
    public String getFormattedTimestamp() { return formattedTimestamp; }
}
