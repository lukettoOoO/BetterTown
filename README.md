# BetterTown

BetterTown is a Java Swing application designed to help citizens and administrators manage town issues, alerts, and feedback. It features distinct interfaces for Users and Admins, integrated mapping with OpenStreetMap, and a robust issue reporting system.

## Features

### User Features
- **Report Issues**: Submit issues with descriptions, photos, and location coordinates via the map.
- **View Map**: Browse reported issues on an interactive map.
- **Alerts**: Receive real-time alerts from administrators about issue status changes.
- **Feed**: View a list of all reported issues.

### Admin Features
- **Manage Issues**: View, update status (In Progress, Resolved), and delete reported issues.
- **Map Overview**: See all issues on the map with their current status.
- **Send Alerts**: Broadcast alerts to users regarding city updates or specific issues.
- **User Management**: View user accounts.

## Prerequisites

- **Java**: JDK 21 or higher.
- **Maven**: For building the project.
- **MySQL**: For the database backend.

## Setup

### 1. Database Setup
1.  Ensure MySQL is running.
2.  Create a database named `BetterTown`.
3.  Import the schema and default data using the provided `database.sql` script:

    ```bash
    mysql -u root -p < database.sql
    ```

### 2. Application Configuration
1.  Navigate to `src/main/resources/`.
2.  Create or edit `db.properties` with your database credentials:

    ```properties
    db.url=jdbc:mysql://localhost:3306/BetterTown
    db.user=your_username
    db.password=your_password
    ```

    *Note: The default `db.properties` is set to `root` with no password, common for some local development environments.*

## Running the Application

### Automatic (Recommended)
Use the provided shell script to build and run the application automatically:

```bash
./run.sh
```

### Manual
1.  **Build**:
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Run**:
    ```bash
    java -jar target/BetterTown-1.0-SNAPSHOT-exec.jar
    ```

## Default Credentials

### Admin Login
- **Email**: `admin@bettertown.com`
- **Password**: `admin123`
- **Role**: Select **Admin**

### User Login
You can register a new user via the "Register" button on the login screen.

## Troubleshooting

- **Map Tiles Not Loading**: Ensure you have an active internet connection. The application uses HTTPS to fetch tiles from OpenStreetMap.
- **Database Connection Error**: Verify your MySQL server is running and the credentials in `src/main/resources/db.properties` are correct.
- **Geolocation Errors**: If the map click doesn't fetch an address, check your internet connection. the application uses the Nominatim API which requires internet access.
