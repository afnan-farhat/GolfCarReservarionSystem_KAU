import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Server {

    // Socket-related variables
    private static ServerSocket serverSocket;  // Server socket for accepting client connections
    private static Socket clientSocket; // Socket for individual client connection
    private static BufferedReader input; // Reader to receive data from the client
    private static PrintWriter output; // Writer to send data to the client
    private static int port_chat = 12345; // Port number for the server
    private static JTextArea conversationArea; // Text area to display messages in the server's GUI

    public static void ServerChat() {
        // Set up the GUI 
        JFrame replyFrame = new JFrame("Server Chat");
        replyFrame.setSize(400, 400);
        replyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        replyFrame.setLocationRelativeTo(null);
        replyFrame.getContentPane().setBackground(Main.BACKGROUND_COLOR);

        // Set up conversation area (chat window)
        conversationArea = new JTextArea(10, 30);
        conversationArea.setEditable(false);
        conversationArea.setBackground(Main.BACKGROUND_COLOR);
        conversationArea.setForeground(Main.TEXT_COLOR);
        JScrollPane conversationScrollPane = new JScrollPane(conversationArea);

        // Reply field and send button
        JTextField replyField = new JTextField(30);
        replyField.setForeground(Main.TEXT_COLOR);

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(Main.BUTTON_COLOR);
        sendButton.setForeground(Main.TEXT_COLOR);

        // Send button action (send message to client)
        sendButton.addActionListener(e -> {
            String replyText = replyField.getText();
            conversationArea.append("You: " + replyText + "\n");
            replyField.setText("");  // Clear the reply field

            // Send reply to client (on the same thread)
            sendMessageToClient(replyText);
        });

        // Set up panel for reply field and send button
        JPanel replyPanel = new JPanel(new BorderLayout());
        replyPanel.setBackground(Main.BACKGROUND_COLOR);
        replyPanel.add(replyField, BorderLayout.CENTER);
        replyPanel.add(sendButton, BorderLayout.EAST);

        replyFrame.add(conversationScrollPane, BorderLayout.CENTER);
        replyFrame.add(replyPanel, BorderLayout.SOUTH);
        replyFrame.setVisible(true);

        // Start the server socket and wait for client connections in a separate thread
        new Thread(() -> {
            try {
                // Start the server socket on port 12345
                serverSocket = new ServerSocket(port_chat);
                System.out.println("Server started. Waiting for client...");

                // Wait for a client to connect
                while (true) {
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");

                    // Create input and output streams for communication with the client
                    input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    output = new PrintWriter(clientSocket.getOutputStream(), true);

                    // Send a greeting message to the client
                    sendMessageToClient("Hello, please note that if you leave the page, the conversation will be lost. If you wish to end the conversation, simply type 'EXIT'.");

                    // Handle the client communication in a new thread
                    new ClientHandler(clientSocket, input, output).start();
                }
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            } finally {
                closeServer();
            }
        }).start();
    }

    public static void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send messages to the client
    private static void sendMessageToClient(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    // Method to append messages to the conversation area
    public static void appendToConversation(String message) {
        conversationArea.append(message + "\n");
    }

    // Client handler to handle communication with the client
    static class ClientHandler extends Thread {

        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(Socket socket, BufferedReader input, PrintWriter output) {
            this.clientSocket = socket;
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                String clientMessage;
                // Continuously read messages from the client
                while ((clientMessage = input.readLine()) != null) {
                    // Append client message to the chat window
                    final String message = "Client: " + clientMessage;
                    SwingUtilities.invokeLater(() -> conversationArea.append(message + "\n"));

                    // If the client sends "EXIT", break the loop and end the conversation
                    if (clientMessage.equalsIgnoreCase("EXIT")) {
                        System.out.println("Client exited");
                        sendMessageToClient("The conversation has ended. Goodbye!");
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from client: " + e.getMessage());
            } finally {
                closeResources();
            }
        }

        // Method to close resources for this client
        private void closeResources() {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
