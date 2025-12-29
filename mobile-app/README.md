# HealthPocket - Application Mobile Android

Application mobile de gestion de santé personnelle développée avec Jetpack Compose et Kotlin.

## 🚀 Fonctionnalités

- **Gestion des Médicaments** : Suivi des prises, rappels automatiques
- **Rendez-vous Médicaux** : Planification et rappels
- **Journal de Santé** : Suivi quotidien de l'humeur et de l'énergie
- **Métriques Vitales** : Enregistrement des données de santé
- **Profil Utilisateur** : Informations médicales et contacts d'urgence
- **Synchronisation Cloud** : Backup automatique des données

## 📋 Prérequis

- **Android Studio** : Ladybug | 2024.2.1 ou supérieur
- **JDK** : 17 ou supérieur
- **Android SDK** : API 26+ (Android 8.0+)
- **Gradle** : 8.13.2

## 🛠️ Stack Technique

### Architecture
- **Pattern** : MVVM + Repository
- **DI** : Hilt (Dagger)
- **UI** : Jetpack Compose + Material 3
- **Navigation** : Navigation Compose

### Base de Données & Réseau
- **Local** : Room Database
- **API** : Retrofit + OkHttp
- **JSON** : Moshi
- **Storage** : DataStore Preferences

### Asynchrone & Background
- **Coroutines** : Kotlin Coroutines + Flow
- **Background Tasks** : WorkManager
- **Images** : Coil

### Versions
- **Kotlin** : 2.0.21
- **Compose BOM** : 2024.09.00
- **Hilt** : 2.52
- **Room** : 2.6.1
- **Retrofit** : 2.9.0

## 📦 Installation

### 1. Cloner le Projet

```bash
git clone <repository-url>
cd mobile-app
```

### 2. Configuration Backend

Modifier l'URL du backend dans `ApiConfig.kt` :

```kotlin
// app/src/main/java/com/healthpocket/data/remote/ApiConfig.kt
object ApiConfig {
    const val BASE_URL = "http://VOTRE_IP:PORT/"
}
```

**Pour émulateur Android** : `http://10.0.2.2:8080/`  
**Pour appareil physique** : `http://192.168.x.x:8080/`

### 3. Build du Projet

```bash
./gradlew clean build
```

### 4. Lancer l'Application

```bash
./gradlew installDebug
```

Ou via Android Studio : **Run > Run 'app'**

## 🧪 Tests

### Tests Unitaires

```bash
./gradlew test
```

### Tests d'Instrumentation

```bash
./gradlew connectedAndroidTest
```

## 📁 Structure du Projet

```
app/src/main/java/com/healthpocket/
├── data/
│   ├── local/          # Room Database (DAOs, Entities)
│   ├── remote/         # API REST (Retrofit, DTOs)
│   ├── repository/     # Repositories
│   ├── preferences/    # DataStore
│   └── notification/   # Broadcast Receivers
├── di/                 # Hilt Modules
├── ui/
│   ├── auth/          # Écrans d'authentification
│   ├── home/          # Écran d'accueil
│   ├── medications/   # Gestion des médicaments
│   ├── appointments/  # Gestion des rendez-vous
│   ├── journal/       # Journal de santé
│   ├── profile/       # Profil utilisateur
│   ├── settings/      # Paramètres
│   ├── navigation/    # Navigation
│   └── theme/         # Thème Material 3
├── HealthPocketApplication.kt
└── MainActivity.kt
```

## 🔐 Permissions

L'application nécessite les permissions suivantes :

- `INTERNET` : Communication avec le backend
- `ACCESS_NETWORK_STATE` : Vérification de la connectivité
- `POST_NOTIFICATIONS` : Notifications de rappels (Android 13+)
- `SCHEDULE_EXACT_ALARM` : Alarmes exactes pour les rappels
- `RECEIVE_BOOT_COMPLETED` : Reprogrammation des rappels après redémarrage
- `VIBRATE` : Vibration pour les notifications
- `WAKE_LOCK` : Réveil de l'appareil pour les rappels

## 🎨 Thème & Design

- **Design System** : Material 3
- **Couleur Primaire** : Teal (#00897B)
- **Couleur Secondaire** : Deep Orange (#FF7043)
- **Mode Sombre** : Supporté
- **Langue** : Français

## 🗄️ Base de Données

### Tables Room
- **users** : Informations utilisateur
- **medications** : Médicaments
- **medication_intakes** : Historique des prises
- **appointments** : Rendez-vous médicaux
- **health_logs** : Entrées du journal de santé
- **vital_metrics** : Métriques vitales (tension, poids, etc.)

### Schéma
Version actuelle : **1**  
Localisation : `app/schemas/`

## 🔄 Synchronisation

L'application synchronise automatiquement les données avec le backend :
- **Upload** : Après chaque modification locale
- **Download** : Au démarrage et périodiquement
- **Conflit** : Résolution côté serveur (last-write-wins)

## 🐛 Débogage

### Logs Gradle

```bash
./gradlew build --stacktrace
```

### Logs Runtime

Utiliser Logcat dans Android Studio avec le filtre `HealthPocket`

### Problèmes Courants

#### Build Failed - KSP
```bash
./gradlew clean
./gradlew kspDebugKotlin
```

#### Room Schema Export
Vérifier que le dossier `schemas/` existe et est accessible

#### Retrofit Connection Failed
- Vérifier l'URL dans `ApiConfig.kt`
- Vérifier que le backend est accessible
- Pour émulateur, utiliser `10.0.2.2` au lieu de `localhost`

## 📱 Compatibilité

- **minSdk** : 26 (Android 8.0 Oreo)
- **targetSdk** : 36 (Android 15)
- **Couverture** : ~98% des appareils Android actifs

## 🚀 Déploiement

### Build Release

```bash
./gradlew assembleRelease
```

L'APK sera généré dans : `app/build/outputs/apk/release/`

### Signature

Configurer le keystore dans `app/build.gradle.kts` :

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/keystore.jks")
        storePassword = "password"
        keyAlias = "alias"
        keyPassword = "password"
    }
}
```

## 📄 Documentation

- [Rapport de Migration](MIGRATION_REPORT.md) - Détails de la migration
- [Architecture MVVM](docs/architecture.md) - Guide d'architecture (à créer)
- [API Documentation](docs/api.md) - Documentation API (à créer)

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 License

Ce projet est sous licence [MIT](LICENSE).

## 👥 Équipe

- **Développement** : Équipe HealthPocket
- **Design** : Material 3 Design System

## 📞 Support

Pour toute question ou problème :
- Ouvrir une issue sur GitHub
- Contacter l'équipe de développement

---

**Version** : 1.0.0  
**Dernière mise à jour** : 29 décembre 2024


