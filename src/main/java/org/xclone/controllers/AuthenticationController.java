package org.xclone.controllers;

import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import static io.javalin.rendering.template.TemplateUtil.model;
import org.jdbi.v3.core.Jdbi;
import org.xclone.queries.AuthentcationQuries;


public class AuthenticationController {
    private Jdbi jdbi;

    public AuthenticationController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderLogin(Context ctx) {
        ctx.render("templates/login.peb");
    }

    public void handleLogin(Context ctx) {

        String password = ctx.formParam("password");
        AuthentcationQuries authentcationQuries = new AuthentcationQuries();
        jdbi.useHandle(handle -> {
            String dbPassword = authentcationQuries.getUserInfoQuery(handle, ctx.formParam("email"));

            if (dbPassword != null && BCrypt.checkpw(password, dbPassword)) {
                ctx.sessionAttribute("email", ctx.formParam("email"));
                ctx.redirect("/app/homepage");
            } else if (dbPassword != null) {
                ctx.render("templates/login.peb", model("errorMessage", "Incorrect password."));
            } else {
                ctx.render("templates/login.peb", model("errorMessage", "No user found with that email."));
            }
        });
    }

    public void renderSignup(Context ctx) {
        ctx.render("templates/signup.peb");
    }

    public void handleSignup(Context ctx) {
        AuthentcationQuries authentcationQuries = new AuthentcationQuries();

        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String username = ctx.formParam("username");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        jdbi.inTransaction(handle -> {
            long userCount = authentcationQuries.checkSignupAvailability(handle, email);

            if (userCount > 0) {
                ctx.render("templates/signup.peb", model("errorMessage", "Email already exists. Please use a different email."));
                return null; // Stop transaction
            } else {
                authentcationQuries.doSignupQuery(handle, email, hashedPassword, username);
                ctx.sessionAttribute("email", email);
                ctx.redirect("/app/homepage");
                return null; // Complete transaction
            }
        });
    }


    public void handleLogout(Context ctx) {
        ctx.sessionAttribute("email", null);
        ctx.redirect("/");
    }
}