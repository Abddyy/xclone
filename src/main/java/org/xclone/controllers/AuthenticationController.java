package org.xclone.controllers;

import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;

import static io.javalin.rendering.template.TemplateUtil.model;
import org.jdbi.v3.core.Jdbi;
import org.xclone.services.AuthentcationServices;

import java.util.Map;

public class AuthenticationController {
    private final Jdbi jdbi;

    public AuthenticationController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderLogin(Context ctx) {
        ctx.render("templates/login.peb");
    }

    public void handleLogin(Context ctx) {
        String password = ctx.formParam("password");
        AuthentcationServices authenticationServices = new AuthentcationServices();
        String username =authenticationServices.getUserNameQuery(jdbi.open(),ctx.formParam("email"));

                jdbi.useHandle(handle -> {
            String dbPassword = authenticationServices.getUserInfoQuery(handle, ctx.formParam("email"));

            if (dbPassword != null && BCrypt.checkpw(password, dbPassword)) {
                ctx.sessionAttribute("email", ctx.formParam("email"));
                ctx.sessionAttribute("username", username);
                ctx.json(Map.of("success", true, "redirect", "/app/homepage"));
            } else if (dbPassword != null) {
                ctx.json(Map.of("success", false, "message", "Incorrect password."));
            } else {
                ctx.json(Map.of("success", false, "message", "No user found with that email."));
            }
        });
    }

    public void renderSignup(Context ctx) {
        ctx.render("templates/signup.peb");
    }

    public void handleSignup(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String username = ctx.formParam("username");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        AuthentcationServices authenticationServices = new AuthentcationServices();

        jdbi.useTransaction(handle -> {
            long userCount = authenticationServices.checkSignupAvailability(handle, email);
            if (userCount > 0) {
                ctx.json(Map.of("success", false, "message", "Email already exists. Please use a different email."));
            } else {
                authenticationServices.doSignupQuery(handle, email, hashedPassword, username);
                ctx.json(Map.of("success", true, "redirect", "/app/homepage"));  // Ensure this URL is correct
            }
        });
    }

    public void handleLogout(Context ctx) {
        ctx.sessionAttribute("email", null);
        ctx.redirect("/");
    }
}
