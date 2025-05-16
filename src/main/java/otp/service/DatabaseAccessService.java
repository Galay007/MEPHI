package otp.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseAccessService {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            Properties props = new Properties();
            props.load(DatabaseAccessService.class.getClassLoader().getResourceAsStream("db.properties"));
            String dockerUrl = System.getenv("DB_URL");
            if (dockerUrl == null || dockerUrl.isEmpty()) {
                URL = props.getProperty("db.url");
            } else {
                URL = dockerUrl;
            }
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Ошибка загрузки параметров БД из " + ": " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    System.out.println("Error " + e.getMessage());
                }
            }
        }
    }
}
