import java.awt.Color;

public class Main {

    public static final Color TEXT_COLOR = new Color(85, 136, 59);
    public static final Color BUTTON_COLOR = new Color(193, 232, 153);
    public static final Color BACKGROUND_COLOR = new Color(230, 240, 220);
    public static String selectedTripInfo;
    public static int random_port;
    public static int studentId;

    // Database connection details
    //(1) Set the connection URL for the existing database
    public static final String url = "jdbc:mysql://localhost:3306/KauGolfCar_system";
    //(2) Set the username for the database
    public static final String username = "root";
    //(3) Set the password for the database
    public static final String password = "Af@2105973";

    public static void main(String[] args) {
        // Create a new thread to run the log-in window in parallel
        Thread firstWindowThread = new Thread(() -> {
            //Start the program 
            LogIn.createInputFrame();
//            MenuView.createMenuView();
        });
        // Start thread
        firstWindowThread.start();

    }
}
