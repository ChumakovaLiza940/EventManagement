import java.sql.*;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost/your_database";
    private static final String USER = "your";
    private static final String PASSWORD = "your";
    private Connection connection;

    public DatabaseConnector() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
