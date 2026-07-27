# 🔐 PolpoPass — Gestionnaire de Mots de Passe (JavaFX)

PolpoPass est un gestionnaire de mots de passe de bureau écrit en **Java / JavaFX**. Il permet de générer des mots de passe robustes, de stocker vos identifiants dans un coffre-fort chiffré local (`vault.enc`), et de gérer votre vie numérique via un tableau de bord, un audit de sécurité, l'import/export CSV et une clé de secours en cas d'oubli du mot de passe maître.

---

## ✨ Fonctionnalités

- **Authentification** : écrans de connexion / inscription (`Login.fxml`, `Register.fxml`), coffre-fort local par utilisateur.
- **Tableau de bord** : vue d'ensemble des identifiants stockés (`Dashboard.fxml`).
- **Générateur de mots de passe** : longueur et jeux de caractères (majuscules, minuscules, chiffres, symboles) configurables.
- **Audit de sécurité** : analyse du coffre-fort pour repérer les mots de passe faibles ou dupliqués (`AuditController`).
- **Import / Export CSV** : sauvegarde ou migration des identifiants (`CsvService`).
- **Clé de secours** : génération d'une clé de récupération (`VK-XXXX-XXXX-XXXX-XXXX`) permettant de réinitialiser le mot de passe maître en cas d'oubli (`RecoveryKeyManager`).
- **Presse-papiers auto-effaçant** : le mot de passe copié est effacé automatiquement après un délai configurable (`ClipboardUtils`).
- **Verrouillage automatique** : verrouillage de session après une période d'inactivité configurable.
- **Thèmes clair / sombre** : feuilles de style CSS (`light.css` / `dark.css`).
- **Paramètres persistants** : stockés dans `app_config.properties` (délai de verrouillage, délai d'effacement du presse-papiers, thème, affichage des mots de passe par défaut).

---

## 🛠️ Architecture du Projet

```text
src/
└── main/
    ├── java/
    │   ├── app/
    │   │   ├── Launcher.java          # Point d'entrée (délègue à MainApp)
    │   │   ├── MainApp.java           # Application JavaFX, fenêtre principale
    │   │   └── AppContext.java        # État de session, navigation, sauvegarde/chargement du coffre
    │   ├── controllers/               # Un contrôleur JavaFX par écran (FXML)
    │   │   ├── LoginController.java
    │   │   ├── RegisterController.java
    │   │   ├── ResetPasswordController.java
    │   │   ├── RecoveryDialogController.java
    │   │   ├── MainViewController.java
    │   │   ├── DashboardController.java
    │   │   ├── GeneratorController.java
    │   │   ├── AddEntryController.java
    │   │   ├── AuditController.java
    │   │   ├── DataManagementController.java
    │   │   ├── SettingsController.java
    │   │   └── BaseController.java    # Interface commune d'injection du contexte
    │   ├── model/
    │   │   ├── PasswordEntry.java     # Entrée du coffre (site, identifiant, mot de passe, catégorie, tags, favori...)
    │   │   └── AppSettings.java       # Chargement/sauvegarde des préférences (app_config.properties)
    │   └── crypto/
    │       ├── CryptoUtils.java       # Chiffrement AES-256 GCM + dérivation de clé PBKDF2
    │       ├── PasswordGenerator.java # Génération de mots de passe aléatoires
    │       ├── RecoveryKeyManager.java# Génération / lecture de la clé de secours
    │       ├── CsvService.java        # Import / export CSV du coffre-fort
    │       └── ClipboardUtils.java    # Copie presse-papiers avec auto-effacement
    └── resources/
        ├── fxml/                     # Vues JavaFX (Login, Register, Dashboard, Generator, Audit, Settings, ...)
        ├── css/                      # Thèmes clair (light.css) et sombre (dark.css)
        └── icons/                    # Icônes de l'interface
```

## 📁 Rangement des fichiers

- **app/** : point d'entrée et contexte applicatif partagé (session, navigation entre écrans, thème, verrouillage automatique).
- **controllers/** : logique de présentation, un contrôleur par vue FXML.
- **model/** : objets métiers (données pures) et préférences utilisateur.
- **crypto/** : chiffrement, dérivation de clé, génération de mots de passe, clé de secours, import/export CSV, presse-papiers.
- **resources/** : fichiers FXML, feuilles de style CSS et icônes.

---

## 🔒 Spécifications de Sécurité

- **Dérivation de Clé (PBKDF2)** : le mot de passe maître n'est jamais stocké. Il est étiré via `PBKDF2WithHmacSHA256` avec 65 536 itérations pour produire une clé AES de 256 bits.
- **Chiffrement AES-256-GCM** : le coffre-fort (`~/.vaultjar/vault.enc`) est chiffré en mode GCM, garantissant confidentialité et authentification des données.
- **Presse-papiers auto-effaçant** : un mot de passe copié est retiré du presse-papiers après un délai configurable.
- **Verrouillage automatique** : la session se verrouille après une période d'inactivité configurable.
- **Clé de secours** : permet de retrouver l'accès au coffre en cas d'oubli du mot de passe maître, sans jamais stocker celui-ci en clair.

---

## 🚀 Comment exécuter l'application ?

### Prérequis

- JDK 17 ou supérieur
- Maven (pour la compilation depuis les sources)
- Le module **JavaFX** doit être disponible (embarqué dans le jar exécutable via un plugin de type *fat jar*, ou disponible séparément selon la configuration de build)

### Lancement depuis les sources

```bash
mvn clean package
java -jar target/polpopass-fx-1.0.jar
```

### Lancement via l'exécutable Windows (.exe / .msi)

Le script `release.py` automatise la génération des installateurs Windows avec `jpackage` (build Maven, packaging `.exe` et `.msi`, icône personnalisée). Voir les instructions dans l'onglet Releases du dépôt pour télécharger un installateur prêt à l'emploi.

---

## 📌 Notes

- Le dossier `src/` contient encore, à la racine, d'anciens fichiers `model/` et `crypto/` hérités de la version Swing d'origine ; le code actif du projet se trouve sous `src/main/java`.
- `app_config.properties` est généré/mis à jour automatiquement au premier lancement et à chaque modification des paramètres depuis l'écran *Settings*.
