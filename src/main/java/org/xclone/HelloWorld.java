package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinPebble;
import io.javalin.rendering.JavalinRenderer;



public class HelloWorld {

    public static void main(String[] args) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                })
                .get("/", ctx -> ctx.render("templates/templateFile.peb"))
                .start(8000);
    }
}

