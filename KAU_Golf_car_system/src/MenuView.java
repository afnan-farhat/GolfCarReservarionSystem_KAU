import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.LineBorder;

public class MenuView {

    // Method to create and display the menu view
    public static void createMenuView() {
        // Create a new JFrame for the Menu and modify the Properties
        JFrame menuFrame = new JFrame("Menu");
        menuFrame.setSize(500, 500);
        menuFrame.setLayout(new BorderLayout());

        // Load and display the logo at the top of the menu
        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);
        menuFrame.add(logoLabel, BorderLayout.NORTH);

        // Set default close operation and center the frame
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Create styled buttons for various menu options
        JButton makeReservationButton = createStyledSquareButton("Make a Reservation");
        JButton cancelReservationButton = createStyledSquareButton("Cancel My Reservation");
        JButton allReservationsButton = createStyledSquareButton("All My Reservations");
        JButton contactUsButton = createStyledSquareButton("Contact Us");
        JButton logOutButton = createStyledSquareButton("Log Out");

        // Add action listener to the buttons
        makeReservationButton.addActionListener(e -> {
            menuFrame.dispose();
            TableView.createTableView();
        });
        cancelReservationButton.addActionListener(e -> {
            menuFrame.dispose();
            CancelReservationView.createCancelView();
        });
        allReservationsButton.addActionListener(e -> {
            menuFrame.dispose();
            try {
                AllReservationView.createAllReservationView();
            } catch (SQLException ex) {
                //errors that occur when fetching all reservations
                Logger.getLogger(MenuView.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        contactUsButton.addActionListener(e -> {
            ContactUs.startClientWithChatUI();
        });

        logOutButton.addActionListener(e -> {
            menuFrame.dispose();
            StudentView_login.createInputFrame();
        });

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));

        // Add buttons to the button panel
        buttonPanel.setBackground(Main.BACKGROUND_COLOR);
        buttonPanel.add(makeReservationButton);
        buttonPanel.add(cancelReservationButton);
        buttonPanel.add(allReservationsButton);
        buttonPanel.add(contactUsButton);
        buttonPanel.add(logOutButton);

        // Add the button panel to the center of the frame
        menuFrame.add(buttonPanel, BorderLayout.CENTER);
        menuFrame.setVisible(true);
    }

    // Method to create a styled square button
    public static JButton createStyledSquareButton(String text) {
        // Create a JButton with specified text
        JButton button = new JButton(text);
        button.setBackground(Main.BUTTON_COLOR);
        button.setForeground(Main.TEXT_COLOR);
        button.setBorder(new LineBorder(Main.TEXT_COLOR, 2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(100, 100));
        return button;
    }
}
