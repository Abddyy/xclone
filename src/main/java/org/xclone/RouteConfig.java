package org.xclone;

import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
//import org.xclone.controllers.TweetController;
import org.xclone.connection;
import org.xclone.controller.AuthenticationController;

import java.sql.Connection;
import java.sql.SQLException;


public class RouteConfig {
    public static void setupRoutes(Javalin app) throws SQLException {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        try {
            Connection conn = connection.getConnection();
            AuthenticationController authenticationController = new AuthenticationController(conn);
            app.get("/", ctx -> ctx.render("templates/main.peb"));
            app.get("/login", authenticationController::renderLogin);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
