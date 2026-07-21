package model;

import java.util.prefs.Preferences;

public class AppSettings {

    private static final Preferences prefs = Preferences.userNodeForPackage(AppSettings.class);

    // --- CLÉS DE CONFIGURATION ---
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes";
    private static final String KEY_SHOW_PASSWORDS_DEFAULT = "show_passwords_default";
    private static final String KEY_CLEAR_CLIPBOARD_DELAY = "clear_clipboard_delay";

    // --- GETTERS & SETTERS (AVEC VALEURS PAR DÉFAUT) ---

    // 1. Mode Sombre / Thème
    public static boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
    public static void setDarkMode(boolean enabled) {
        prefs.putBoolean(KEY_DARK_MODE, enabled);
    }

    // 2. Verrouillage automatique (en minutes, 0 = désactivé)
    public static int getAutoLockMinutes() {
        return prefs.getInt(KEY_AUTO_LOCK_MINUTES, 5); // 5 min par défaut
    }
    public static void setAutoLockMinutes(int minutes) {
        prefs.putInt(KEY_AUTO_LOCK_MINUTES, minutes);
    }

    // 3. Masquer/Afficher les mots de passe par défaut dans le tableau
    public static boolean isShowPasswordsByDefault() {
        return prefs.getBoolean(KEY_SHOW_PASSWORDS_DEFAULT, false);
    }
    public static void setShowPasswordsByDefault(boolean show) {
        prefs.putBoolean(KEY_SHOW_PASSWORDS_DEFAULT, show);
    }

    // 4. Nettoyage du presse-papier après copie (en secondes)
    public static int getClearClipboardDelay() {
        return prefs.getInt(KEY_CLEAR_CLIPBOARD_DELAY, 30); // 30 sec par défaut
    }
    public static void setClearClipboardDelay(int seconds) {
        prefs.putInt(KEY_CLEAR_CLIPBOARD_DELAY, seconds);
    }
}