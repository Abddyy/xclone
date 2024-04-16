package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.rendering.template.JavalinPebble;
import java.sql.*;
public class Main {

    public static void main(String[] args) throws SQLException {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                    config.staticFiles.add("/static");
                })
                .start(8000);

        RouteConfig.setupRoutes(app);
    }
}


