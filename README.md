# [Charity Hub](https://github.com/TechMentors-LLC/charity_hub) &middot; [![GitHub license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/TechMentors-LLC/charity_hub/blob/main/LICENSE) [![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot) [![CI](https://github.com/TechMentors-LLC/charity_hub/actions/workflows/test.yml/badge.svg)](https://github.com/TechMentors-LLC/charity_hub/actions)

Charity Hub is a robust Spring Boot platform for managing charitable cases and contributions.

* **Secure:** Built with Spring Security and JWT authentication, ensuring secure access to sensitive charitable data and user information.
* **Scalable:** Leverages Spring Modulith architecture and MongoDB for efficient data management and system scalability.
* **Real-time:** Integrates Firebase Cloud Messaging for instant notifications across web and mobile platforms.

[Learn how to use Charity Hub](#documentation)

## Installation

Charity Hub requires the following prerequisites:
* Java 21 or higher
* MongoDB
* Firebase Admin credentials

Get started in minutes:
```bash
# Clone the repository
git clone https://github.com/TechMentors-LLC/charity_hub.git

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Firebase Setup Instructions
To run this application, you need to set up a Firebase account and obtain the `adminSdk.json` file. Follow the steps below to do so:

1. **Create a Firebase Account and Project:**
- Go to the [Firebase Console](https://console.firebase.google.com/).
- Click on "Add project" and follow the on-screen instructions to create a new Firebase project.

2. **Add Firebase to Your Application:**
- In the Firebase project dashboard, click on the gear icon next to "Project Overview" and select "Project settings".
- Navigate to the "Service accounts" tab.
- Click on "Generate new private key" under the "Firebase Admin SDK" section.
- A JSON file will be downloaded to your computer. This file contains the necessary credentials for your Firebase project.

3. **Move the `adminSdk.json` File:**
- Rename the downloaded JSON file to `adminSdk.json`.
- Create a folder named `cert` inside the `resources` folder of your project.
- Move the `adminSdk.json` file into the `cert` folder.

## Documentation

The project is organized into several core modules:

* **Accounts Module**
  * Account authentication
  * Role-based access control
  * Profile management

* **Cases Module**
  * Manage charitable cases
  * Track contributions/donations
  * Handle case status updates

* **Ledger Module**
  * View and manage user contributions
  * Fetch detailed ledger information for users
  * Provide summary of pledged, paid, and confirmed contributions
  * Manage hierarchical relationships between members
  * Log events related to account creation and membership

* **Notifications Module**
  * Real-time push notifications
  * Email notifications
  * Event tracking

## Observability

Charity Hub uses **SigNoz** as the observability backend for Metrics, Traces, and Logs. The application is pre-configured to send telemetry data to SigNoz running on `localhost`.

### 1. Install SigNoz
We recommend using the official Docker setup:

```bash
# Clone the SigNoz repository
git clone -b main https://github.com/SigNoz/signoz.git
cd signoz/deploy/

# Run the installation script
./install.sh
```

Once installed, the SigNoz UI will be available at http://localhost:8080.

### 2. Connect the Application
The application is already configured to send data to the default SigNoz OTLP endpoint (`http://localhost:4318`). Simply run the application:

```bash
./gradlew bootRun
```

Generate some traffic, and you will see the `charity-hub` service in the SigNoz dashboard with correlated Metrics, Traces, and Logs.

For more details on using SigNoz, refer to the official documentation.

## Technology Stack

Charity Hub is built with modern technologies:

```json
{
  "backend": {
    "language": "Java 21",
    "framework": "Spring Boot 3.3.5",
    "database": "MongoDB",
    "security": ["Spring Security", "JWT"],
    "messaging": "Firebase Cloud Messaging",
    "observability": ["Micrometer", "Prometheus", "OpenTelemetry"]
  }
}
```

## Contributing

We welcome contributions! Here's how you can help:

* 🐛 Report bugs by opening an issue
* 💡 Propose new features
* 📖 Improve documentation
* 🔍 Review pull requests

### Getting Started with Development

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Charity Hub is [Apache 2.0 licensed](./LICENSE).
