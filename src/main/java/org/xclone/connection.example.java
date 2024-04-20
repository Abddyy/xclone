//package org.xclone;
//
//import org.jdbi.v3.core.Jdbi;
//import org.jdbi.v3.sqlobject.SqlObjectPlugin;
//
//public class DatabaseConnection {
//
//    private static final String URL = "jdbc:postgresql://localhost/<DB_NAME>";
//    private static final String USER = "<DB_USER>";
//    private static final String PASSWORD = "<DB_PASSWORD>";
//    private static Jdbi jdbi = null;
//
//    public static Jdbi getJdbi() {
//        if (jdbi == null) {
//            jdbi = Jdbi.create(URL, USER, PASSWORD);
//            jdbi.installPlugin(new SqlObjectPlugin());  // This plugin supports SQL Object API
//        }
//        return jdbi;
//    }
//}
