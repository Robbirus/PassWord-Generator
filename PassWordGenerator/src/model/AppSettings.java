package model;

import java.io.*;
import java.util.Properties;

public class AppSettings {

    private static final String CONFIG_FILE = "app_config.properties";

    private static boolean showPasswordsByDefault = false;
    private static boolean darkMode = false;
    private static int clipboardClearDelay = 30; // seconds
    private static int autoLockDelay = 5;         // minutes

    static {
        loadSettings();
    }

    public static void loadSettings() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);

            showPasswordsByDefault = Boolean.parseBoolean(props.getProperty("showPasswordsByDefault", "false"));
            darkMode = Boolean.parseBoolean(props.getProperty("darkMode", "false"));
            clipboardClearDelay = Integer.parseInt(props.getProperty("clipboardClearDelay", "30"));
            autoLockDelay = Integer.parseInt(props.getProperty("autoLockDelay", "5"));

        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur lors du chargement des paramètres : " + e.getMessage());
        }
    }

    public static void saveSettings() {
        Properties props = new Properties();
        props.setProperty("showPasswordsByDefault", String.valueOf(showPasswordsByDefault));
        props.setProperty("darkMode", String.valueOf(darkMode));
        props.setProperty("clipboardClearDelay", String.valueOf(clipboardClearDelay));
        props.setProperty("autoLockDelay", String.valueOf(autoLockDelay));

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Configuration du Gestionnaire de Mots de Passe");
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des paramètres : " + e.getMessage());
        }
    }

    public static boolean isShowPasswordsByDefault() {
        return showPasswordsByDefault;
    }

    public static void setShowPasswordsByDefault(boolean show) {
        showPasswordsByDefault = show;
        saveSettings();
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        saveSettings();
    }

    public static int getClipboardClearDelay() {
        return clipboardClearDelay;
    }

    public static void setClipboardClearDelay(int delaySeconds) {
        clipboardClearDelay = delaySeconds;
        saveSettings();
    }

    public static int getAutoLockDelay() {
        return autoLockDelay;
    }

    public static void setAutoLockDelay(int delayMinutes) {
        autoLockDelay = delayMinutes;
        saveSettings();
    }
}