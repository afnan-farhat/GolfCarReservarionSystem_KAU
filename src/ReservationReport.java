import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReservationReport {

    // Generates a detailed reservation report and writes it to a text file
    public static void generateReport() {
        String reportFilePath = "Report.txt";
        try (
            // Establish a connection to the database
            Connection conn = DriverManager.getConnection(Main.url, Main.username, Main.password);
            
            // Prepare SQL query to retrieve reservation and schedule details
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT r.StudentID, s.NAME AS UserName, r.GolfCarNumber, r.TripNumber, r.Time, " +
                "g.Destination, r.ReservedSeats, g.AvailableSeats " +
                "FROM reservations r " +
                "JOIN golfcarschedule g ON r.GolfCarNumber = g.GolfCarNumber AND r.TripNumber = g.TripNumber " +
                "JOIN student s ON r.StudentID = s.STUDENT_ID");
            
            // Execute the query and create a writer to generate the report file
            ResultSet rs = stmt.executeQuery();
            BufferedWriter writer = new BufferedWriter(new FileWriter(reportFilePath))
        ) {
            // Write the report header
            writer.write("Reservation Report:\n");
            writer.write("ID\tUser\tDate\tTime\tCarID\tStatus\tDestination\tSeats Reserved\tTotal Seats\n");
            writer.write("----------------------------------------------------------------------------------------------------\n");

            // Initialize maps to store aggregated data for analysis
            Map<String, Integer> reservationCountByUser = new HashMap<>();
            Map<String, Integer> reservationCountByDestination = new HashMap<>();
            Map<String, Integer> reservationCountByTimeSlot = new HashMap<>();
            Map<String, Integer> carOccupancy = new HashMap<>();
            Map<String, Integer> carTotalSeats = new HashMap<>();

            int totalReservations = 0;

            // Process each row of the result set
            while (rs.next()) {
                int tripID = rs.getInt("TripNumber");
                String user = rs.getString("UserName");
                String studentID = rs.getString("StudentID");
                String time = rs.getString("Time");
                if (time == null) time = "00:00";
                int hour = Integer.parseInt(time.split(":")[0]);

                // Include only reservations made between 8 AM and 3 PM
                if (hour < 8 || hour >= 15) continue;

                int carID = rs.getInt("GolfCarNumber");
                String destination = rs.getString("Destination");
                if (destination == null) destination = "Unknown";
                int reservedSeats = rs.getInt("ReservedSeats");
                int totalSeats = rs.getInt("AvailableSeats");

                // Use the current system date for the report
                String date = LocalDate.now().toString();

                // Write a formatted reservation record to the report file
                writer.write(String.format("%d\t%s (%s)\t%s\t%s\t%d\t%s\t%s\t%d\t%d\n",
                        tripID, user, studentID, date, time, carID, "Active", destination, reservedSeats, totalSeats));

                // Update total reservations count
                totalReservations += reservedSeats;

                // Aggregate data for analysis by user, destination, and time slot
                reservationCountByUser.put(user, reservationCountByUser.getOrDefault(user, 0) + reservedSeats);
                reservationCountByDestination.put(destination, reservationCountByDestination.getOrDefault(destination, 0) + reservedSeats);
                String timeSlot = getTimeSlot(time);
                reservationCountByTimeSlot.put(timeSlot, reservationCountByTimeSlot.getOrDefault(timeSlot, 0) + reservedSeats);

                // Track car occupancy and total seats for calculation
                carOccupancy.put(String.valueOf(carID), carOccupancy.getOrDefault(String.valueOf(carID), 0) + reservedSeats);
                carTotalSeats.put(String.valueOf(carID), totalSeats);
            }

            writer.flush();

            // Write aggregated summaries to the report
            writeSummary(writer, "Summary by User", reservationCountByUser);
            writeSummary(writer, "Top Reserved Destinations", reservationCountByDestination);
            writeSummary(writer, "Reservations by Time Slot", reservationCountByTimeSlot);

            // Calculate and write car occupancy rates
            writer.write("\nGolf Car Occupancy Rate:\n");
            for (String carID : carOccupancy.keySet()) {
                int reservedSeats = carOccupancy.get(carID);
                int totalSeats = carTotalSeats.getOrDefault(carID, 1);
                int occupancyRate = (reservedSeats * 100) / totalSeats;
                writer.write("Car ID: " + carID + "\tOccupancy: " + occupancyRate + "%\n");
            }

            // Write total reservations to the report
            writer.write("\nTotal Reservations: " + totalReservations + "\n");
            System.out.println("Report generated successfully: " + reportFilePath);
        } catch (SQLException e) {
            // Handle database errors
            System.err.println("Database error: " + e.getMessage());
        } catch (IOException e) {
            // Handle file writing errors
            System.err.println("File I/O error: " + e.getMessage());
        }
    }

    // Returns a time slot category based on the hour of the reservation
    private static String getTimeSlot(String time) {
        int hour = Integer.parseInt(time.split(":")[0]);
        if (hour < 10) return "Morning (8 AM - 10 AM)";
        else if (hour < 12) return "Mid-Morning (10 AM - 12 PM)";
        else return "Afternoon (12 PM - 3 PM)";
    }

    // Writes a summary section to the report for a given title and data map
    private static void writeSummary(BufferedWriter writer, String title, Map<String, Integer> summary) throws IOException {
        writer.write("\n" + title + ":\n");
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
    }

    public static void main(String[] args) {
        // Generate the reservation report when the program runs
        generateReport();
    }
}