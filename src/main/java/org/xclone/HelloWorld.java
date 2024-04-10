package org.xclone;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinPebble;
import io.javalin.rendering.JavalinRenderer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

public class HelloWorld {

    public static void main(String[] args) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                })
                .get("/", ctx -> ctx.render("templates/templateFile.peb"))
                .get("/tweets", ctx -> {

                    String sql = "SELECT * FROM \"xcloneSchema\".\"tweet\"";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        ResultSet rs = pstmt.executeQuery();
                        List<String> tweets = new ArrayList<>();


                        while (rs.next()) {
                            // Assuming you have columns like 'id', 'content' in your 'tweet' table
                            int id = rs.getInt("tweet_id"); // Or the appropriate column label
                            String content = rs.getString("content"); // Or the appropriate column label
                            //System.out.println("Tweet ID: " + id + ", Content: " + content);
                            tweets.add(content);

                        }
                        ctx.render("templates/tweet_list.peb", model("tweets", tweets));

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .start(8000);
    }
}
