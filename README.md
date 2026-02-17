# BetterTown 🏙️

> **A smart city management platform empowering citizens and administrators to build better communities.**

BetterTown is a robust Java Swing application that streamlines the process of reporting, tracking, and resolving urban issues. Designed with a focus on usability and real-time feedback, it serves as a bridge between city officials and the community.

---

## 👥 The Team

This project was collaboratively developed by:

*   **Mihut Luca-Adrian**
*   **Grovu Bianca-Delia**

---

## 🛠️ Technologies Used

This project handles complex logic including geolocation, database management, and secure authentication, built with the following stack:

*   **Core Language**: Java 21 (JDK 21)
*   **GUI Framework**: Java Swing (Custom UI components and layout management)
*   **Build Tool**: Apache Maven
*   **Database**: MySQL (Complex schema with relational data integrity)
*   **Mapping & Geolocation**:
    *   **JXMapViewer2**: Interactive map visualization with custom tile factories.
    *   **OpenStreetMap**: Map tile data source (HTTPS secured).
    *   **Nominatim API**: Reverse geocoding for converting coordinates to human-readable addresses.
*   **Security**:
    *   **jBCrypt**: Industrial-grade password hashing and salt generation.
*   **Data Parsing**: `org.json` for handling external API responses.

---

## ✨ Key Features

### 👤 For Citizens (User Role)
*   **Interactive Map Reporting**: Simply click on the map to pinpoint an issue location. The app automatically fetches the address and city.
*   **Visual Evidence**: Attach photos to reports for clarity.
*   **Real-time Feed**: Browse a feed of reported issues in the community.
*   **Live Alerts**: Receive notifications from administrators when issue statuses change.

### 🛡️ For Administrators (Admin Role)
*   **Issue Management Dashboard**: Track reported issues, view details, and prioritize fixes.
*   **Status Workflow**: Update issues from "Reported" to "In Progress" or "Resolved".
*   **Global Alerts**: Broadcast urgent alerts to all users.
*   **User Management**: Monitor user activity and manage accounts.

---

## 🚀 Getting Started

### Prerequisites
*   Java Development Kit (JDK) 21+
*   Maven
*   MySQL Server

### Installation & Setup

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/lukettoOoO/BetterTown.git
    cd BetterTown
    ```

2.  **Database Configuration**
    *   Create a MySQL database named `BetterTown`.
    *   Run the provided SQL script to set up the schema and default data:
        ```bash
        mysql -u root -p < database.sql
        ```
    *   Configure your database connection in `src/main/resources/db.properties`:
        ```properties
        db.url=jdbc:mysql://localhost:3306/BetterTown
        db.user=YOUR_USERNAME
        db.password=YOUR_PASSWORD
        ```

3.  **Run the Application**
    We have provided a convenience script to build and launch the app in one go:
    ```bash
    ./run.sh
    ```
    *(Alternatively, use `mvn clean package` and run the generated JAR in `target/`)*

### Default Login Credentials

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@bettertown.com` | `admin123` |
| **User** | *(Register a new account via the GUI)* | *(User defined)* |

---

## 📸 Project Highlights
*   **Security First**: All passwords are securely hashed using BCrypt before storage.
*   **Robust Error Handling**: Network requests to geocoding APIs handle timeouts and errors gracefully to prevent crashes.
*   **External Configuration**: Database credentials are decoupled from the source code for security and portability.


