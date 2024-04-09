package org.xclone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connection {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = "jdbc:postgresql://localhost/BookStore";
        String user = "postgres";
        String password = "2003";

        return DriverManager.getConnection(url, user, password);
    }
}