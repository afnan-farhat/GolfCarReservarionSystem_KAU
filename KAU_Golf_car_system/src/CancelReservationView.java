
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class CancelReservationView {

    // attribute of seats
    static int totalReservedSeats;

    // Define method to create the "Cancel Reservation" page
    static void createCancelView() {
        // Create a new JFrame for the Cancel Reservation Page
        JFrame tableCFrame = new JFrame("Cancel Reservation Page");

        // Create a label to display the logo
        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);

        // Add the logo label to the top (North) of the JFrame
        tableCFrame.add(logoLabel, BorderLayout.NORTH);

        // Center the JFrame on the screen and set size to 550x500 and color of background
        tableCFrame.setLocationRelativeTo(null);
        tableCFrame.setSize(550, 500);
        tableCFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Define column names for the table
        String[] columnNames = {"Select", "Golf Car Number", "Trip Number", "Time", "Destination", "Available Seats"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            // Override the method to specify the type of the first column (checkboxes)
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;  // Return Boolean class for checkboxes
                }
                return super.getColumnClass(columnIndex);  // For other columns, return the default class
            }
        };

        // Create a new JTable using the model and set the color of background and text
        JTable table = new JTable(model);
        table.setBackground(Main.BACKGROUND_COLOR);
        table.setForeground(Main.TEXT_COLOR);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        tableCFrame.add(scrollPane);

        // Load the user's reservations into the table
        loadReservationData(model);

        // Create a button to cancel reservations and set the color of background and text 
        JButton button = new JButton("Cancel Reservation");
        button.setBackground(Main.BUTTON_COLOR);
        button.setForeground(Main.TEXT_COLOR);

        // Add an action listener to the cancel button
        button.addActionListener(e -> {
            // Show a confirmation dialog when the button is clicked
            int confirmed = JOptionPane.showConfirmDialog(
                    tableCFrame,
                    "Are you sure you want to cancel the selected reservations?", // Message in the confirmation dialog
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION // Yes/No option buttons
            );

            if (confirmed == JOptionPane.YES_OPTION) {  // If the user confirms cancellation
                for (int i = 0; i < table.getRowCount(); i++) {  // Iterate over the table rows
                    Boolean isChecked = (Boolean) table.getValueAt(i, 0);  // Check if the row is selected
                    if (isChecked != null && isChecked) {
                        int golfCarNumber = (int) table.getValueAt(i, 1);  // Get the GolfCarNumber
                        int tripNumber = (int) table.getValueAt(i, 2);  // Get the TripNumber

                        // Remove the row from the UI
                        model.removeRow(i);
                        i--;  // Adjust the index after removing a row

                        // Cancel the reservation in the database
                        cancelReservation(golfCarNumber, tripNumber);
                    }
                }
            }
        });

        // Create a button to go back to the main menu and Set the colro of background and text
        JButton backButton = new JButton("Back to Menu");
        backButton.setBackground(Main.BUTTON_COLOR);
        backButton.setForeground(Main.TEXT_COLOR);

        // Add an action listener to the back button
        backButton.addActionListener(e -> {
            tableCFrame.dispose();  // Close the current JFrame
            MenuView.createMenuView();  // Call method to open the main menu view
        });

        // Create a new JPanel to hold the buttons and Set the colro of background
        JPanel panel = new JPanel();
        panel.setBackground(Main.BACKGROUND_COLOR);
        panel.add(backButton);
        panel.add(button);
        tableCFrame.add(panel, BorderLayout.SOUTH);  // Add the panel (with buttons) to the bottom (South) of the JFrame

        tableCFrame.setVisible(true);  // Make the JFrame visible
    }

    private static void loadReservationData(DefaultTableModel model) {
        String query = "SELECT GolfCarNumber, TripNumber, Time, Destination, SUM(ReservedSeats) AS TotalReservedSeats "
                + "FROM Reservations WHERE StudentID = ? "
                + "GROUP BY TripNumber, GolfCarNumber, Time, Destination";

        try ( Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);  PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the StudentID parameter
            stmt.setInt(1, Main.studentId);

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Process the result set and populate the table
            while (rs.next()) {
                int golfCarNumber = rs.getInt("GolfCarNumber");
                int tripNumber = rs.getInt("TripNumber");
                String time = rs.getString("Time");
                String destination = rs.getString("Destination");
                totalReservedSeats = rs.getInt("TotalReservedSeats");  // Get the sum of reserved seats

                // Add the data to the table model, defaulting the checkbox to false
                model.addRow(new Object[]{false, golfCarNumber, tripNumber, time, destination, totalReservedSeats});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cancelReservation(int golfCarNumber, int tripNumber) {
        // SQL to delete the reservation from the database
        String deleteSQL = "DELETE FROM Reservations WHERE StudentID = ? AND GolfCarNumber = ? AND TripNumber = ?";

        try ( Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);  PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {

            deleteStmt.setInt(1, Main.studentId);
            deleteStmt.setInt(2, golfCarNumber);
            deleteStmt.setInt(3, tripNumber);

            // Execute the delete statement
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected > 0) {

                UpdateAvailableSeats(conn, tripNumber);
                JOptionPane.showMessageDialog(null, "Reservation cancelled successfully.");
                // Server.sendMessageToServer("Reservation cancelled successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Error cancelling reservation. Please try again.");

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Method to update the available seats for a specific trip in the database
    private static void UpdateAvailableSeats(Connection conn, int tripNumber) throws SQLException {
        // SQL query to update the AvailableSeats column by incrementing it with a specified value
        String updateSeatsSQL = "UPDATE GolfCarSchedule SET AvailableSeats = AvailableSeats + ? WHERE TripNumber = ?";

        // Try-with-resources block to ensure the PreparedStatement is automatically closed
        try ( PreparedStatement updateStmt = conn.prepareStatement(updateSeatsSQL)) {
            // Set the first parameter (?) in the SQL query to the value of totalReservedSeats
            updateStmt.setInt(1, totalReservedSeats);

            // Set the second parameter (?) in the SQL query to the provided tripNumber
            updateStmt.setInt(2, tripNumber);

            // Execute the update query to modify the database
            updateStmt.executeUpdate();
        } catch (SQLException ex) {
            // Print the stack trace if an SQL exception occurs
            ex.printStackTrace();
        }
    }
}
