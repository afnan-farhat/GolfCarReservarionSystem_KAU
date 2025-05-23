# Golf Car Reservation System at King Abdulaziz University

This system is designed to streamline the reservation process for golf cars within King Abdulaziz University, ensuring an efficient and user-friendly experience for students and administrators alike. Below are the key features and concepts implemented in this system.

---

## Key Concepts Applied
1. **Networking:**
   - Real-time communication through chat functionality and notifications.
2. **Threading:**
   - Handling multiple simultaneous operations, such as reservation requests and chat messages.
3. **Database:**
   - SQL database integration to store and manage data related to users, reservations, and golf cars.
4. **Input/Output (I/O):**
   - Generating detailed reports of all reservations.

---

## System Features

### Student Interface (Main)
The student interface allows students to:
- **Login/Logout:**
   - Secure access to the system with personalized accounts.
- **Reserve a Golf Car:**
   - Book a golf car for their specific needs and schedule.
- **Cancel Reservations:**
   - Manage their reservations with the ability to cancel as needed.
- **Contact Admin:**
   - Communicate directly with the admin via chat for support or inquiries.

---

### Admin Interface
The admin interface enables administrators to:
- **Chat with Students:**
   - Respond to student inquiries and provide assistance in real-time.
- **Manage Golf Cars:**
   - Add or remove golf cars from the system to keep the inventory up-to-date.

---

## System Components
1. **Database Creation:**
   - A dedicated SQL Workbench class is used to create and manage the database schema for users, reservations, and golf cars.
2. **Report Generation:**
   - A `Report` class is implemented to generate detailed reservation reports, including:
     - Reservation ID
     - Student details
     - Reservation time and status
     - Golf car availability

---

## Technology Stack
- **Frontend:** Designed for user-friendly interactions for both students and admins.
- **Backend:** Robust server-side logic to handle reservation requests, chats, and system updates.
- **Database:** SQL Workbench for structured data storage and retrieval.

---

This system ensures efficient management of golf car reservations and enhances communication between students and administrators. The inclusion of real-time chat, database integration, and detailed reporting makes it a comprehensive solution for King Abdulaziz University.
