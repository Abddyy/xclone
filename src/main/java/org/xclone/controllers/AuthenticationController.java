package org.xclone.controllers;

import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import static io.javalin.rendering.template.TemplateUtil.model;


public class AuthenticationController {
    private final Connection connection;

    public AuthenticationController(Connection connection) {
        this.connection = connection;
    }

    public void renderLogin(Context ctx) {
        ctx.render("templates/login.peb");
    }

    public void handleLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String sql = "SELECT user_id, password FROM \"xcloneSchema\".\"user\" WHERE email = ?";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (BCrypt.checkpw(password, dbPassword)) {
                    ctx.sessionAttribute("email", email); // Store email in session
                    ctx.redirect("/app/homepage");
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
    }

    public void renderSignup(Context ctx) {
        ctx.render("templates/signup.peb");
    }

    public void handleSignup(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String username = ctx.formParam("username");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "SELECT * FROM \"xcloneSchema\".\"user\" WHERE email = ?";
        try (
             PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ctx.render("templates/signup.peb", model("errorMessage", "Email already exists. Please use a different email."));
            } else {
                String insertSql = "INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = this.connection.prepareStatement(insertSql)) {
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
    }

    public void handleLogout(Context ctx) {
        ctx.sessionAttribute("email", null);
        ctx.redirect("/");
    }
}