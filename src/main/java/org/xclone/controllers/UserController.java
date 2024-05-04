package org.xclone.controllers;
import io.javalin.http.Context;
import org.xclone.User;
import org.jdbi.v3.core.Jdbi;
import org.xclone.services.UserServices;
import java.util.List;
import static io.javalin.rendering.template.TemplateUtil.model;

public class UserController {

    private final UserServices userServices = new UserServices();
    private final Jdbi jdbi;

    public UserController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void renderExplore(Context ctx) {
        List<User> users = userServices.userlist(jdbi);
        String username = ctx.sessionAttribute("username");
        ctx.render("templates/explore.peb", model("users", users, "username", username));
    }

}
