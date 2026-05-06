package eventsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton utility class for MySQL JDBC connection.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/event_management_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";          // Change if your XAMPP MySQL has a password

    private static Connection connection = null;

    /**
     * Returns an active Connection, creating one if necessary.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j.jar to project libraries.", e);
        }
        return connection;
    }

    /** Close the shared connection (call on application exit). */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
