package org.xclone;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinPebble;
import io.javalin.rendering.JavalinRenderer;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class HelloWorld {

    private static final int RANDOM_STRING_LENGTH = 10;

    public static void main(String[] args) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                })
                .get("/", ctx -> ctx.render("templates/templateFile.peb"))
                .get("/add-random-title", ctx -> {
                    String generatedString = generateRandomString(RANDOM_STRING_LENGTH);
                    String sql = "INSERT into \"DemoSchema\".\"Post\" (title)\n values (?)";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, generatedString);


                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }).start(8000);
    }

    private static String generateRandomString(int length) {
        // Use alphanumeric characters for generating the string
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
