package org.xclone;

import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
import org.jdbi.v3.core.Jdbi;
import org.xclone.controllers.AuthenticationController;
import org.xclone.controllers.ProfileController;
import org.xclone.controllers.TweetController;

public class RouteConfig {
    public static void setupRoutes(Javalin app) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Jdbi jdbi = DatabaseConnection.getJdbi();

        AuthenticationController authenticationController = new AuthenticationController(jdbi);
        TweetController tweetController = new TweetController(jdbi);
        ProfileController profileController=new ProfileController(jdbi);
        app.get("/", ctx -> ctx.render("templates/main.peb"));

        app.get("/login", authenticationController::renderLogin);
        app.post("/login", authenticationController::handleLogin);
        app.get("/signup", authenticationController::renderSignup);
        app.post("/signup", authenticationController::handleSignup);
        app.get("/logout", authenticationController::handleLogout);

        app.get("/profile", profileController::handleProfile);

        app.get("/app/homepage", tweetController::renderHomepage);
        app.post("/app/post", tweetController::handlePostCreate);
        app.post("/app/like", tweetController::handleLikeAction);

        app.before("app/*", ctx -> {
            if (ctx.sessionAttribute("email") == null) {
                ctx.redirect("/?login=true");
            }
        });
    }
}
