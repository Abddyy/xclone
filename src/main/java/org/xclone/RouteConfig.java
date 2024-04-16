package org.xclone;

import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
//import org.xclone.controllers.TweetController;
import org.xclone.connection;

import java.sql.Connection;
import java.sql.SQLException;


public class RouteConfig {
    public static void setupRoutes(Javalin app) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        try {
            Connection conn = connection.getConnection();
//            TweetController tweet_controller = new TweetController(conn);  // Assume Database.getConnection() is a static method that returns a connection

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        app.get("/", ctx -> ctx.render("templates/main.peb"));
//        app.get("/login", tweetController::renderLogin);
//        app.post("/login", tweetController::handleLogin);
//        app.get("/signup", tweetController::renderSignup);
        // More routes here...
    }
}
