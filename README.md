# 🩺 HealthPocket

> Application mobile de santé personnelle - Centralisez et suivez vos informations de santé essentielles

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-green.svg)](https://developer.android.com/jetpack/compose)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

## 📋 Description

**HealthPocket** est une application mobile de santé simple et utile, permettant de centraliser et suivre des informations de santé essentielles au quotidien. L'application fonctionne en mode **offline-first** avec synchronisation automatique vers un backend.

### Fonctionnalités principales

- 👤 **Profil utilisateur** - Gérez vos informations personnelles (allergies, groupe sanguin)
- 💊 **Suivi de médicaments** - Ajoutez vos traitements avec rappels et historique
- 📅 **Agenda santé** - Planifiez vos rendez-vous médicaux
- 📝 **Journal santé** - Notez vos symptômes et ressentis quotidiens
- 📊 **Paramètres vitaux** - Suivez poids, tension, glycémie avec graphiques
- 🌐 **Multilingue** - Français, Anglais, Espagnol
- 🌙 **Mode sombre** - Interface adaptée jour/nuit

## 🏗️ Architecture du Monorepo

```
healthpocket/
├── mobile-app/              # Application Android (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/healthpocket/
│   │   │   │   ├── data/           # Couche données (Room, Retrofit, Repository)
│   │   │   │   │   ├── local/      # Base de données locale (Room)
│   │   │   │   │   ├── remote/     # API REST (Retrofit)
│   │   │   │   │   ├── repository/ # Repositories (offline-first)
│   │   │   │   │   └── preferences/# DataStore preferences
│   │   │   │   ├── di/             # Injection de dépendances (Hilt)
│   │   │   │   └── ui/             # Couche présentation (Screens, ViewModels)
│   │   │   │       ├── auth/       # Écrans d'authentification
│   │   │   │       ├── home/       # Dashboard principal
│   │   │   │       ├── medications/# Gestion des médicaments
│   │   │   │       ├── appointments/# Gestion des RDV
│   │   │   │       ├── journal/    # Journal de santé
│   │   │   │       ├── profile/    # Profil utilisateur
│   │   │   │       ├── settings/   # Paramètres
│   │   │   │       ├── theme/      # Thème Material 3
│   │   │   │       └── navigation/ # Navigation Compose
│   │   │   └── res/                # Ressources (strings, themes)
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── backend-api/             # API REST (Spring Boot + Kotlin)
│   ├── src/main/kotlin/
│   │   └── com/healthpocket/api/
│   │       ├── controller/         # Endpoints REST
│   │       ├── service/            # Logique métier
│   │       ├── repository/         # Accès données (JPA)
│   │       ├── model/              # Entités JPA
│   │       ├── dto/                # Data Transfer Objects
│   │       ├── security/           # JWT Authentication
│   │       ├── config/             # Configuration (OpenAPI)
│   │       └── exception/          # Gestion des erreurs
│   ├── src/test/                   # Tests unitaires
│   └── build.gradle.kts
│
├── database/
│   └── schema.sql           # Script SQL PostgreSQL
│
└── README.md
```

### Pattern MVVM

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

| Technologie | Version | Usage |
|-------------|---------|-------|
| Kotlin | 1.9.21 | Langage principal |
| Jetpack Compose | 1.5.0 | UI déclarative |
| Room | 2.6.1 | Base de données locale |
| Retrofit | 2.9.0 | Client HTTP |
| Hilt | 2.50 | Injection de dépendances |
| Navigation Compose | 2.7.6 | Navigation |
| Material 3 | 1.2.0 | Design System |
| Coroutines | 1.7.3 | Programmation asynchrone |
| DataStore | 1.0.0 | Préférences utilisateur |

### Backend

| Technologie | Version | Usage |
|-------------|---------|-------|
| Spring Boot | 3.2.0 | Framework backend |
| Kotlin | 1.9.21 | Langage principal |
| Spring Data JPA | 3.2.0 | ORM |
| PostgreSQL | 16 | Base de données |
| Spring Security | 6.2.0 | Authentification JWT |
| Springdoc OpenAPI | 2.3.0 | Documentation API |

## 🚀 Installation et Démarrage

### Prérequis

- **Java** 17+
- **Android Studio** Hedgehog (2023.1.1)+
- **PostgreSQL** 16+ ou compte [Neon](https://neon.tech)
- **Gradle** 8.0+

### 1. Cloner le projet

```bash
git clone https://github.com/your-username/healthpocket.git
cd healthpocket
```

### 2. Configuration de la base de données

#### Option A : PostgreSQL local

```bash
# Créer la base de données
createdb healthpocket

# Exécuter le script de création
psql -d healthpocket -f database/schema.sql
```

#### Option B : Neon (Cloud)

1. Créez un projet sur [Neon Console](https://console.neon.tech)
2. Copiez la connection string
3. Exécutez le script `database/schema.sql` via l'interface SQL

### 3. Lancer le Backend

```bash
cd backend-api

# Configurer les variables d'environnement
export DATABASE_URL=jdbc:postgresql://localhost:5432/healthpocket
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your-secret-key-min-32-characters-long

# Lancer l'application
./gradlew bootRun
```

L'API sera disponible sur `http://localhost:8080`

Documentation Swagger : `http://localhost:8080/swagger-ui.html`

### 4. Lancer l'Application Mobile

```bash
cd mobile-app

# Ouvrir avec Android Studio
# File > Open > sélectionner le dossier mobile-app

# Ou en ligne de commande
./gradlew installDebug
```

#### Configuration de l'API

Modifier `mobile-app/app/src/main/java/com/healthpocket/data/remote/ApiConfig.kt` :

```kotlin
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/" // Émulateur Android
    // const val BASE_URL = "http://YOUR_IP:8080/" // Appareil physique
}
```

## 🔄 Fonctionnement Offline/Sync

### Stratégie Offline-First

L'application utilise le pattern **offline-first** :

1. **Toutes les données sont stockées localement** dans Room Database
2. **Les modifications sont marquées** avec un flag `syncStatus` (`PENDING`, `SYNCED`, `ERROR`)
3. **La synchronisation est tentée** automatiquement lors de la création/modification
4. **En cas d'échec réseau**, les données restent en local avec statut `PENDING`
5. **La synchronisation complète** peut être déclenchée via l'endpoint `/api/sync`

### États de synchronisation

| État | Description |
|------|-------------|
| `SYNCED` | Données synchronisées avec le serveur |
| `PENDING` | Modifications locales en attente de sync |
| `ERROR` | Erreur lors de la dernière tentative |

### Flux de synchronisation

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

## 📱 Écrans de l'application

| Écran | Description |
|-------|-------------|
| **Login/Register** | Authentification utilisateur |
| **Home (Dashboard)** | Vue d'ensemble : médicaments du jour, prochains RDV, journal |
| **Medications** | Liste et gestion des médicaments |
| **Appointments** | Calendrier des rendez-vous médicaux |
| **Journal** | Suivi quotidien de l'humeur et des symptômes |
| **Profile** | Informations personnelles et de santé |
| **Settings** | Mode sombre, langue, préférences |

## 📚 Documentation API

### Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/auth/refresh` | Rafraîchir le token |
| GET | `/api/users/profile` | Profil utilisateur |
| PUT | `/api/users/profile` | Mise à jour profil |
| GET | `/api/medications` | Liste des médicaments |
| POST | `/api/medications` | Ajouter un médicament |
| GET | `/api/appointments` | Liste des RDV |
| POST | `/api/appointments` | Ajouter un RDV |
| GET | `/api/health-logs` | Journal de santé |
| POST | `/api/health-logs` | Nouvelle entrée |
| GET | `/api/vitals` | Paramètres vitaux |
| POST | `/api/vitals` | Ajouter une mesure |
| POST | `/api/sync` | Synchronisation complète |

### Exemple de requête

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Créer un médicament (avec token)
curl -X POST http://localhost:8080/api/medications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Doliprane",
    "dosage": "1000mg",
    "frequency": "3 fois par jour",
    "scheduleTimes": ["08:00", "14:00", "20:00"],
    "startDate": "2024-01-15"
  }'
```

## 🧪 Tests

### Tests Mobile

```bash
cd mobile-app

# Tests unitaires
./gradlew test

# Tests instrumentés
./gradlew connectedAndroidTest
```

### Tests Backend

```bash
cd backend-api

# Tous les tests
./gradlew test

# Avec couverture
./gradlew test jacocoTestReport
```

## 🌍 Internationalisation

L'application supporte :
- 🇫🇷 Français (par défaut)
- 🇬🇧 English
- 🇪🇸 Español

Les fichiers de traduction sont dans :
- `mobile-app/app/src/main/res/values/strings.xml` (FR)
- `mobile-app/app/src/main/res/values-en/strings.xml` (EN)
- `mobile-app/app/src/main/res/values-es/strings.xml` (ES)

## 🔐 Sécurité

- **Authentification JWT** avec access token et refresh token
- **Mots de passe hashés** avec BCrypt
- **HTTPS** recommandé en production
- **Validation** des entrées côté serveur
- **CORS** configuré pour l'API

## 📦 Structure des données

### Entités principales

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

## 🚧 Améliorations futures

- [ ] Graphiques de suivi des paramètres vitaux
- [ ] Export PDF des données de santé
- [ ] Partage avec un professionnel de santé
- [ ] Widgets Android pour accès rapide
- [ ] Notifications push via Firebase
- [ ] Authentification biométrique

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Auteurs

- Étudiant Ingénieur - Projet Académique

## 🙏 Remerciements

- [Material Design 3](https://m3.material.io/)
- [Android Developers](https://developer.android.com/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Neon Database](https://neon.tech/)
