
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ContactUs {

    // Socket-related variables
    private static Socket socket;
    private static BufferedReader input;
    private static PrintWriter output;
    private static JTextArea chatArea;

    public static void startClientWithChatUI() {
        // Create a new frame for the chat and set the color of background
        JFrame replyFrame = new JFrame("Client Chat");
        replyFrame.setSize(800, 400);
        replyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        replyFrame.setLocationRelativeTo(null);
        replyFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR); 

        // Create a text area to display chat messages and set the color of background and text
        chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false); // Make chat area non-editable
        chatArea.setBackground(Main.BACKGROUND_COLOR); 
        chatArea.setForeground(Main.TEXT_COLOR); 
        JScrollPane chatAreaScrollPane = new JScrollPane(chatArea);

        // Reply field and send button
        JTextField replyField = new JTextField(30);
        replyField.setForeground(Main.TEXT_COLOR);

        // Create a "Send" button and set the color of background and text
        JButton sendButton = new JButton("Send");
        sendButton.setBackground(Main.BUTTON_COLOR);
        sendButton.setForeground(Main.TEXT_COLOR);

        // Add an action listener to handle the button click
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the text from the reply field
                String message = replyField.getText();
                // Add the message to the chat area
                chatArea.append("You: " + message + "\n");
                // Clear the reply field after sending the message
                replyField.setText("");

                // Send reply to server (on the same thread)
                sendMessageToServer(message);
            }
        });

        // Create a panel to hold the reply field and send button
        JPanel replyPanel = new JPanel(new BorderLayout());
        replyPanel.setBackground(Main.BACKGROUND_COLOR);
        replyPanel.add(replyField, BorderLayout.CENTER); // Add the reply field in the center
        replyPanel.add(sendButton, BorderLayout.EAST); // Add the send button to the right

        // Add the chat area and reply panel to the frame
        replyFrame.add(chatAreaScrollPane, BorderLayout.CENTER); // Add the chat area in the center
        replyFrame.add(replyPanel, BorderLayout.SOUTH); // Add the reply panel at the bottom
        replyFrame.setVisible(true);

        // Start the communication with the server
        new Thread(() -> {
            try {
                // Create Socket object and connect to the server
                socket = new Socket("127.0.0.1", 12345);
                System.out.println("Connected to server.");

                // Create input stream obj (BufferedReader)
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Create output stream obj (PrintWriter)
                output = new PrintWriter(socket.getOutputStream(), true);

                // Read the initial message from the server
                String serverMessage = input.readLine();
                SwingUtilities.invokeLater(() -> chatArea.append("Server: " + serverMessage + "\n"));

                // Read messages from the server and update the conversation area
                String serverReply;
                while ((serverReply = input.readLine()) != null) {
                    // If the server sends "EXIT", close the connection
                    if (serverReply.equalsIgnoreCase("EXIT")) {
                        break;
                    }

                    // Append server message to the chat window
                    final String message = "Server: " + serverReply;
                    SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("You exit.");
                replyFrame.dispose();
                closeResources();

            }
        }).start();  // Start the client communication in a background thread
    }

    // Method to send messages to the server
    private static void sendMessageToServer(String message) {
        if (output != null) {
            output.println(message);  // Send the message to the server
        }
    }

    // Method to close resources
    private static void closeResources() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}