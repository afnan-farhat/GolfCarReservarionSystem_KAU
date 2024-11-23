
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.table.DefaultTableModel;

public class Reservation_Proccess {

    static Connection conn;
    static Lock lock = new ReentrantLock();  // Lock to synchronize seat reservation operations

    public static void createSeatSelectionFrame(DefaultTableModel model, int selectedRow, int availableSeats) {
        // Create the seat selection frame
        JFrame seatFrame = new JFrame("Seat Selection");
        seatFrame.setSize(500, 500);
        seatFrame.setLayout(new GridLayout(8, 6));
        seatFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Back button to navigate back to the table view
        JButton backButton = new JButton("Back to Table");
        backButton.setBackground(Main.BUTTON_COLOR);
        backButton.setForeground(Main.TEXT_COLOR);
        seatFrame.setLocationRelativeTo(null);

        // Panel for the back button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Main.BACKGROUND_COLOR);

        // Add buttons for available seats
        for (int i = 0; i < availableSeats; i++) {
            JButton seatButton = new JButton("Seat " + (i + 1));
            seatButton.setBackground(Main.BUTTON_COLOR);
            seatButton.setForeground(Main.TEXT_COLOR);

            // Action listener for seat reservation
            seatButton.addActionListener(e -> {
                lock.lock();  // Ensure thread safety for seat reservation
                try {
                    String seatNum = seatButton.getText();
                    if (availableSeats <= 0) {
                        // Notify user if the seat is no longer available
                        JOptionPane.showMessageDialog(seatFrame, "Sorry, Seat is no longer available.");
                    } else {
                        // Establish a database connection if not already connected
                        try {
                            conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        // Retrieve trip number and attempt to update seat availability
                        int tripNum = (int) model.getValueAt(selectedRow, 1);
                        if (UpdateAvailableSeats(conn, tripNum)) {
                            JOptionPane.showMessageDialog(seatFrame, "Thanks for your reservation.");
                            InsertReservation(model, selectedRow);
                        } else {
                            JOptionPane.showMessageDialog(seatFrame, "Sorry, error occurred while making the reservation. Try again.");
                        }
                    }
                } finally {
                    lock.unlock();  // Release the lock after operation
                }
                seatFrame.dispose();
                // Recreate the menu view to reflect changes
                MenuView.createMenuView();
            });
            seatFrame.add(seatButton);
        }

        // Add buttons for unavailable seats
        int totalSeats = 14;  // Total number of seats per vehicle
        for (int i = availableSeats; i < totalSeats; i++) {
            JButton seatButton = new JButton("Seat " + (i + 1));
            seatButton.setEnabled(false);
            seatButton.setBackground(Color.LIGHT_GRAY);
            seatFrame.add(seatButton);
        }

        // Action listener for the back button to return to the table view
        backButton.addActionListener(e -> {
            seatFrame.dispose();
            TableView.createTableView();
        });

        buttonPanel.add(backButton);
        seatFrame.add(buttonPanel, BorderLayout.PAGE_END);

        // Display the seat selection frame
        seatFrame.setVisible(true);

        // Background thread to periodically refresh seat availability
        new Thread(() -> {
            while (true) {
                try {
                    refreshSeatAvailability(seatFrame, model, selectedRow);  // Update seat status
                    Thread.sleep(5000);  // Refresh interval (5 seconds)
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    // Periodically check and update the seat availability
    private static void refreshSeatAvailability(JFrame seatFrame, DefaultTableModel model, int selectedRow) {
        // Re-fetch available seats from the database
        int tripNum = (int) model.getValueAt(selectedRow, 1);
        int availableSeats = getAvailableSeats(tripNum);

        // Update seat buttons in the UI
        SwingUtilities.invokeLater(() -> {
            for (Component component : seatFrame.getContentPane().getComponents()) {
                if (component instanceof JButton) {
                    JButton seatButton = (JButton) component;
                    String seatText = seatButton.getText();

                    // Check if seat is available and update button properties
                    int seatNumber = Integer.parseInt(seatText.split(" ")[1]);

                    if (seatNumber > availableSeats) {
                        seatButton.setEnabled(false);
                        seatButton.setBackground(Color.LIGHT_GRAY);
                    } else {
                        seatButton.setEnabled(true);
                        seatButton.setBackground(Main.BUTTON_COLOR);
                    }
                }
            }
        });
    }

    // Method to fetch available seats from the database
    private static int getAvailableSeats(int tripNumber) {
        String query = "SELECT AvailableSeats FROM GolfCarSchedule WHERE TripNumber = ?";
        try ( Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);  PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tripNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("AvailableSeats");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;  // Return 0 if there was an error or no result
    }

    // Updates the available seats for a specific trip in the database
    private static boolean UpdateAvailableSeats(Connection conn, int tripNumber) {
        // SQL query to decrement the number of available seats for the given trip
        String updateSeatsSQL = "UPDATE GolfCarSchedule SET AvailableSeats = AvailableSeats - 1 WHERE TripNumber = ?";
        try ( PreparedStatement updateStmt = conn.prepareStatement(updateSeatsSQL)) {
            updateStmt.setInt(1, tripNumber); // Set the trip number in the query
            int rowsAffected = updateStmt.executeUpdate(); // Execute the update query
            return rowsAffected > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            e.printStackTrace(); // Print the exception stack trace for debugging
            return false; // Return false if an exception occurs
        }
    }

// Inserts a new reservation record into the Reservations table
    private static void InsertReservation(DefaultTableModel model, int selectedRow) {
        try {
            // Establish a connection to the database
            conn = DriverManager.getConnection(Main.url, Main.username, Main.password);

            // Retrieve trip details from the selected row of the table
            int golfNo = (int) model.getValueAt(selectedRow, 0); // Golf cart number
            int tripNum = (int) model.getValueAt(selectedRow, 1); // Trip number
            String time = (String) model.getValueAt(selectedRow, 2); // Time of the trip
            String destination = (String) model.getValueAt(selectedRow, 3); // Destination of the trip

            // SQL query to insert a new reservation record
            String insertSQL = "INSERT INTO Reservations (StudentID, GolfCarNumber, TripNumber, Time, Destination , ReservedSeats) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try ( PreparedStatement insertPstmt = conn.prepareStatement(insertSQL)) {
                insertPstmt.setInt(1, Main.studentId); // Student ID making the reservation
                insertPstmt.setInt(2, golfNo); // Golf cart number
                insertPstmt.setInt(3, tripNum); // Trip number
                insertPstmt.setString(4, time); // Trip time
                insertPstmt.setString(5, destination); // Trip destination
                insertPstmt.setInt(6, 1); // Reserved seats (always 1 for individual reservation)
                insertPstmt.executeUpdate(); // Execute the insert query
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Print the exception stack trace for debugging
        }
    }
}
