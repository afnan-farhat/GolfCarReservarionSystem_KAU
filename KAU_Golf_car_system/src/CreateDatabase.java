import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDatabase {

    public static void CreateDatabase() {
        // Step to create the database if it does not exist
        createDatabaseIfNotExists();

        Connection con = null;

        try {
            // (1) Set the connection URL for the existing database
            // already in the main class

            // (2) Create connection
            con = DriverManager.getConnection(Main.url, Main.username, Main.password);

            // (3) Create necessary tables
            createTable(con, "Student", createUsersTableSQL());
            createTable(con, "GolfCarSchedule", createGolfCarScheduleTableSQL());
            createTable(con, "Reservations", createReservationsTableSQL());

            // (4) Close connection
            con.close();
        } catch (SQLException s) {
            System.out.println("SQL statement is not executed!");
            s.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // create the database if it doesn't exist
    private static void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password)) {
            // Database exists
            System.out.println("Database 'KauGolfCar_system' already exists.");
        } catch (SQLException e) {
            // Database doesn't exist, create it
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", Main.username, Main.password);
                    Statement stmt = conn.createStatement()) {

                String sql = "CREATE DATABASE KauGolfCar_system";
                stmt.executeUpdate(sql);
                System.out.println("Database 'KauGolfCar_system' created successfully.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error creating database: " + ex.getMessage());
            }
        }
    }

    // Directly create the table by executing the SQL query
    private static void createTable(Connection conn, String tableName, String createTableSQL) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);  // Executes CREATE TABLE IF NOT EXISTS
            System.out.println("Table " + tableName + " created successfully!");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    // SQL queries for creating tables
    // creating 'Student' table
    private static String createUsersTableSQL() {
        return "CREATE TABLE IF NOT EXISTS Student ("
                + "STUDENT_ID INT, "
                + "NAME VARCHAR(10) NOT NULL, "
                + "EMAIL VARCHAR(30) NOT NULL, "
                + "PHONE_NUMBER VARCHAR(10)"
                + ")";
    }

    // creating 'GolfCarSchedule' table
    private static String createGolfCarScheduleTableSQL() {
        return "CREATE TABLE IF NOT EXISTS GolfCarSchedule ("
                + "GolfCarNumber INT, "
                + "TripNumber INT, "
                + "Time VARCHAR(5) NOT NULL, "
                + "Destination VARCHAR(10) NOT NULL, "
                + "AvailableSeats INT NOT NULL, "
                + "PRIMARY KEY (GolfCarNumber, TripNumber)"
                + ")";
    }

    // creating 'Reservations' table
    private static String createReservationsTableSQL() {
        return "CREATE TABLE IF NOT EXISTS Reservations ("
                + "StudentID INT, "
                + "GolfCarNumber INT NOT NULL, "
                + "TripNumber INT NOT NULL, "
                + "Time VARCHAR(10) NOT NULL, "
                + "Destination VARCHAR(50) NOT NULL, "
                + "ReservedSeats INT, "
                + "FOREIGN KEY (GolfCarNumber, TripNumber) REFERENCES GolfCarSchedule(GolfCarNumber, TripNumber)"
                + ")";

    }

}
