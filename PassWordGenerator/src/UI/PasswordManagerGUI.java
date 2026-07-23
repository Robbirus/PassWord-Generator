package UI;

import crypto.CryptoUtils;
import crypto.PasswordGenerator;
import crypto.RecoveryKeyManager;
import model.AppSettings;
import model.PasswordEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class PasswordManagerGUI extends JFrame {

    private final SecurityAuditPanel auditPanel;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final JTabbedPane tabbedPane;

    private String currentMasterPassword = "";
    private String currentLogin = "";
    private String currentRecoveryKey = "";
    private final String FILE_NAME = System.getProperty("user.home") + File.separator + ".vaultjar" + File.separator + "vault.enc";

    private ArrayList<PasswordEntry> fakeDatabase;
    private final DashboardPanel dashboardPanel;
    private final PasswordGenerator passwordGenerator;

    private ImageIcon vaultIcon     = UIUtils.loadAndScaleIcon("/icons/vault.png", 26, 26);
    private ImageIcon addIcon       = UIUtils.loadAndScaleIcon("/icons/add.png", 26, 26);
    private ImageIcon auditIcon     = UIUtils.loadAndScaleIcon("/icons/shield.png", 26, 26);
    private ImageIcon generatorIcon = UIUtils.loadAndScaleIcon("/icons/generator.png", 26, 26);
    private ImageIcon dataIcon      = UIUtils.loadAndScaleIcon("/icons/data.png", 26, 26);
    private ImageIcon settingsIcon  = UIUtils.loadAndScaleIcon("/icons/settings.png", 26, 26);
    private ImageIcon diceIcon  = UIUtils.loadAndScaleIcon("/icons/dice.png", 26, 26);


    public PasswordManagerGUI() {
        super("Mon Gestionnaire de Mots de Passe");

        // Apply the saved theme to the very first window launch
        applyTheme();

        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupAutoLock();

        Image appIcon = UIUtils.loadAndScaleImage("/icons/app_icon.png", 64, 64);
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        fakeDatabase = new ArrayList<>();
        passwordGenerator = new PasswordGenerator();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Verification of the existence of the safe
        File vaultFile = new File(FILE_NAME);
        boolean vaultExists = vaultFile.exists();

        JPanel authPanel = vaultExists ? createLoginPanel() : createRegisterPanel();

        // Initializing Tab Components
        this.dashboardPanel = new DashboardPanel(this);
        this.auditPanel = new SecurityAuditPanel(this);
        SettingsPanel settingsPanel = new SettingsPanel(this);
        DataManagementPanel dataPanel = new DataManagementPanel(this);
        PasswordGeneratorPanel generatorPanel = new PasswordGeneratorPanel();

        JPanel formPanel = createFormPanel();

        this.tabbedPane = new JTabbedPane();

        // Creating the tab bar
        this.tabbedPane.addTab("Mon Coffre-fort", vaultIcon, dashboardPanel);
        this.tabbedPane.addTab("Ajouter", addIcon, formPanel);
        this.tabbedPane.addTab("Audit Sécurité", auditIcon, auditPanel);
        this.tabbedPane.addTab("Générateur", generatorIcon, generatorPanel);
        this.tabbedPane.addTab("Outils & Data", dataIcon, dataPanel);
        this.tabbedPane.addTab("Options", settingsIcon, settingsPanel);

        this.tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            // If the user clicks on the Audit tab (index 2)
            if (selectedIndex == 2 && auditPanel != null) {
                auditPanel.analyzeVault();
            }
        });

        // Adding main views to CardLayout
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(tabbedPane, "DASHBOARD");

        add(mainPanel);
        cardLayout.show(mainPanel, "AUTH");
    }

    public ArrayList<PasswordEntry> getDatabase() {
        return this.fakeDatabase;
    }

    public void refreshDashboardDisplay() {
        if (dashboardPanel != null) {
            dashboardPanel.loadDataIntoTable(fakeDatabase);
        }
        if (auditPanel != null) {
            auditPanel.analyzeVault();
        }
    }

    public void applyTheme() {
        try {
            if (model.AppSettings.isDarkMode()) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            }
            // Force visual update of the entire window
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Erreur thème : " + ex.getMessage());
        }
    }

    // -- SCREEN 1A: ACCOUNT CREATION (First Launch) ---
    private JPanel createRegisterPanel() {
        JPanel container = new JPanel(new GridBagLayout()); // Automatic center

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TITLE
        JLabel title = new JLabel("Bienvenue ! Configurez votre coffre-fort", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        gbc.gridwidth = 1; // RESET WIDTH AND HEIGHT

        // ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Identifiant / Login :"), gbc);
        JTextField userInput = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(userInput, gbc);

        // PASSWORD
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Mot de passe maître :"), gbc);
        JPasswordField passInput = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(passInput, gbc);

        // Confirmation
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Confirmer le mot de passe :"), gbc);
        JPasswordField confirmInput = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(confirmInput, gbc);

        // Info
        JLabel infoLabel = new JLabel("<html><center><i>Ce mot de passe protégera l'ensemble de vos données.<br>Une clé de secours sera générée à l'étape suivante.</i></center></html>", SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(infoLabel, gbc);

        // CREATE BUTTON
        JButton btnRegister = new JButton("Créer mon coffre-fort");
        btnRegister.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        btnRegister.setPreferredSize(new Dimension(250, 40));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnRegister, gbc);

        btnRegister.addActionListener(e -> {
            String login = userInput.getText().trim();
            String pass = new String(passInput.getPassword());
            String confirm = new String(confirmInput.getPassword());

            if (login.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String key = RecoveryKeyManager.generateKey();
            boolean saved = promptSaveRecoveryKey(login, key);

            if (saved) {
                this.currentLogin = login;
                this.currentMasterPassword = pass;
                this.currentRecoveryKey = key;
                if (this.fakeDatabase == null) {
                    this.fakeDatabase = new ArrayList<>();
                }
                this.fakeDatabase.clear();

                saveVault();
                refreshDashboardDisplay();
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "La sauvegarde de la clé de secours est obligatoire pour continuer.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });

        container.add(formPanel);
        return container;
    }

    // -- SCREEN 1B: LOGIN (Return user) ---
    private JPanel createLoginPanel() {
        JPanel container = new JPanel(new GridBagLayout()); // Automatic center

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TITLE
        JLabel title = new JLabel("Déverrouiller votre coffre-fort", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);

        gbc.gridwidth = 1; // RESET WIDTH AND HEIGHT

        // ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Identifiant / Login :"), gbc);
        JTextField userInput = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(userInput, gbc);

        // PASSWORD
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Mot de passe maître :"), gbc);
        JPasswordField passInput = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(passInput, gbc);

        // LOGIN BUTTON
        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        btnLogin.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnLogin, gbc);

        // Bouton Récupération
        JButton btnRecovery = new JButton("Mot de passe oublié ? Utiliser la clé de secours");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnRecovery, gbc);

        btnLogin.addActionListener(e -> {
            String login = userInput.getText().trim();
            String pass = new String(passInput.getPassword());

            if (login.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (loadVault(login, pass)) {
                this.currentLogin = login;
                this.currentMasterPassword = pass;
                refreshDashboardDisplay();
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Identifiant ou mot de passe maître incorrect !", "Échec de connexion", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRecovery.addActionListener(e -> openRecoveryDialog());

        container.add(formPanel);
        return container;
    }

    // -- DIALOG: Mandatory key download .txt ----
    private boolean promptSaveRecoveryKey(String login, String key) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Votre clé de secours est : " + key + "\n\nVous DEVEZ enregistrer ce fichier texte sur votre ordinateur pour finaliser la création.",
                "Enregistrement de la clé de secours", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (opt == JOptionPane.OK_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("Vault_Recovery_Key_" + login + ".txt"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                return RecoveryKeyManager.saveKeyToFile(fileToSave, login, key);
            }
        }
        return false;
    }

    // --- DIALOG: Connection by Backup Key ---
    private void openRecoveryDialog() {
        JDialog dialog = new JDialog(this, "Récupération par Clé de Secours", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        JLabel lblLogin = new JLabel("Identifiant :");
        lblLogin.setBounds(40, 30, 100, 30);
        dialog.add(lblLogin);

        JTextField txtLogin = new JTextField();
        txtLogin.setBounds(150, 30, 230, 30);
        dialog.add(txtLogin);

        JLabel lblKey = new JLabel("Clé de Secours :");
        lblKey.setBounds(40, 80, 120, 30);
        dialog.add(lblKey);

        JTextField txtKey = new JTextField();
        txtKey.setBounds(150, 80, 230, 30);
        dialog.add(txtKey);

        JButton btnImport = new JButton("Charger un fichier .txt");
        btnImport.setBounds(150, 120, 230, 25);
        btnImport.addActionListener(evt -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(dialog);
            if (option == JFileChooser.APPROVE_OPTION) {
                String extractedKey = RecoveryKeyManager.readKeyFromFile(fileChooser.getSelectedFile());
                if (extractedKey != null) {
                    txtKey.setText(extractedKey);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Impossible de lire la clé depuis ce fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        dialog.add(btnImport);

        JButton btnValidate = new JButton("Réinitialiser le mot de passe");
        btnValidate.setBounds(100, 180, 250, 40);
        btnValidate.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnValidate.addActionListener(evt -> {
            String login = txtLogin.getText().trim();
            String key = txtKey.getText().trim();

            if (login.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (loadVaultWithKey(login, key)) {
                dialog.dispose();
                promptNewPasswordAfterRecovery(login);
            } else {
                JOptionPane.showMessageDialog(dialog, "Identifiant ou clé de secours invalide !", "Échec", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(btnValidate);

        dialog.setVisible(true);
    }

    private void promptNewPasswordAfterRecovery(String login) {
        JPasswordField p1 = new JPasswordField();
        JPasswordField p2 = new JPasswordField();
        Object[] message = {
                "Entrez votre nouveau mot de passe maître :", p1,
                "Confirmez le nouveau mot de passe maître :", p2
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Nouveau mot de passe maître", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newPass = new String(p1.getPassword());
            String confirmPass = new String(p2.getPassword());

            if (!newPass.isEmpty() && newPass.equals(confirmPass)) {
                String newKey = RecoveryKeyManager.generateKey();
                if (promptSaveRecoveryKey(login, newKey)) {
                    this.currentLogin = login;
                    this.currentMasterPassword = newPass;
                    this.currentRecoveryKey = newKey;

                    saveVault();
                    refreshDashboardDisplay();
                    cardLayout.show(mainPanel, "DASHBOARD");
                    JOptionPane.showMessageDialog(this, "Mot de passe maître réinitialisé avec succès !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas ou sont vides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -- SCREEN 3: ADDITION FORM ---
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Form components---
        JTextField siteField = new JTextField(20);
        JTextField userField = new JTextField(20);
        JTextField passField = new JTextField(20);

        String[] categories = {"Général", "Réseaux Sociaux", "Travail", "Banque", "Achats", "Personnel"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);

        JTextField tagsField = new JTextField(20);
        JCheckBox favoriteCheck = new JCheckBox("Marquer comme favori");

        JButton btnGenerate = new JButton("Générer");
        btnGenerate.setIcon(diceIcon);
        JButton btnAdd = new JButton("Ajouter au coffre");
        btnAdd.setIcon(addIcon);
        btnAdd.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        // Ligne Mot de passe + Bouton Générateur
        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.add(passField, BorderLayout.CENTER);
        passPanel.add(btnGenerate, BorderLayout.EAST);

        // --- Disposition GridBagLayout ---
        int row = 0;

        // Site Web
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Site Web / Application :"), gbc);
        gbc.gridx = 1;
        panel.add(siteField, gbc);

        // Identifiant
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Identifiant / Email :"), gbc);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        // Mot de Passe
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Mot de Passe :"), gbc);
        gbc.gridx = 1;
        panel.add(passPanel, gbc);

        // Catégorie
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Catégorie :"), gbc);
        gbc.gridx = 1;
        panel.add(categoryCombo, gbc);

        // Tags
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Tags (séparés par des virgules) :"), gbc);
        gbc.gridx = 1;
        panel.add(tagsField, gbc);

        // Favorites
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(favoriteCheck, gbc);

        // ADD BUTTON
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(btnAdd, gbc);

        // --- Actions ---
        btnGenerate.addActionListener(e -> {
            String generatedPass = this.passwordGenerator.generatePassword(16, true, true, true, true);
            passField.setText(generatedPass);
        });

        btnAdd.addActionListener(e -> {
            String site = siteField.getText().trim();
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String tags = tagsField.getText().trim();
            boolean isFavorite = favoriteCheck.isSelected();

            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Veuillez remplir au moins le site, l'identifiant et le mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String currentDate = java.time.LocalDate.now().toString();
            PasswordEntry newEntry = new PasswordEntry(site, user, pass, currentDate, category, tags, isFavorite);

            fakeDatabase.add(newEntry);
            saveVault();
            refreshDashboardDisplay();

            siteField.setText("");
            userField.setText("");
            passField.setText("");
            tagsField.setText("");
            categoryCombo.setSelectedIndex(0);
            favoriteCheck.setSelected(false);

            JOptionPane.showMessageDialog(panel, "Mot de passe ajouté avec succès !");
        });

        return panel;
    }

    // --- VAULT BACKUP & DECRYPTION ---
    public void saveVault() {
        try {
            File file = new File(FILE_NAME);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            StringBuilder sb = new StringBuilder();
            for (PasswordEntry entry : fakeDatabase) {
                sb.append(entry.getWebsite()).append("|||")
                        .append(entry.getUsername()).append("|||")
                        .append(entry.getPassword()).append("|||")
                        .append(entry.getDateAdded()).append("\n");
            }

            String cipherText = CryptoUtils.encrypt(sb.toString(), currentMasterPassword);

            try (PrintWriter out = new PrintWriter(FILE_NAME)) {
                out.println(currentLogin + "|||" + currentRecoveryKey);
                out.print(cipherText);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean loadVault(String login, String masterPassword) {
        File file = new File(FILE_NAME);
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
                this.fakeDatabase = new ArrayList<>();
                return true;
            }

            String plainText = CryptoUtils.decrypt(cipherText, masterPassword);

            ArrayList<PasswordEntry> loadedData = new ArrayList<>();
            if (!plainText.isEmpty()) {
                String[] lines = plainText.split("\n");
                for (String line : lines) {
                    String[] data = line.split("\\|\\|\\|");
                    if (data.length >= 3) {
                        String date = (data.length >= 4) ? data[3] : "N/A";
                        loadedData.add(new PasswordEntry(data[0], data[1], data[2], date));
                    }
                }
            }

            this.currentRecoveryKey = savedKey;
            if (this.fakeDatabase == null) {
                this.fakeDatabase = new ArrayList<>();
            }
            this.fakeDatabase.clear();
            if (loadedData != null) {
                this.fakeDatabase.addAll(loadedData);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean loadVaultWithKey(String login, String recoveryKey) {
        File file = new File(FILE_NAME);
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

    public ArrayList<PasswordEntry> getFakeDatabase() {
        return this.fakeDatabase;
    }

    private Timer autoLockTimer;

    private void setupAutoLock() {
        // Intercepts all user events in the application
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            resetInactivityTimer();
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

        resetInactivityTimer();
    }

    public void resetInactivityTimer() {
        if (autoLockTimer != null) {
            autoLockTimer.stop();
        }

        int minutes = AppSettings.getAutoLockDelay();
        if (minutes <= 0) return; // If 0, Auto-Lock is disabled

        int delayMs = minutes * 60 * 1000;
        autoLockTimer = new Timer(delayMs, e -> lockVault());
        autoLockTimer.setRepeats(false);
        autoLockTimer.start();
    }

    private void lockVault() {
        if (autoLockTimer != null) autoLockTimer.stop();

        JOptionPane.showMessageDialog(this,
                "Coffre-fort verrouillé automatiquement en raison d'une période d'inactivité.",
                "Auto-Lock", JOptionPane.WARNING_MESSAGE);

        // Close the main window and redisplay the login screen/master password
        this.dispose();
    }
}