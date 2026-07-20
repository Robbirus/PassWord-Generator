# 🔐 Gestionnaire de Mots de Passe Java

PasswordGenerator est un gestionnaire de mots de passe de bureau léger, sécurisé et multiplateforme développé en Java Swing. Il permet de générer des mots de passe robustes et de stocker vos identifiants de manière hautement sécurisée grâce à un chiffrement de bout en bout.

---

## 🛠️ Architecture du Projet & Clean Code

Pour maintenir de bonnes pratiques de développement, le projet est structuré selon une architecture découplée :

```text
src/
└── com/
    └── vaultjar/
        ├── App.java                 # Point d'entrée de l'application
        ├── model/
        │   └── PasswordEntry.java   # Structure de données d'un compte
        ├── crypto/
        │   ├── CryptoUtils.java     # Moteur de chiffrement AES-256 GCM
        │   └── PasswordGenerator.java # Logique de génération de mots de passe
        └── ui/
            └── PasswordManagerGUI.java # Interface graphique (Swing CardLayout)
```
## 📁 Rangement des fichiers :
model/ : Contient les objets métiers (les données pures).

crypto/ : Regroupe la logique de sécurité (chiffrement, dérivation de clé) et le moteur de génération.

UI/ : Isole la couche présentation (les fenêtres, panneaux et gestionnaires d'événements).

App.java : À la racine, il orchestre simplement le démarrage de l'IHM.

## 🔒 Spécifications de Sécurité
La sécurité de vos données repose sur des standards cryptographiques industriels :

Dérivation de Clé (PBKDF2) : Votre mot de passe maître n'est jamais stocké. Il est étiré via PBKDF2WithHmacSHA256 avec 65 536 itérations et un sel unique pour générer une clé de chiffrement robuste de 256 bits.

Chiffrement AES-256-GCM : Le coffre-fort (vault.enc) est chiffré en mode GCM (Galois/Counter Mode), offrant à la fois la confidentialité et l'authentification des données (protection contre les modifications malveillantes).

Zéro Stockage Persistant du Secret : Le mot de passe maître n'est conservé qu'en mémoire vive durant la session pour assurer le chiffrement à la volée lors des ajouts.

## 🚀 Comment exécuter l'application ?
Prérequis
Java Runtime (JRE) 17 ou supérieur installé sur votre machine (uniquement pour la version .jar).

Lancement via l'Exécutable JAR
Téléchargez le fichier VaultJar.jar depuis l'onglet Releases, puis double-cliquez dessus ou utilisez votre terminal :

```bash
java -jar PassWord-Generator.jar
```
Lancement via l'Exécutable Windows (.EXE).
Pour les utilisateurs Windows sans Java préinstallé, téléchargez l'installateur PassWordGenerator.exe depuis l'onglet Releases et installez-le directement.
