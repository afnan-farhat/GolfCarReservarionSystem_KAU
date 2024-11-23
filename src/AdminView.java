import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminView {

    // Define constant colors for consistent theme across the application
    private static final Color TEXT_COLOR = new Color(85, 136, 59);
    private static final Color BUTTON_COLOR = new Color(193, 232, 153);
    private static final Color BACKGROUND_COLOR = new Color(230, 240, 220);

    // Declare buttons
    private static JButton manageGolfCarsButton;
    private static JButton ChattingButton;

    // Declare network components
    private static Socket socket;
    private static PrintWriter outputNot;
    private static JFrame golfCarFrame;

    public static void main(String[] args) {
        // Start the application by displaying the admin menu
        AdminView();
    }

    public static void AdminView() {
        // Create the main admin menu window
        JFrame menuFrame = new JFrame("Admin Menu");
        menuFrame.setSize(500, 350);
        // Load and display the logo at the top of the menu
        ImageIcon logo = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logo);
        menuFrame.add(logoLabel, BorderLayout.NORTH);

        // Set default close operation and center the frame
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Create buttons for golf car management and chatting 
        manageGolfCarsButton = new JButton("Manage Golf Cars");
        ChattingButton = new JButton("Chatting");

        // Apply consistent button colors
        manageGolfCarsButton.setBackground(BUTTON_COLOR);
        manageGolfCarsButton.setForeground(TEXT_COLOR);
        ChattingButton.setBackground(BUTTON_COLOR);
        ChattingButton.setForeground(TEXT_COLOR);

        // Set action listeners for the buttons
        manageGolfCarsButton.addActionListener(e -> {
            // Show the golf car management view and close the admin menu
            showGolfCarManagement();
            menuFrame.dispose();
        });

        ChattingButton.addActionListener(e -> {
            // Start the chat server
            Server.ServerChat();
        });

        // Set up the layout for the buttons
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // 2 rows, 1 column, 10px gap
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(manageGolfCarsButton);
        panel.add(ChattingButton);

        // Add the panel to the main window
        menuFrame.add(panel, BorderLayout.CENTER);

        // Create a close button at the bottom of the menu
        JPanel closeButtonPanel = new JPanel();
        JButton closeButton = new JButton("Close Menu");
        closeButton.setBackground(BUTTON_COLOR);
        closeButton.setForeground(TEXT_COLOR);
        closeButtonPanel.setBackground(BACKGROUND_COLOR);
        closeButtonPanel.add(closeButton);

        // Add the action listener for the close button
        closeButton.addActionListener(e -> menuFrame.dispose());

        // Add the close button panel at the bottom
        menuFrame.add(closeButtonPanel, BorderLayout.SOUTH);
        menuFrame.setVisible(true);
    }

    // Method to manage golf cars (showing and handling data)
    public static void showGolfCarManagement() {
        // Create the golf car management window
        golfCarFrame = new JFrame("Golf Car Manage");
        golfCarFrame.setSize(500, 500);
        golfCarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        golfCarFrame.setLocationRelativeTo(null);
        golfCarFrame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Define column names for the golf car management table
        String[] columnNames = {"Car Number", "Number of Trips", "Time", "Destination", "Available Seats"};
        DefaultTableModel golfCarTableModel = new DefaultTableModel(columnNames, 0);
        JTable golfCarTable = new JTable(golfCarTableModel);
        golfCarTable.setBackground(BACKGROUND_COLOR);
        golfCarTable.setForeground(TEXT_COLOR);
        JScrollPane scrollPane = new JScrollPane(golfCarTable);

        // Load data from the database
        loadGolfCarData(golfCarTableModel);

        // Create input fields to add new golf car information
        JLabel carNumberLabel = new JLabel("Car Number:");
        carNumberLabel.setForeground(TEXT_COLOR);
        JTextField carNumberField = new JTextField(10);

        JLabel tripsLabel = new JLabel("Number of Trips:");
        tripsLabel.setForeground(TEXT_COLOR);
        JTextField tripsField = new JTextField(10);

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setForeground(TEXT_COLOR);
        JTextField timeField = new JTextField(10);

        JLabel destinationLabel = new JLabel("Destination:");
        destinationLabel.setForeground(TEXT_COLOR);
        JTextField destinationField = new JTextField(10);

        JLabel availableSeatsLabel = new JLabel("Available Seats:");
        availableSeatsLabel.setForeground(TEXT_COLOR);
        JTextField availableSeatsField = new JTextField(10);

        // Set up a grid layout for the input fields
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.add(carNumberLabel);
        inputPanel.add(carNumberField);
        inputPanel.add(tripsLabel);
        inputPanel.add(tripsField);
        inputPanel.add(timeLabel);
        inputPanel.add(timeField);
        inputPanel.add(destinationLabel);
        inputPanel.add(destinationField);
        inputPanel.add(availableSeatsLabel);
        inputPanel.add(availableSeatsField);

        // Set up the button panel with buttons for adding, deleting, and navigating
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton addCarButton = new JButton("Add Golf Car");
        addCarButton.setBackground(BUTTON_COLOR);
        addCarButton.setForeground(TEXT_COLOR);

        JButton deleteCarButton = new JButton("Delete Golf Car");
        deleteCarButton.setBackground(BUTTON_COLOR);
        deleteCarButton.setForeground(TEXT_COLOR);

        JButton backButton = new JButton("Back to Menu");
        backButton.setBackground(BUTTON_COLOR);
        backButton.setForeground(TEXT_COLOR);

        // Add buttons to the button panel
        buttonPanel.add(backButton);
        buttonPanel.add(addCarButton);
        buttonPanel.add(deleteCarButton);

        // Add actions for the buttons (Add, Delete, Back)
        addCarButton.addActionListener(e -> {
            // Add the new golf car to the table and database
            String carNumber = carNumberField.getText();
            String trips = tripsField.getText();
            String time = timeField.getText();
            String destination = destinationField.getText();
            String availableSeats = availableSeatsField.getText();

            if (!carNumber.isEmpty() && !trips.isEmpty() && !time.isEmpty() && !destination.isEmpty() && !availableSeats.isEmpty()) {
                try {
                    int carNumberInt = Integer.parseInt(carNumber);
                    int tripsInt = Integer.parseInt(trips);
                    int availableSeatsInt = Integer.parseInt(availableSeats);

                    addGolfCar(golfCarTableModel, carNumberInt, tripsInt, time, destination, availableSeatsInt);
                    JTextField[] fields = {carNumberField, tripsField, timeField, destinationField, availableSeatsField};
                    clearFields(fields);
                } catch (NumberFormatException ex) {
                    // Show error message if the input is not a valid number
                    JOptionPane.showMessageDialog(golfCarFrame, "Please enter valid numbers for Car Number, Trip Number, and Available Seats.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Show error message if any field is empty
                JOptionPane.showMessageDialog(golfCarFrame, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }

        });

        // Action to delete a golf car when selected in the table
        deleteCarButton.addActionListener(e -> {
            int selectedRow = golfCarTable.getSelectedRow();
            if (selectedRow != -1) {
                String carNumber = (String) golfCarTableModel.getValueAt(selectedRow, 0);
                String tripNumber = (String) golfCarTableModel.getValueAt(selectedRow, 1);
                deleteGolfCar(golfCarTableModel, selectedRow, carNumber, tripNumber);
            } else {
                // Show error if no row is selected
                JOptionPane.showMessageDialog(golfCarFrame, "Please select a row to delete.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Go back to the admin menu
        backButton.addActionListener(e -> {
            golfCarFrame.dispose();
            AdminView();
        });

        // Add components to the golf car management window
        golfCarFrame.add(scrollPane, BorderLayout.CENTER);
        golfCarFrame.add(inputPanel, BorderLayout.NORTH);
        golfCarFrame.add(buttonPanel, BorderLayout.SOUTH);
        golfCarFrame.setVisible(true);
    }

    // Clears the input fields after adding a new golf car
    private static void clearFields(JTextField[] fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }

    // Loads golf car data from the database into the table
    private static void loadGolfCarData(DefaultTableModel model) {
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM golfcarschedule")) {

            while (rs.next()) {
                String carNumber = rs.getString("GolfCarNumber");
                String trips = rs.getString("TripNumber");
                String time = rs.getString("Time");
                String destination = rs.getString("Destination");
                String availableSeats = rs.getString("AvailableSeats");
                model.addRow(new Object[]{carNumber, trips, time, destination, availableSeats});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adds a new golf car to the table and database
    public static void addGolfCar(DefaultTableModel model, int carNumber, int trips, String time, String destination, int availableSeats) {
        // Check if the car number and trip number already exist
        if (isDuplicateEntry(carNumber, trips)) {
            JOptionPane.showMessageDialog(null, "This car number and trip combination already exists. Please enter a unique combination.", "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert the new golf car into the database (locally)
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO golfcarschedule (GolfCarNumber, TripNumber, Time, Destination, AvailableSeats) VALUES (?, ?, ?, ?, ?)")) {

            // Insert the new golf car into the database
            pstmt.setInt(1, carNumber);
            pstmt.setInt(2, trips);
            pstmt.setString(3, time);
            pstmt.setString(4, destination);
            pstmt.setInt(5, availableSeats);
            pstmt.executeUpdate();  // Add the car to the database
            // Add the new golf car to the table in the UI
            model.addRow(new Object[]{carNumber, trips, time, destination, availableSeats});

        } catch (SQLException e) {
            // Handle database insertion errors
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while adding golf car: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Attempt to notify the student (if available)
        new Thread(() -> {
            try {
                socket = new Socket("127.0.0.1", 12347); // Attempt to connect to the server
                outputNot = new PrintWriter(socket.getOutputStream(), true); // Auto-flush enabled
                sendMessageToStudent("New Trip has been added " + "in Golf Car No. " + carNumber);
            } catch (IOException ex) {
                golfCarFrame.dispose();
                showGolfCarManagement();
            }
        }).start();
    }

    //Check if the golf car that added by admin is a Duplicate or not
    private static boolean isDuplicateEntry(int carNumber, int trips) {
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM golfcarschedule WHERE GolfCarNumber = ? AND TripNumber = ?")) {
            pstmt.setInt(1, carNumber);
            pstmt.setInt(2, trips);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Duplicate entry found
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while checking for duplicates: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false; // No duplicate entry found
    }

    // Deletes a golf car from the table and database
    private static void deleteGolfCar(DefaultTableModel model, int rowIndex, String carNumber, String tripNumber) {
        // Check if there are any active reservations for this golf car and trip
        if (hasActiveReservations(carNumber, tripNumber)) {
            JOptionPane.showMessageDialog(null, "Cannot delete this golf car because there are active reservations for this trip.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM golfcarschedule WHERE GolfCarNumber = ? AND TripNumber = ?")) {

            pstmt.setString(1, carNumber);
            pstmt.setString(2, tripNumber);
            pstmt.executeUpdate();

            // Remove the row from the model (table)
            model.removeRow(rowIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while deleting golf car: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Ensure that the golf car has Active Reservations , if NOT i can delete the trip 
    private static boolean hasActiveReservations(String carNumber, String tripNumber) {
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations WHERE GolfCarNumber = ? AND TripNumber = ?")) {

            pstmt.setString(1, carNumber);
            pstmt.setString(2, tripNumber);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // There are active reservations for this golf car and trip
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while checking for active reservations: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false; // No active reservations found
    }

    //Apply network concept, to send a notification when add a new trip
    private static void sendMessageToStudent(String message) {
        if (outputNot != null) {
            outputNot.println(message);
        }
    }

}