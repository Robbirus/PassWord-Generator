import os
import shutil
import subprocess
import sys

# ==============================================================================
# CONFIGURATION DU PROJET
# ==============================================================================
# Chemin vers ton fichier JAR d'origine généré par l'IDE
JAR_SOURCE_PATH = os.path.join(
    os.path.expanduser("~"),
    "PassWord-Generator",
    "out",
    "artifacts",
    "PassWord_Generator_jar",
    "PassWord-Generator.jar"
)

# Dossier racine où seront stockées toutes tes releases
BASE_BUILDS_DIR = r"C:\PasswordManager_Builds"

# Informations sur l'application
APP_NAME = "PasswordManager"
MAIN_CLASS = "App"
VENDOR = "Robbirus"


# ==============================================================================
# SCRIPT DE GENERATION DE RELEASE
# ==============================================================================
def create_release(version):
    print(f"🚀 Début de la génération de la release v{version}...")

    # Vérification que le fichier JAR source existe bien
    if not os.path.exists(JAR_SOURCE_PATH):
        print(f"❌ ERREUR : Fichier JAR introuvable dans :\n   {JAR_SOURCE_PATH}")
        print("Veuillez d'abord compiler votre projet dans votre IDE.")
        sys.exit(1)

    # Définition des dossiers de version
    version_dir = os.path.join(BASE_BUILDS_DIR, f"v{version}")
    input_dir = os.path.join(version_dir, "input")
    release_dir = os.path.join(version_dir, "release")

    # Nettoyage / Création des dossiers
    if os.path.exists(input_dir):
        shutil.rmtree(input_dir)
    os.makedirs(input_dir, exist_ok=True)
    os.makedirs(release_dir, exist_ok=True)

    # Nom du JAR temporaire pour jpackage et du JAR final
    temp_jar_name = "PassWord-Generator.jar"
    temp_jar_path = os.path.join(input_dir, temp_jar_name)
    final_jar_path = os.path.join(release_dir, f"{APP_NAME}-{version}.jar")

    # Copie du JAR vers le dossier input et vers le dossier release final
    print("📦 Copie du fichier JAR...")
    shutil.copy2(JAR_SOURCE_PATH, temp_jar_path)
    shutil.copy2(JAR_SOURCE_PATH, final_jar_path)

    # --------------------------------------------------------------------------
    # 1. Génération de l'EXE
    # --------------------------------------------------------------------------
    print("⚙️ Génération de l'exécutable (.exe)...")
    cmd_exe = [
        "jpackage",
        "--type", "exe",
        "--input", input_dir,
        "--dest", release_dir,
        "--name", f"{APP_NAME}",
        "--main-jar", temp_jar_name,
        "--main-class", MAIN_CLASS,
        "--win-shortcut",
        "--win-menu",
        "--app-version", version,
        "--vendor", VENDOR
    ]

    result_exe = subprocess.run(cmd_exe)
    if result_exe.returncode != 0:
        print("⚠️ Attention : La génération de l'EXE a rencontré une erreur.")
    else:
        print("✅ EXE généré avec succès !")

    # --------------------------------------------------------------------------
    # 2. Génération du MSI
    # --------------------------------------------------------------------------
    print("⚙️ Génération de l'installateur (.msi)...")
    cmd_msi = [
        "jpackage",
        "--type", "msi",
        "--input", input_dir,
        "--dest", release_dir,
        "--name", f"{APP_NAME}Installer",
        "--main-jar", temp_jar_name,
        "--main-class", MAIN_CLASS,
        "--win-shortcut",
        "--win-menu",
        "--app-version", version,
        "--vendor", VENDOR
    ]

    result_msi = subprocess.run(cmd_msi)
    if result_msi.returncode != 0:
        print("⚠️ Attention : La génération du MSI a rencontré une erreur.")
    else:
        print("✅ MSI généré avec succès !")

    # Nettoyage du dossier temporaire input
    shutil.rmtree(input_dir, ignore_errors=True)

    print("\n" + "="*60)
    print(f"🎉 RELEASE v{version} TERMINÉE !")
    print(f"📁 Retrouvez vos fichiers (.jar, .exe, .msi) dans :\n   {release_dir}")
    print("="*60)


if __name__ == "__main__":
    # Permet de passer le numéro de version en argument ou de le demander à l'utilisateur
    if len(sys.argv) > 1:
        target_version = sys.argv[1]
    else:
        target_version = input("Entrez le numéro de version (ex: 1.1.0) : ").strip()

    if target_version:
        create_release(target_version)
    else:
        print("❌ Numéro de version invalide.")