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
import java.util.Objects;

import static io.javalin.rendering.template.TemplateUtil.model;

public class HelloWorld {

    public static void main(String[] args) {
        JavalinRenderer.register(new JavalinPebble(), ".peb", ".pebble");

        Javalin app = Javalin.create(config -> {
                    config.fileRenderer(new JavalinPebble());
                  })
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
                .get("/", ctx -> ctx.render("templates/login.peb"))
                .post("/login",ctx->{
                    // get username and password from form
                    String email = ctx.formParam("email");
                    String password = ctx.formParam("password");
                    String user_name=null;
                    String sql="SELECT * From \"xcloneSchema\".\"user\" where email = ?";
                    int flag_pass=0;
                    int flag_dne =0;
                    int done=0;

                    try (Connection conn = connection.getConnection(); // Assume this method returns a valid connection
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, email);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            while (rs.next()) {
                                String res_email = rs.getString("email");
                                user_name=rs.getString("username");
                                String res_password=rs.getString("password");

                                if(Objects.equals(password, res_password)){
                                    flag_pass=1;
                                }
                                flag_dne=1;
                            }
                            if(flag_dne!=1){
                                ctx.render("templates/login_failed.peb");
                                done=1;
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL Exception: " + e.getMessage());
                    }
                    if(flag_pass==1){
                        ctx.sessionAttribute("user", email);
                        ctx.redirect("/homepage");
                    }
                    else if (flag_pass!=1&&done!=1) {
                        ctx.render("templates/wrong_password.peb");
                    }
                })
                .get("/homepage",ctx->{

                   String email= ctx.sessionAttribute("user");
                    ctx.render("templates/homepage.peb",model("email",email));

                })
                .get("/signup", ctx -> {
                    ctx.render("templates/signup.peb");  // Make sure this template exists
                })
                .post("/signup", ctx -> {
                    String email = ctx.formParam("email");
                    String password = ctx.formParam("password");
                    String user_name = ctx.formParam("username");
                    int flag_exist = 0;
                    String sql = "SELECT * FROM \"xcloneSchema\".\"user\" WHERE email = ?";

                    String ins = "INSERT INTO \"xcloneSchema\".\"user\"" +
                            "(email, password, username) VALUES (?, ?, ?)";

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, email);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                flag_exist = 1;
                                System.out.println("The email already exists");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL Exception: " + e.getMessage());
                    }

                    try (Connection conn = connection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(ins)) {

                        if (flag_exist == 0) {
                            pstmt.setString(1, email);
                            pstmt.setString(2, password);
                            pstmt.setString(3, user_name);
                            pstmt.executeUpdate();
                            System.out.println("Signed up successfully");
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL Exception: " + e.getMessage());
                    }
                })
                .start(8000);
    }
}
