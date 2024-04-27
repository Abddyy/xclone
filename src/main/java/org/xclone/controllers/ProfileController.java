package org.xclone.controllers;

import io.javalin.http.Context;
import org.jdbi.v3.core.Jdbi;


import static io.javalin.rendering.template.TemplateUtil.model;

public class ProfileController {
    private final Jdbi jdbi;

    public ProfileController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    public void handleProfile(Context ctx){

        String username = ctx.sessionAttribute("username");
        ctx.render("templates/profile.peb", model("username",username));

    }

}
