import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogIn {

    static void createInputFrame() {

        // Create a new JFrame for the login and modify the Properties
        JFrame inputFrame = new JFrame("Log in");
        inputFrame.setLocationRelativeTo(null);
        inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inputFrame.setSize(500, 400);
        inputFrame.setLayout(new BorderLayout());
        inputFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Load the logo image and create a JLabel to display it
        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);
        inputFrame.add(logoLabel, BorderLayout.NORTH);

        // Create a panel with a grid layout for input fields
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.setBackground(Main.BACKGROUND_COLOR);

        // Create text fields for Student info
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        idField.setForeground(Main.TEXT_COLOR);
        nameField.setForeground(Main.TEXT_COLOR);
        emailField.setForeground(Main.TEXT_COLOR);
        phoneField.setForeground(Main.TEXT_COLOR);

        // Create a button for submitting the login information
        JButton submitButton = new JButton("Login");
        submitButton.setBackground(Main.BUTTON_COLOR);
        submitButton.setForeground(Main.TEXT_COLOR);

        // Add labels and input fields to the input panel
        inputPanel.add(new JLabel("Enter Student ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Enter Student Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Enter Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Enter Phone Number:"));
        inputPanel.add(phoneField);

        // Create another panel to hold the submit button
        JPanel submitPanel = new JPanel();
        submitPanel.setBackground(Main.BACKGROUND_COLOR);

        // Add Button to the panel
        submitPanel.add(submitButton);

        // Add panels to the frame
        inputFrame.add(inputPanel, BorderLayout.CENTER);
        inputFrame.add(submitPanel, BorderLayout.PAGE_END);

        // Add an action listener to the submit button
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String studentId = idField.getText();
                String studentName = nameField.getText();
                String email = emailField.getText();
                String phoneNumber = phoneField.getText();

                try {
                    // Validate inputs first
                    List<String> errors = validateInputs(studentId, studentName, email, phoneNumber);

                    // If there are any errors, show them and prevent further action
                    if (!errors.isEmpty()) {
                        String errorMessage = String.join("\n", errors);
                        JOptionPane.showMessageDialog(inputFrame, errorMessage, "Input Errors", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // If validation passes, save the student data and proceed
                    saveStudentData(studentId, studentName, email, phoneNumber);
                    // Close the input frame and open the menu frame if everything is valid
                    inputFrame.dispose();

                    MenuView.createMenuView();

                } catch (Exception ex) {
                    // Catch any unexpected errors and show a generic error message
                    JOptionPane.showMessageDialog(inputFrame, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        inputFrame.setVisible(true);
    }

    private static List<String> validateInputs(String studentId, String studentName, String email, String phoneNumber) {
        List<String> errorMessages = new ArrayList<>();

        // Validate Student ID (must be a 7-digit integer)
        if (!studentId.matches("\\d{7}")) {
            errorMessages.add("Student ID must be exactly 7 digits.");
        }

        // Validate Name (must only contain letters and spaces)
        if (!studentName.matches("[a-zA-Z ]+")) {
            errorMessages.add("Name must contain only letters and spaces.");
        }

        // Validate Email (basic regex for email format)
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            errorMessages.add("Invalid email address. Please ensure the email:\n"
                    + "- Contains only letters, numbers, underscores (_), plus (+), ampersands (&), asterisks (*), hyphens (-), and dots (.) before the '@'.\n"
                    + "- Has a valid domain name with a '.' separating sections.\n"
                    + "- Ends with a valid domain suffix between 2 and 7 characters (e.g., '.com', '.org').");
        }

        // Validate Phone Number (must be exactly 10 digits)
        if (!phoneNumber.matches("\\d{10}")) {
            errorMessages.add("Phone number must be exactly 10 digits.");
        }

        return errorMessages;
    }

    private static void saveStudentData(String studentId, String studentName, String email, String phoneNumber) {
        int c = 0;
        // Ensure MySQL JDBC driver is loaded
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            // If the JDBC driver is not found, print the stack trace and show an error message to the user
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "JDBC Driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // query to insert the student data into the "student" table
        String sql = "INSERT INTO student (STUDENT_ID, NAME, EMAIL, PHONE_NUMBER) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the parameters in the query
            Main.studentId = Integer.parseInt(studentId);// Convert studentId to an integer
            pstmt.setInt(1, Main.studentId);
            pstmt.setString(2, studentName);
            pstmt.setString(3, email);
            pstmt.setString(4, phoneNumber);

            // Execute the SQL update to insert the data into the database
            pstmt.executeUpdate();
            // Show a success message to the user upon successful insertion
            JOptionPane.showMessageDialog(null, "Welcome, " + studentName + "!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            // If an SQL error occurs (database connection issues, query errors)
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving user information: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
