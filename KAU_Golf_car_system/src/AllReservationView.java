
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class AllReservationView {

    // Define method to create the "All Reservations" page
    public static void createAllReservationView() throws SQLException { 
        // Create a new JFrame for the All Reservations page
        JFrame tableFrame = new JFrame("All Reservations");  

        // Create a label to display the logo
        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);  
        // Add the logo label to the top (North) of the JFrame
        tableFrame.add(logoLabel, BorderLayout.NORTH);  

        // Center the JFrame on the screen
        tableFrame.setLocationRelativeTo(null);  
        tableFrame.setSize(550, 500);
        // Set the background color of the JFrame's content pane
        tableFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);  

        // Define column names for the table
        String[] columnNames = {"Golf Car Number", "Trip Number", "Time", "Destination", "ReservedSeats"};  
        // Create a table model with column names and zero rows
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);  
        // Create a new JTable using the model
        JTable table = new JTable(model);  
        // Set the background and text color of the table
        table.setBackground(Main.BACKGROUND_COLOR);  
        table.setForeground(Main.TEXT_COLOR);  

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);  

        loadReservationData(model);
        
        // Create a button to go back to the main menu and set the color od background and text
        JButton backButton = new JButton("Back to Menu");  
        backButton.setBackground(Main.BUTTON_COLOR); 
        backButton.setForeground(Main.TEXT_COLOR);  
        
        // Add an action listener to the back button
        backButton.addActionListener(e -> {  
            tableFrame.dispose();  // Close the current JFrame
            MenuView.createMenuView();  // Call method to open the main menu view
        });

        // Create a new JPanel to hold the back button and set the color pf background
        JPanel panel = new JPanel();  
        panel.setBackground(Main.BACKGROUND_COLOR);  
        // Add the back button to the panel
        panel.add(backButton); 

        // Add the scroll pane (with the table) to the center of the JFrame
        tableFrame.add(scrollPane, BorderLayout.CENTER);  
        tableFrame.add(panel, BorderLayout.SOUTH);  
        tableFrame.setVisible(true);  // Make the JFrame visible
    }

    private static void loadReservationData(DefaultTableModel model) {
        String query = "SELECT GolfCarNumber, TripNumber, Time, Destination, SUM(ReservedSeats) AS TotalReservedSeats "
                + "FROM Reservations WHERE StudentID = ? "
                + "GROUP BY TripNumber, GolfCarNumber, Time, Destination";

        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement stmt = conn.prepareStatement(query)) {

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
                int totalReservedSeats = rs.getInt("TotalReservedSeats");  // Get the sum of reserved seats

                // Add the data to the table model
                model.addRow(new Object[]{golfCarNumber, tripNumber, time, destination, totalReservedSeats});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}