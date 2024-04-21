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
        //String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        AuthentcationQuries authentcationQuries = new AuthentcationQuries();
        jdbi.useHandle(handle -> {
            String dbPassword = authentcationQuries.getLoginQuery(handle, ctx.formParam("email"));

            if (dbPassword != null && BCrypt.checkpw(password, dbPassword)) {
                ctx.sessionAttribute("email", ctx.formParam("email")); // Store email in session
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
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String username = ctx.formParam("username");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        jdbi.inTransaction(handle -> {
            long userCount = handle.createQuery("SELECT COUNT(*) FROM \"xcloneSchema\".\"user\" WHERE email = :email")
                    .bind("email", email)
                    .mapTo(Long.class)
                    .one();

            if (userCount > 0) {
                ctx.render("templates/signup.peb", model("errorMessage", "Email already exists. Please use a different email."));
                return null; // Stop transaction
            } else {
                handle.createUpdate("INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (:email, :password, :username)")
                        .bind("email", email)
                        .bind("password", hashedPassword)
                        .bind("username", username)
                        .execute();
                ctx.sessionAttribute("user", email);
                ctx.redirect("/homepage");
                return null; // Complete transaction
            }
        });
    }


    public void handleLogout(Context ctx) {
        ctx.sessionAttribute("email", null);
        ctx.redirect("/");
    }
}