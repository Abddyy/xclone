package org.xclone.controller;

import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;


public class AuthenticationController {
    private final Connection connection;

    public AuthenticationController(Connection connection) {
        this.connection = connection;
    }

    public void renderLogin(Context ctx) {
        ctx.render("templates/login.peb");
    }

    public void handleLogin(Context ctx) {

    }

    public void renderSignup(Context ctx) {
    }
}