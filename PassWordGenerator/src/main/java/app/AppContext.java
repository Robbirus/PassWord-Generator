package app;

import controllers.AuditController;
import controllers.BaseController;
import controllers.DashboardController;
import controllers.MainViewController;
import crypto.CryptoUtils;
import crypto.RecoveryKeyManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AppSettings;
import model.PasswordEntry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

/**
 * Application context shared among all JavaFX controllers.
 * Replaces the old PasswordManagerGUI (JFrame) class: carries the state of
 * session (login, master password, base in memory), the logic of
 * encryption/decryption of the vault, navigation between screens,
 * the theme and automatic locking.
 */
public class AppContext {

    private final Stage primaryStage;
    private Scene scene;

    private ArrayList<PasswordEntry> database = new ArrayList<>();
    private String currentLogin = "";
    private String currentMasterPassword = "";
    private String currentRecoveryKey = "";

    private final String vaultFileName =
            System.getProperty("user.home") + File.separator + ".vaultjar" + File.separator + "vault.enc";

    private DashboardController dashboardController;
    private AuditController auditController;

    private PauseTransition autoLockTimer;

    public AppContext(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // --- Initialisation ---

    public void attachScene(Scene scene) {
        this.scene = scene;
        applyTheme();
        setupAutoLock();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    // --- Session Data Access ---

    public ArrayList<PasswordEntry> getDatabase() {
        return database;
    }

    public String getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(String login) {
        this.currentLogin = login;
    }

    public String getCurrentMasterPassword() {
        return currentMasterPassword;
    }

    public void setCurrentMasterPassword(String password) {
        this.currentMasterPassword = password;
    }

    public String getCurrentRecoveryKey() {
        return currentRecoveryKey;
    }

    public void setCurrentRecoveryKey(String key) {
        this.currentRecoveryKey = key;
    }

    public boolean vaultExists() {
        return new File(vaultFileName).exists();
    }

    // --- Screen Navigation ---

    public void showAuthView() {
        try {
            String fxml = vaultExists() ? "/fxml/Login.fxml" : "/fxml/Register.fxml";
            Parent root = loadAndInject(fxml);
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            MainViewController controller = loader.getController();
            controller.setAppContext(this); // builds the internal tabs
            scene.setRoot(root);
            refreshDashboard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads an FXML and automatically injects this context if the controller
     * implements BaseController.
     */
    public Parent loadAndInject(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof BaseController bc) {
            bc.setAppContext(this);
        }
        return root;
    }

    // --- Refreshing Views ---

    public void registerDashboard(DashboardController c) {
        this.dashboardController = c;
    }

    public void registerAudit(AuditController c) {
        this.auditController = c;
    }

    public void refreshDashboard() {
        if (dashboardController != null) {
            dashboardController.loadDataIntoTable(database);
        }
        if (auditController != null) {
            auditController.analyzeVault();
        }
    }

    // --- Backup / loading of the encrypted vault ---

    public void saveVault() {
        try {
            File file = new File(vaultFileName);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            StringBuilder sb = new StringBuilder();
            for (PasswordEntry entry : database) {
                sb.append(sanitize(entry.getWebsite())).append("|||")
                        .append(sanitize(entry.getUsername())).append("|||")
                        .append(sanitize(entry.getPassword())).append("|||")
                        .append(sanitize(entry.getDateAdded())).append("|||")
                        .append(sanitize(entry.getCategory())).append("|||")
                        .append(sanitize(entry.getTags())).append("|||")
                        .append(entry.isFavorite()).append("\n");
            }

            String cipherText = CryptoUtils.encrypt(sb.toString(), currentMasterPassword);

            try (PrintWriter out = new PrintWriter(vaultFileName)) {
                out.println(currentLogin + "|||" + currentRecoveryKey);
                out.print(cipherText);
            }
        } catch (Exception e) {
            showError("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Removes characters that would corrupt the "|||"-delimited storage format
     * (the field separator itself, and newlines which would create a bogus extra row).
     */
    private static String sanitize(String value) {
        if (value == null) return "";
        return value.replace("|||", "").replace("\n", " ").replace("\r", "");
    }

    public boolean loadVault(String login, String masterPassword) {
        File file = new File(vaultFileName);
        if (!file.exists()) return false;

        try (Scanner scanner = new Scanner(file)) {
            if (!scanner.hasNextLine()) return false;

            String headerLine = scanner.nextLine();
            String[] parts = headerLine.split("\\|\\|\\|");
            if (parts.length < 2) return false;

            String savedLogin = parts[0].trim();
            String savedKey = parts[1].trim();

            if (!savedLogin.equalsIgnoreCase(login.trim())) {
                return false;
            }

            StringBuilder cipherTextBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                cipherTextBuilder.append(scanner.nextLine()).append("\n");
            }

            String cipherText = cipherTextBuilder.toString().trim();
            if (cipherText.isEmpty()) {
                this.currentRecoveryKey = savedKey;
                this.database = new ArrayList<>();
                return true;
            }

            String plainText = CryptoUtils.decrypt(cipherText, masterPassword);

            ArrayList<PasswordEntry> loadedData = new ArrayList<>();
            if (!plainText.isEmpty()) {
                String[] lines = plainText.split("\n");
                for (String line : lines) {
                    String[] data = line.split("\\|\\|\\|", -1);
                    if (data.length >= 3) {
                        String date = (data.length >= 4) ? data[3] : "N/A";
                        String category = (data.length >= 5) ? data[4] : "Général";
                        String tags = (data.length >= 6) ? data[5] : "";
                        boolean favorite = (data.length >= 7) && Boolean.parseBoolean(data[6]);
                        loadedData.add(new PasswordEntry(data[0], data[1], data[2], date, category, tags, favorite));
                    }
                }
            }

            this.currentRecoveryKey = savedKey;
            this.database.clear();
            this.database.addAll(loadedData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean loadVaultWithKey(String login, String recoveryKey) {
        File file = new File(vaultFileName);
        if (!file.exists()) return false;

        try (Scanner scanner = new Scanner(file)) {
            if (!scanner.hasNextLine()) return false;

            String headerLine = scanner.nextLine();
            String[] parts = headerLine.split("\\|\\|\\|");

            if (parts.length >= 2) {
                String savedLogin = parts[0].trim();
                String savedKey = parts[1].trim();
                return savedLogin.equalsIgnoreCase(login.trim()) && savedKey.equals(recoveryKey.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean promptSaveRecoveryKey(String login, String key) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Enregistrement de la clé de secours");
        alert.setHeaderText(null);
        alert.setContentText("Votre clé de secours est : " + key +
                "\n\nVous DEVEZ enregistrer ce fichier texte sur votre ordinateur pour finaliser la création.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("Vault_Recovery_Key_" + login + ".txt");
            File fileToSave = fileChooser.showSaveDialog(primaryStage);
            if (fileToSave != null) {
                return RecoveryKeyManager.saveKeyToFile(fileToSave, login, key);
            }
        }
        return false;
    }

    // --- Theme ---

    public void applyTheme() {
        if (scene == null) return;
        String css = AppSettings.isDarkMode() ? "/css/dark.css" : "/css/light.css";
        scene.getStylesheets().setAll(getClass().getResource(css).toExternalForm());
    }

    // --- Automatic Lockout by Inactivity ---

    private void setupAutoLock() {
        scene.addEventFilter(MouseEvent.ANY, e -> resetInactivityTimer());
        scene.addEventFilter(KeyEvent.ANY, e -> resetInactivityTimer());
        resetInactivityTimer();
    }

    public void resetInactivityTimer() {
        if (autoLockTimer != null) {
            autoLockTimer.stop();
        }

        int minutes = AppSettings.getAutoLockDelay();
        if (minutes <= 0) return; // 0 = Auto-Lock disabled

        autoLockTimer = new PauseTransition(Duration.minutes(minutes));
        autoLockTimer.setOnFinished(e -> lockVault());
        autoLockTimer.play();
    }

    private void lockVault() {
        if (autoLockTimer != null) autoLockTimer.stop();

        showInfo("Auto-Lock", "Coffre-fort verrouillé automatiquement en raison d'une période d'inactivité.");

        // Resets the session and redisplays the login screen
        currentLogin = "";
        currentMasterPassword = "";
        currentRecoveryKey = "";
        database.clear();
        dashboardController = null;
        auditController = null;

        showAuthView();
    }

    // --- Dialog helpers ---

    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(primaryStage);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}