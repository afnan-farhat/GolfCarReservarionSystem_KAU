
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Main {

    // Define constant colors for consistent theme across the application
    public static final Color TEXT_COLOR = new Color(85, 136, 59);
    public static final Color BUTTON_COLOR = new Color(193, 232, 153);
    public static final Color BACKGROUND_COLOR = new Color(230, 240, 220);
    public static String selectedTripInfo;
    public static int random_port;
    public static int studentId;

    // Declare buttons
    private static JButton admin_interface;
    private static JButton student_interface;
    private static JButton create_database;
    private static JButton Daily_report;

    // Database connection details
    //(1) Set the connection URL for the existing database
    public static final String url = "jdbc:mysql://localhost:3306/KauGolfCar_system";
    //(2) Set the username for the database
    public static final String username = "Your_userName";
    //(3) Set the password for the database
    public static final String password = "Your_password";

    public static void main(String[] args) {
        // Create a new thread to run the log-in window in parallel
        Thread firstWindowThread = new Thread(() -> {
            //Start the program 
            interfaces();
        });
        // Start thread
        firstWindowThread.start();

    }

    public static void interfaces() {
        // Create the main admin menu window
        JFrame menuFrame = new JFrame("KAU Golf Car System");
        menuFrame.setSize(500, 350);
        // Load and display the logo at the top of the menu
        ImageIcon logo = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logo);
        menuFrame.add(logoLabel, BorderLayout.NORTH);

        // Set default close operation and center the frame
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Create buttons for golf car management and chatting 
        admin_interface = new JButton("Admin");
        student_interface = new JButton("Student");
        create_database = new JButton("Create Database");
        Daily_report = new JButton("Daily Report");

        // Apply consistent button colors
        admin_interface.setBackground(BUTTON_COLOR);
        admin_interface.setForeground(TEXT_COLOR);
        student_interface.setBackground(BUTTON_COLOR);
        student_interface.setForeground(TEXT_COLOR);
        create_database.setBackground(BUTTON_COLOR);
        create_database.setForeground(TEXT_COLOR);
        Daily_report.setBackground(BUTTON_COLOR);
        Daily_report.setForeground(TEXT_COLOR);

        // Set action listeners for the buttons
        admin_interface.addActionListener(e -> {
            // Admin interface 
            AdminView.AdminView();

        });

        student_interface.addActionListener(e -> {
            // Student interface 
            StudentView_login.createInputFrame();
        });

        create_database.addActionListener(e -> {
            // Create the Database
            CreateDatabase.CreateDatabase();
        });

        Daily_report.addActionListener(e -> {
            // Prepare the dailt report of reservation 
            ReservationReport.generateReport();
        });

        // Set up the layout for the buttons
        JPanel panel = new JPanel(); 
        panel.setLayout(new GridLayout(4, 1));

        panel.setBackground(BACKGROUND_COLOR);
        panel.add(admin_interface);
        panel.add(student_interface);
        panel.add(create_database);
        panel.add(Daily_report);

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
}
