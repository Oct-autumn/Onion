# Onion - Project Management System

## Project Introduction

Onion is a project management system developed based on the **Spring Boot (backend)** and **Vue 3 (frontend)** technology stack. It aims to provide teams with efficient project collaboration, Kanban task management, user management, and other functions to help teams improve project management efficiency.
![Onion kanban](doc/kanban.png)

## Technology Stack

### Backend Technology
- Framework: Spring Boot
- Security: Spring Security + JWT
- Persistence Layer: Spring Data JPA
- Tools: JWT Utilities, Hash Tools, Random Tools
- Database: H2 Database (can be extended to MySQL and other relational databases)

### Frontend Technology
- Framework: Vue 3
- Routing: Vue Router
- Network Request: Axios (encapsulated in `request.js`)
- Componentization: Vue Single-File Components (.vue)

## Project Structure

### Backend (OnionServer)
```
OnionServer
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.onion.onionserver
│   │   │       ├── config          # Configuration Classes (CORS, Spring Security, Web Configuration, etc.)
│   │   │       ├── controller      # Controllers (Interface Layer, Handle HTTP Requests)
│   │   │       ├── manager         # Business Management Classes (Encapsulate Core Business Logic)
│   │   │       ├── model
│   │   │       │   ├── dao         # Data Entity Classes (Correspond to Database Tables)
│   │   │       │   ├── dto         # Data Transfer Objects (Encapsulate Interface Input and Output Parameters)
│   │   │       │   ├── enums       # Enumeration Classes (Status, Type, etc.)
│   │   │       │   └── repo        # Data Access Layer (JPA Repository)
│   │   │       ├── service         # Service Classes (e.g., Redis Service)
│   │   │       ├── util            # Utility Classes (JWT, Hash, Random Number, etc.)
│   │   │       └── OnionServerApplication  # Application Startup Class
│   │   └── resources               # Resource Files (Configuration Files, Static Resources)
│   └── test                        # Test Classes (Optional)
├── DataBase.db                     # H2 Database File (Example)
├── db_init.sql                     # Database Initialization Script
├── build.gradle                    # Build Configuration File
└── gradlew/gradlew.bat             # Gradle Scripts
```

### Frontend (onionweb)
```
onionweb
├── src
│   ├── api                         # Interface Request Encapsulation (Interact with Backend)
│   ├── assets                      # Static Resources (Images, Styles, etc.)
│   ├── components                  # Common Components
│   │   ├── layouts                 # Layout Components (e.g., MainLayout)
│   │   ├── Project.vue             # Project-Related Common Components
│   │   └── User.vue                # User-Related Common Components
│   ├── router                      # Routing Configuration (index.js)
│   ├── utils                       # Utility Classes (e.g., request.js encapsulates Axios)
│   └── views                       # Page View Components
│       ├── project                 # Project Module Views
│       │   ├── ChartView.vue       # Project Chart View
│       │   ├── KanbanView.vue      # Kanban View
│       │   ├── ProjectDetailLayout.vue # Project Detail Layout
│       │   └── ProjectInfoView.vue # Project Info View
│       ├── LoginView.vue           # Login View
│       ├── ProfileView.vue         # Profile View
│       ├── ProjectView.vue         # Project List View
│       ├── RegisterView.vue        # Register View
│       └── UserView.vue            # User Management View
├── App.vue                         # Root Component
├── main.js                         # Entry File
├── index.html                      # Entry HTML
├── package.json                    # Dependency Configuration
└── ...
```

## Feature Modules

### User Management
- User registration, login, and personal information management
- User permission control (based on Spring Security + JWT)

### Project Management
- Project creation, editing, and deletion
- Project member management (adding and removing members)
- Project information viewing and statistics

### Kanban Management
- Task card creation, movement, and status update
- Task classification and progress tracking

## Quick Start

### Backend Startup
1. Ensure JDK 1.8+ and Gradle are installed
2. Navigate to the `OnionServer` directory and execute the following command to start the backend:
   ```bash
   ./gradlew bootRun
   ```
3. The backend starts on port `8080` by default, which can be modified via `application.properties`.

### Frontend Startup
1. Ensure Node.js and npm are installed
2. Navigate to the `onionweb` directory and install dependencies:
   ```bash
   npm install
   ```
3. Start the frontend development server:
   ```bash
   npm run dev
   ```
4. Access `http://localhost:3000` in the browser (the specific port depends on the actual startup log).

## License

[MIT](LICENSE)