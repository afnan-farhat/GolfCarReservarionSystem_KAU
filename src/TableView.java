
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TableView {

    private static ServerSocket serverSocket;
    public static boolean serverRunning = false;  // Flag to check if the server is running
    public static DefaultTableModel model;
    public static JTable table;

    // Method to create and display the table view
    public static void createTableView() {
        // Create a new JFrame for the table view and modify the Properties
        JFrame tableFrame = new JFrame("Golf Car Table");
        tableFrame.setLocationRelativeTo(null);
        tableFrame.setSize(550, 500);
        tableFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Start server connection in a separate thread
        new Thread(() -> {
            connectAsServer(); // Call the method to start the server
        }).start();

        // Load the logo image and create a JLabel to display it
        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);

        // Add the logo label to the top of the frame
        tableFrame.add(logoLabel, BorderLayout.NORTH);

        // Sample Data for the table
        String[] columnNames = {"Golf Car Number", "Trip Number", "Time", "Destination", "Available seats"};

        // Create a DefaultTableModel with the data and column names
        model = new DefaultTableModel(columnNames, 0);

        // Create a JTable using the model
        table = new JTable(model);
        loadGolfCarData();

        table.setBackground(Main.BACKGROUND_COLOR);
        table.setForeground(Main.TEXT_COLOR);

        // Create a JScrollPane to make the table scrollable
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons and modify the Properties
        JButton button = new JButton("Reserve this Trip");
        button.setBackground(Main.BUTTON_COLOR);
        button.setForeground(Main.TEXT_COLOR);

        button.addActionListener(e -> {
            try {
                showSelectedRowData();
            } catch (SQLException ex) {
                //error in show the selected row
                Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
            }
            tableFrame.dispose();
        });

        // Create a "Back to Menu" button and set its properties
        JButton backButton = new JButton("Back to Menu");
        backButton.setBackground(Main.BUTTON_COLOR);
        backButton.setForeground(Main.TEXT_COLOR);
        backButton.addActionListener(e -> {
            stopServer();
            tableFrame.dispose();
            MenuView.createMenuView();
        });

        // Create a panel to hold the buttons
        JPanel panel = new JPanel();
        panel.setBackground(Main.BACKGROUND_COLOR);
        panel.add(backButton);
        panel.add(button);

        // Add the scroll pane containing the table to the center of the frame
        tableFrame.add(scrollPane, BorderLayout.CENTER);
        // Add the panel with buttons to the bottom of the frame
        tableFrame.add(panel, BorderLayout.SOUTH);

        tableFrame.setVisible(true);

        // Periodically refresh seat availability in the background
        new Thread(() -> {
            while (true) {
                try {
                    // Refresh available seats by checking the database periodically
                    refreshSeatAvailability();
                    Thread.sleep(5000);  // Refresh every 2 seconds
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (SQLException ex) {
                    Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    // Periodically check and update the seat availability
    private static void refreshSeatAvailability() throws SQLException {
        // Loop through each row and update the available seats
        for (int row = 0; row < model.getRowCount(); row++) {
            try {
                int tripNum = (int) model.getValueAt(row, 1);
                int availableSeats = getAvailableSeats(tripNum);
                model.setValueAt(availableSeats, row, 4);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("ArrayIndexOutOfBoundsException");
            }
        }
    }
        // Method to fetch available seats from the database using a trip number
    private static int getAvailableSeats(int tripNumber) throws SQLException {
        ResultSet rs = null;
        String query = "SELECT AvailableSeats FROM GolfCarSchedule WHERE TripNumber = ?";
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tripNumber);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("AvailableSeats");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    // Method to load golf car data into the table
    private static void loadGolfCarData() {
        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM golfcarschedule")) {

            // Loop through the result set and add rows to the table model
            while (rs.next()) {
                int carNumber = rs.getInt("GolfCarNumber");
                int trips = rs.getInt("TripNumber");
                String time = rs.getString("Time");
                String destination = rs.getString("Destination");
                int availableSeats = rs.getInt("AvailableSeats");
                model.addRow(new Object[]{carNumber, trips, time, destination, availableSeats});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to handle the selected row and show seat selection
    public static void showSelectedRowData() throws SQLException {
        // Get the index of the selected row
        int selectedRow = table.getSelectedRow();

        if (selectedRow != -1) {
            stopServer();
            int availableSeats = Integer.parseInt(model.getValueAt(selectedRow, 4).toString());
            Reservation_Proccess.createSeatSelectionFrame(model, selectedRow, availableSeats);
        } else {
            createTableView();  // If no row selected, refresh the table view
        }
    }

    // Server connection handling
    public static void connectAsServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12347); // Start the server on port 12347
                serverRunning = true;

                System.out.println("Server started and waiting for clients...");
                // Accept client connections in a loop
                while (serverRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept(); // Accept a client connection
                        handleClient(clientSocket); // Handle the client connection in a new thread
                    } catch (SocketException e) {
                        if (serverRunning) { // Log the error only if the server is still running
                            System.err.println("Socket exception: " + e.getMessage());
                        } else {
                            stopServer();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stopServer();
            }
        }).start();
    }

    // Handle a single client's communication
    private static void handleClient(Socket clientSocket) {
        new Thread(() -> {
            System.out.println("New client connected: " + clientSocket.getInetAddress());

            try {
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Read messages from the client and show them in a dialog
                String clientMessage;
                while ((clientMessage = clientIn.readLine()) != null) { // Keep reading messages
                    final String alertMessage = clientMessage; // Use a final variable inside the lambda
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, alertMessage, "Notification", JOptionPane.INFORMATION_MESSAGE);
                        refreshGolfCarData();
                    });
                }

                //System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close(); // Close the client socket when done
                    //System.out.println("Client socket closed.");
                } catch (IOException ex) {
                    Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    // Refresh the golf car data in the table
    public static void refreshGolfCarData() {
        model.setRowCount(0); // This removes all rows from the table
        loadGolfCarData(); // Load the latest data from the database
    }

    // Stop the server
    private static void stopServer() {
        serverRunning = false; // Set the flag to false to stop accepting new clients
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the server socket
                System.out.println("Server stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
