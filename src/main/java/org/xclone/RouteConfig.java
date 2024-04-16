package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
import org.xclone.controllers.AuthenticationController;
import org.xclone.controllers.TweetController;
import java.sql.Connection;
import java.sql.SQLException;


public class RouteConfig {
    public static void setupRoutes(Javalin app) throws SQLException {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        try {
            Connection conn = connection.getConnection();
            AuthenticationController authenticationController = new AuthenticationController(conn);
            TweetController tweetController= new TweetController(conn);

            app.get("/", ctx -> ctx.render("templates/main.peb"));

            app.get("/login", authenticationController::renderLogin);
            app.post("/login", authenticationController::handleLogin);
            app.get("/signup", authenticationController::renderSignup);
            app.post("/signup", authenticationController::handleSignup);
            app.get("/logout", authenticationController::handleLogout);

            app.get("/app/homepage", tweetController::renderHomepage);
            app.post("/app/post", tweetController::handlePostCreate);
            app.post("/app/like", tweetController::handleLikeAction);

            app.before("app/*",ctx -> {
                if ( ctx.sessionAttribute("email") == null) {
                    ctx.redirect("/login");
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
