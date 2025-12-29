# 🎯 Prochaines Étapes - HealthPocket

Migration terminée avec succès ! ✅

Voici les étapes à suivre pour finaliser la configuration et lancer l'application.

---

## 1️⃣ Configuration Backend (OBLIGATOIRE)

### Localisation
`app/src/main/java/com/healthpocket/data/remote/ApiConfig.kt`

### Action Requise

```kotlin
object ApiConfig {
    // 🔧 MODIFIER CETTE LIGNE selon votre environnement
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Pour émulateur Android : http://10.0.2.2:PORT/
    // Pour appareil physique : http://VOTRE_IP:PORT/
    // Exemple : http://192.168.1.100:8080/
}
```

### Comment trouver votre IP ?

**Linux/Mac** :
```bash
ip addr show | grep inet
# ou
ifconfig | grep inet
```

**Windows** :
```cmd
ipconfig
```

---

## 2️⃣ Premier Build

### Ouvrir le Projet

1. Lancer **Android Studio**
2. **File > Open** → Sélectionner `/home/flotss/Projects/HealthPocket/mobile-app`
3. Attendre l'indexation et la synchronisation Gradle

### Synchroniser Gradle

Android Studio devrait automatiquement synchroniser. Si ce n'est pas le cas :
- Cliquer sur l'icône 🐘 "Sync Project with Gradle Files"
- Ou : **File > Sync Project with Gradle Files**

### Build Initial

```bash
cd /home/flotss/Projects/HealthPocket/mobile-app
./gradlew clean build
```

**Note** : Le premier build peut prendre 5-10 minutes (téléchargement des dépendances + génération KSP/Room)

---

## 3️⃣ Vérifications

### ✅ Checklist Avant Lancement

- [ ] Backend accessible (tester l'URL dans un navigateur)
- [ ] `BASE_URL` configurée dans `ApiConfig.kt`
- [ ] Gradle sync réussi
- [ ] Build réussi sans erreurs
- [ ] Émulateur ou appareil connecté

### Tester la Connexion Backend

```bash
# Remplacer par votre URL
curl http://10.0.2.2:8080/health
# ou
curl http://192.168.1.100:8080/health
```

---

## 4️⃣ Lancer l'Application

### Via Android Studio

1. Sélectionner un émulateur ou appareil
2. Cliquer sur ▶️ **Run 'app'**
3. Attendre le déploiement

### Via Ligne de Commande

```bash
# Installer sur l'appareil/émulateur connecté
./gradlew installDebug

# Lancer l'application
adb shell am start -n com.healthpocket/.MainActivity
```

---

## 5️⃣ Tests

### Tests Unitaires

```bash
./gradlew test
```

### Tests d'Instrumentation

```bash
# Nécessite un émulateur/appareil connecté
./gradlew connectedAndroidTest
```

### Test Manuel

1. Lancer l'application
2. Créer un compte (Register)
3. Se connecter (Login)
4. Tester les fonctionnalités :
   - ✅ Ajouter un médicament
   - ✅ Créer un rendez-vous
   - ✅ Ajouter une entrée journal
   - ✅ Modifier le profil

---

## 6️⃣ Résolution de Problèmes

### Erreur : "Unable to resolve dependency"

```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Erreur KSP : "Task :app:kspDebugKotlin FAILED"

```bash
./gradlew clean
rm -rf app/build/generated/ksp
./gradlew kspDebugKotlin
```

### Erreur Room : "Cannot find schema"

Vérifier que le dossier existe :
```bash
ls -la app/schemas/
```

### Erreur Retrofit : "Failed to connect"

1. Vérifier `BASE_URL` dans `ApiConfig.kt`
2. Vérifier que le backend est lancé
3. Pour émulateur : utiliser `10.0.2.2` pas `localhost`
4. Pour appareil physique : même réseau WiFi que le backend

### Erreur Hilt : "Dagger component not found"

```bash
./gradlew clean
./gradlew build
```

---

## 7️⃣ Configuration Avancée (Optionnel)

### Mode Debug vs Release

**Debug** (par défaut) :
- Logs activés
- Pas d'obfuscation
- Debuggable

**Release** :
```bash
./gradlew assembleRelease
```

### Changer les Timeouts API

Dans `ApiConfig.kt` :
```kotlin
const val CONNECT_TIMEOUT = 30L  // secondes
const val READ_TIMEOUT = 30L
const val WRITE_TIMEOUT = 30L
```

### Désactiver les Logs HTTP

Dans `AppModule.kt`, commenter :
```kotlin
// .addInterceptor(loggingInterceptor)
```

---

## 8️⃣ Documentation

### Fichiers Utiles

- **[README.md](README.md)** : Documentation générale
- **[MIGRATION_REPORT.md](MIGRATION_REPORT.md)** : Détails de la migration
- **[NEXT_STEPS.md](NEXT_STEPS.md)** : Ce fichier

### Structure du Code

```
app/src/main/java/com/healthpocket/
├── data/           # Couche données (DB, API, Repos)
├── di/             # Injection de dépendances (Hilt)
├── ui/             # Interface utilisateur (Compose)
│   ├── auth/       # Authentification
│   ├── home/       # Écran d'accueil
│   ├── medications/# Médicaments
│   ├── appointments/# Rendez-vous
│   ├── journal/    # Journal santé
│   ├── profile/    # Profil
│   └── settings/   # Paramètres
└── HealthPocketApplication.kt
```

---

## 9️⃣ Commandes Utiles

### Gradle

```bash
# Clean
./gradlew clean

# Build Debug
./gradlew assembleDebug

# Build Release
./gradlew assembleRelease

# Tests
./gradlew test

# Linter
./gradlew lint

# Dépendances
./gradlew dependencies
```

### ADB

```bash
# Lister les appareils
adb devices

# Installer l'APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Désinstaller
adb uninstall com.healthpocket

# Logs en temps réel
adb logcat | grep HealthPocket

# Clear app data
adb shell pm clear com.healthpocket
```

---

## 🔟 Checklist de Production

Avant de déployer en production :

- [ ] Tests unitaires passent (100%)
- [ ] Tests d'instrumentation passent
- [ ] URL backend configurée pour la production
- [ ] Logs HTTP désactivés
- [ ] ProGuard/R8 configuré
- [ ] APK signé avec keystore de production
- [ ] Version code/name incrémentés
- [ ] Permissions justifiées dans le store
- [ ] Screenshots et description préparés
- [ ] Politique de confidentialité disponible

---

## 📞 Besoin d'Aide ?

### Problème de Build
1. Vérifier les logs : `./gradlew build --stacktrace`
2. Nettoyer : `./gradlew clean`
3. Invalider cache Android Studio : **File > Invalidate Caches > Invalidate and Restart**

### Problème Runtime
1. Vérifier Logcat dans Android Studio
2. Filtrer par tag : `HealthPocket`
3. Vérifier les exceptions et stack traces

### Problème Backend
1. Tester l'URL dans un navigateur
2. Vérifier les logs du backend
3. Vérifier le réseau (même WiFi pour appareil physique)

---

## 🎉 C'est Parti !

Votre application HealthPocket est prête à être lancée !

### Commande Rapide

```bash
cd /home/flotss/Projects/HealthPocket/mobile-app
./gradlew clean build
./gradlew installDebug
```

### Bon Développement ! 🚀

---

**Questions ?** Consultez [README.md](README.md) ou [MIGRATION_REPORT.md](MIGRATION_REPORT.md)


