# 🩺 HealthPocket

> Personal health mobile application - Centralize and track your essential health information

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-green.svg)](https://developer.android.com/jetpack/compose)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Test Coverage](https://img.shields.io/badge/Test%20Coverage-40%2B%20tests-success.svg)](/)

## 📋 Description

**HealthPocket** is a simple and useful mobile health application that allows you to centralize and track essential health information in your daily life. The application works in **offline-first** mode with automatic synchronization to a backend server.

### Key Features

- 👤 **User Profile** - Manage your personal information (allergies, blood type)
- 💊 **Medication Tracking** - Add your treatments with reminders and history
- 📅 **Health Calendar** - Schedule your medical appointments
- 📝 **Health Journal** - Log your symptoms and daily feelings
- 📊 **Vital Metrics** - Track weight, blood pressure, glucose with charts
- 🌐 **Multilingual** - English, French, Spanish, Croatian
- 🌙 **Dark Mode** - Day/night adaptive interface

## 🏗️ Monorepo Architecture

```
healthpocket/
├── mobile-app/              # Android Application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/healthpocket/
│   │   │   │   ├── data/           # Data layer (Room, Retrofit, Repository)
│   │   │   │   │   ├── local/      # Local database (Room)
│   │   │   │   │   ├── remote/     # REST API (Retrofit)
│   │   │   │   │   ├── repository/ # Repositories (offline-first)
│   │   │   │   │   └── preferences/# DataStore preferences
│   │   │   │   ├── di/             # Dependency injection (Hilt)
│   │   │   │   └── ui/             # Presentation layer (Screens, ViewModels)
│   │   │   │       ├── auth/       # Authentication screens
│   │   │   │       ├── home/       # Main dashboard
│   │   │   │       ├── medications/# Medication management
│   │   │   │       ├── appointments/# Appointments management
│   │   │   │       ├── journal/    # Health journal
│   │   │   │       ├── profile/    # User profile
│   │   │   │       ├── settings/   # Settings
│   │   │   │       ├── theme/      # Material 3 theme
│   │   │   │       └── navigation/ # Compose navigation
│   │   │   └── res/                # Resources (strings, themes)
│   │   ├── src/test/               # Unit tests (ViewModels, Repositories, Utils)
│   │   ├── src/androidTest/        # UI tests (Compose UI tests)
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── backend-api/             # REST API (Spring Boot + Kotlin)
│   ├── src/main/kotlin/
│   │   └── com/healthpocket/api/
│   │       ├── controller/         # REST endpoints
│   │       ├── service/            # Business logic
│   │       ├── repository/         # Data access (JPA)
│   │       ├── model/              # JPA entities
│   │       ├── dto/                # Data Transfer Objects
│   │       ├── security/           # JWT Authentication
│   │       ├── config/             # Configuration (OpenAPI)
│   │       └── exception/          # Error handling
│   ├── src/test/                   # Unit & integration tests
│   └── build.gradle.kts
│
├── database/
│   └── schema.sql           # PostgreSQL SQL script
│
└── README.md
```

### MVVM Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐      │
│  │   Screen    │◄───│  ViewModel  │◄───│   State     │      │
│  │  (Compose)  │    │   (Hilt)    │    │  (StateFlow)│      │
│  └─────────────┘    └─────────────┘    └─────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Data Layer                            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐      │
│  │ Repository  │◄───│  Local DB   │    │  Remote API │      │
│  │(Offline-1st)│    │   (Room)    │    │  (Retrofit) │      │
│  └─────────────┘    └─────────────┘    └─────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Technologies

### Mobile (Android)

| Technology         | Version | Usage                    |
| ------------------ | ------- | ------------------------ |
| Kotlin             | 1.9.21  | Main language            |
| Jetpack Compose    | 1.5.0   | Declarative UI           |
| Room               | 2.6.1   | Local database           |
| Retrofit           | 2.9.0   | HTTP client              |
| Hilt               | 2.50    | Dependency injection     |
| Navigation Compose | 2.7.6   | Navigation               |
| Material 3         | 1.2.0   | Design System            |
| Coroutines         | 1.7.3   | Asynchronous programming |
| DataStore          | 1.0.0   | User preferences         |
| JUnit              | 4.13.2  | Unit testing             |
| Mockito            | 5.7.0   | Mocking framework        |
| Espresso           | 3.6.1   | UI testing               |
| Turbine            | 1.0.0   | Flow testing             |

### Backend

| Technology        | Version | Usage                        |
| ----------------- | ------- | ---------------------------- |
| Spring Boot       | 3.2.0   | Backend framework            |
| Kotlin            | 1.9.21  | Main language                |
| Spring Data JPA   | 3.2.0   | ORM                          |
| PostgreSQL        | 16      | Database                     |
| Spring Security   | 6.2.0   | JWT Authentication           |
| Springdoc OpenAPI | 2.3.0   | API documentation            |
| MockK             | 1.13.8  | Kotlin mocking library       |
| JUnit 5           | 5.10.1  | Testing framework            |
| H2 Database       | 2.2.224 | In-memory database for tests |

## 🚀 Installation and Setup

### Prerequisites

- **Java** 17+
- **Android Studio** Hedgehog (2023.1.1)+
- **PostgreSQL** 16+ or [Neon](https://neon.tech) account
- **Gradle** 8.0+

### 1. Clone the Project

```bash
git clone https://github.com/your-username/healthpocket.git
cd healthpocket
```

### 2. Database Configuration

#### Option A: Local PostgreSQL

```bash
# Create the database
createdb healthpocket

# Run the creation script
psql -d healthpocket -f database/schema.sql
```

#### Option B: Neon (Cloud)

1. Create a project on [Neon Console](https://console.neon.tech)
2. Copy the connection string
3. Execute the `database/schema.sql` script via the SQL interface

### 3. Start the Backend

```bash
cd backend-api

# Configure environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/healthpocket
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your-secret-key-min-32-characters-long

# Start the application
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

Swagger documentation: `http://localhost:8080/swagger-ui.html`

### 4. Start the Mobile App

```bash
cd mobile-app

# Open with Android Studio
# File > Open > select the mobile-app folder

# Or via command line
./gradlew installDebug
```

#### API Configuration

Edit `mobile-app/app/src/main/java/com/healthpocket/data/remote/ApiConfig.kt`:

```kotlin
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/" // Android Emulator
    // const val BASE_URL = "http://YOUR_IP:8080/" // Physical device
}
```

## 🔄 Offline/Sync Operation

### Offline-First Strategy

The application uses the **offline-first** pattern:

1. **All data is stored locally** in Room Database
2. **Modifications are marked** with a `syncStatus` flag (`PENDING`, `SYNCED`, `ERROR`)
3. **Synchronization is attempted** automatically on creation/modification
4. **In case of network failure**, data remains local with `PENDING` status
5. **Full synchronization** can be triggered via the `/api/sync` endpoint

### Synchronization States

| State     | Description                       |
| --------- | --------------------------------- |
| `SYNCED`  | Data synchronized with server     |
| `PENDING` | Local modifications awaiting sync |
| `ERROR`   | Error during last sync attempt    |

### Synchronization Flow

```
┌──────────────┐    Modification    ┌──────────────┐
│  User Action │───────────────────►│  Room (Local)│
└──────────────┘                    └──────────────┘
                                           │
                                           ▼
                                    ┌──────────────┐
                                    │  Repository  │
                                    │  trySync()   │
                                    └──────────────┘
                                           │
                         ┌─────────────────┼─────────────────┐
                         ▼                 ▼                 ▼
                   ┌──────────┐     ┌──────────┐     ┌──────────┐
                   │ Internet │     │No Network│     │  Error   │
                   │Available │     │          │     │          │
                   └──────────┘     └──────────┘     └──────────┘
                         │                │                │
                         ▼                ▼                ▼
                   ┌──────────┐     ┌──────────┐     ┌──────────┐
                   │  SYNCED  │     │ PENDING  │     │  ERROR   │
                   └──────────┘     └──────────┘     └──────────┘
```

## 📱 Application Screens

| Screen               | Description                                                   |
| -------------------- | ------------------------------------------------------------- |
| **Login/Register**   | User authentication                                           |
| **Home (Dashboard)** | Overview: today's medications, upcoming appointments, journal |
| **Medications**      | Medication list and management                                |
| **Appointments**     | Medical appointments calendar                                 |
| **Journal**          | Daily mood and symptom tracking                               |
| **Profile**          | Personal and health information                               |
| **Settings**         | Dark mode, language, preferences                              |

## 📚 API Documentation

### Main Endpoints

| Method | Endpoint             | Description          |
| ------ | -------------------- | -------------------- |
| POST   | `/api/auth/register` | User registration    |
| POST   | `/api/auth/login`    | User login           |
| POST   | `/api/auth/refresh`  | Refresh token        |
| GET    | `/api/users/profile` | User profile         |
| PUT    | `/api/users/profile` | Update profile       |
| GET    | `/api/medications`   | List medications     |
| POST   | `/api/medications`   | Add medication       |
| GET    | `/api/appointments`  | List appointments    |
| POST   | `/api/appointments`  | Add appointment      |
| GET    | `/api/health-logs`   | Health journal       |
| POST   | `/api/health-logs`   | New entry            |
| GET    | `/api/vitals`        | Vital metrics        |
| POST   | `/api/vitals`        | Add measurement      |
| POST   | `/api/sync`          | Full synchronization |

### Example Request

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Create a medication (with token)
curl -X POST http://localhost:8080/api/medications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Ibuprofen",
    "dosage": "400mg",
    "frequency": "3 times a day",
    "scheduleTimes": ["08:00", "14:00", "20:00"],
    "startDate": "2024-01-15"
  }'
```

## 🧪 Tests

### Test Coverage

HealthPocket includes a comprehensive testing suite with over **40 test files** covering:

- **Unit Tests**: ViewModels, Repositories, Services, Utilities
- **Integration Tests**: API Controllers, Database operations
- **UI Tests**: Compose UI screens and navigation

### Backend Tests

#### Service Layer Tests

- `AuthServiceTest` - Authentication logic
- `UserServiceTest` - User management
- `MedicationServiceTest` - Medication operations
- `AppointmentServiceTest` - Appointment management
- `HealthLogServiceTest` - Health journal
- `VitalMetricServiceTest` - Vital metrics tracking
- `SyncServiceTest` - Data synchronization

#### Controller Tests

- `AppointmentControllerTest` - REST endpoints
- `MedicationControllerTest` - API integration
- `VitalMetricControllerTest` - Metrics API

#### Security Tests

- `JwtTokenProviderTest` - JWT token generation and validation

#### Run Backend Tests

```bash
cd backend-api

# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "com.healthpocket.api.service.AuthServiceTest"
```

### Mobile Tests

#### Unit Tests

**ViewModels**:

- `AuthViewModelTest` - Authentication flows
- `MedicationsViewModelTest` - Medication list
- `AddMedicationViewModelTest` - Add medication
- `EditMedicationViewModelTest` - Edit medication
- `MedicationDetailViewModelTest` - Medication details
- `AppointmentsViewModelTest` - Appointment list
- `AddAppointmentViewModelTest` - Add appointment
- `AppointmentDetailViewModelTest` - Appointment details
- `JournalViewModelTest` - Health journal
- `SettingsViewModelTest` - Settings management

**Repositories**:

- `MedicationRepositoryTest` - Offline-first data operations
- `AppointmentRepositoryTest` - Appointment data layer
- `HealthLogRepositoryTest` - Journal data layer
- `VitalMetricRepositoryTest` - Metrics data layer

**Utilities**:

- `DateTimeUtilsTest` - Date/time formatting
- `ColorUtilsTest` - Color parsing
- `MetricUtilsTest` - Metric conversions
- `ScheduleTimeUtilsTest` - Schedule parsing

#### UI Tests (Espresso + Compose)

- `AuthUITest` - Login/Registration screens
- `HomeUITest` - Dashboard
- `MedicationUITest` - Medication screens
- `AppointmentUITest` - Appointment screens
- `JournalUITest` - Journal screens
- `ProfileUITest` - Profile screen
- `SettingsUITest` - Settings screen
- `VitalsUITest` - Vital metrics screen
- `NavigationTest` - Navigation flows

#### Run Mobile Tests

```bash
cd mobile-app

# Run unit tests
./gradlew test

# Run unit tests for a specific flavor
./gradlew testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.healthpocket.ui.medications.MedicationsViewModelTest"
```

### Test Technologies

#### Backend

- **JUnit 5** - Test framework
- **MockK** - Kotlin mocking library
- **Spring Boot Test** - Integration testing
- **H2 Database** - In-memory database for tests

#### Mobile

- **JUnit 4** - Test framework
- **Mockito** - Mocking framework
- **Turbine** - Flow testing library
- **Espresso** - UI testing framework
- **Compose Test** - Jetpack Compose testing
- **Hilt Test** - Dependency injection for tests

## 🌍 Internationalization

The application supports:

- �🇧 English (default)
- 🇫🇷 French
- 🇪🇸 Spanish
- 🇭🇷 Croatian

Translation files are located in:

- `mobile-app/app/src/main/res/values/strings.xml` (EN - default)
- `mobile-app/app/src/main/res/values-fr/strings.xml` (FR)
- `mobile-app/app/src/main/res/values-es-rES/strings.xml` (ES)
- `mobile-app/app/src/main/res/values-hr/strings.xml` (HR)

## 🔐 Security

- **JWT Authentication** with access token and refresh token
- **Password hashing** with BCrypt
- **HTTPS** recommended in production
- **Input validation** on server side
- **CORS** configured for API

## 📦 Data Structure

### Main Entities

```
User
├── id, email, password_hash
├── first_name, last_name, birth_date
├── gender, blood_type, allergies
└── emergency_contact, preferences

Medication
├── id, user_id, name, dosage
├── frequency, schedule_times
├── start_date, end_date
└── reminder_enabled, sync_status

MedicationIntake
├── id, medication_id, user_id
├── scheduled_time, taken_time
└── status, sync_status

Appointment
├── id, user_id, title
├── doctor_name, location
├── appointment_date, duration
└── reminder_enabled, status

HealthLog
├── id, user_id, log_date
├── mood, energy_level, sleep_quality
└── symptoms, notes

VitalMetric
├── id, user_id, metric_type
├── value, secondary_value, unit
└── measured_at, notes
```

## 🚧 Future Improvements

- [ ] Vital metrics tracking charts
- [ ] PDF export of health data
- [ ] Sharing with healthcare professionals
- [ ] Android widgets for quick access
- [ ] Push notifications via Firebase
- [ ] Biometric authentication

## 📝 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## 👥 Authors

- Engineering Student - Academic Project

## 🙏 Acknowledgments

- [Material Design 3](https://m3.material.io/)
- [Android Developers](https://developer.android.com/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Neon Database](https://neon.tech/)

---

<div align="center">

Made with ❤️ for better health management

**HealthPocket** - Your health, in your pocket

</div>
