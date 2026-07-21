package UI;

import crypto.CryptoUtils;
import crypto.PasswordGenerator;
import crypto.RecoveryKeyManager;
import model.PasswordEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class PasswordManagerGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    private String currentMasterPassword = "";
    private String currentLogin = "";
    private String currentRecoveryKey = "";
    private final String FILE_NAME = System.getProperty("user.home") + File.separator + ".vaultjar" + File.separator + "vault.enc";

    private ArrayList<PasswordEntry> fakeDatabase;
    private DashboardPanel dashboardPanel;
    private PasswordGenerator passwordGenerator;

    public PasswordManagerGUI() {
        super("Mon Gestionnaire de Mots de Passe");
        setSize(650, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
        SettingsPanel settingsPanel = new SettingsPanel(this.dashboardPanel);
        JPanel formPanel = createFormPanel();

        // Creating the tab bar
        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.addTab("🔑 Mon Coffre-fort", dashboardPanel);
        this.tabbedPane.addTab("➕ Ajouter", formPanel);
        this.tabbedPane.addTab("⚙️ Options", settingsPanel);

        // Adding main views to CardLayout
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(tabbedPane, "DASHBOARD");

        add(mainPanel);
        cardLayout.show(mainPanel, "AUTH");
    }

    public void showFormScreen() {
        // 1. Switches to the main view
        cardLayout.show(mainPanel, "DASHBOARD");
        // 2. Select tab 1 ("➕ Add")
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    public void refreshDashboardDisplay() {
        if (dashboardPanel != null) {
            dashboardPanel.loadDataIntoTable(fakeDatabase);
        }
    }

    // -- SCREEN 1A: ACCOUNT CREATION (First Launch) ---
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(null);

        JLabel title = new JLabel("Bienvenue ! Configurez votre coffre-fort", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        title.setBounds(0, 30, 650, 35);
        panel.add(title);

        JLabel userLabel = new JLabel("Identifiant / Login :");
        userLabel.setBounds(120, 100, 180, 30);
        panel.add(userLabel);
        JTextField userInput = new JTextField();
        userInput.setBounds(300, 100, 220, 30);
        panel.add(userInput);

        JLabel passLabel = new JLabel("Mot de passe maître :");
        passLabel.setBounds(120, 150, 180, 30);
        panel.add(passLabel);
        JPasswordField passInput = new JPasswordField();
        passInput.setBounds(300, 150, 220, 30);
        panel.add(passInput);

        JLabel confirmLabel = new JLabel("Confirmer le mot de passe :");
        confirmLabel.setBounds(120, 200, 180, 30);
        panel.add(confirmLabel);
        JPasswordField confirmInput = new JPasswordField();
        confirmInput.setBounds(300, 200, 220, 30);
        panel.add(confirmInput);

        JLabel infoLabel = new JLabel("<html><center><i>Ce mot de passe protégera l'ensemble de vos données.<br>Une clé de secours sera générée à l'étape suivante.</i></center></html>", SwingConstants.CENTER);
        infoLabel.setBounds(50, 250, 550, 40);
        panel.add(infoLabel);

        JButton btnRegister = new JButton("Créer mon coffre-fort");
        btnRegister.setFont(new Font("Dialog", Font.BOLD, 15));
        btnRegister.setBounds(200, 310, 250, 45);
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
                this.fakeDatabase = new ArrayList<>();

                saveVault();
                refreshDashboardDisplay();
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "La sauvegarde de la clé de secours est obligatoire pour continuer.", "Attention", JOptionPane.WARNING_MESSAGE);
            }
        });
        panel.add(btnRegister);

        return panel;
    }

    // -- SCREEN 1B: LOGIN (Return user) ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(null);

        JLabel title = new JLabel("Déverrouiller votre coffre-fort", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 22));
        title.setBounds(0, 40, 650, 35);
        panel.add(title);

        JLabel userLabel = new JLabel("Identifiant / Login :");
        userLabel.setBounds(150, 120, 150, 30);
        panel.add(userLabel);
        JTextField userInput = new JTextField();
        userInput.setBounds(300, 120, 200, 30);
        panel.add(userInput);

        JLabel passLabel = new JLabel("Mot de passe maître :");
        passLabel.setBounds(150, 170, 150, 30);
        panel.add(passLabel);
        JPasswordField passInput = new JPasswordField();
        passInput.setBounds(300, 170, 200, 30);
        panel.add(passInput);

        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setFont(new Font("Dialog", Font.BOLD, 15));
        btnLogin.setBounds(225, 230, 200, 40);
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
        panel.add(btnLogin);

        JButton btnRecovery = new JButton("Mot de passe oublié ? Utiliser la clé de secours");
        btnRecovery.setBounds(150, 310, 350, 30);
        btnRecovery.addActionListener(e -> openRecoveryDialog());
        panel.add(btnRecovery);

        return panel;
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
        btnValidate.setFont(new Font("Dialog", Font.BOLD, 13));
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
        JPanel panel = new JPanel(null);

        JLabel titleLabel = new JLabel("Ajouter une entrée", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setBounds(0, 20, 650, 30);
        panel.add(titleLabel);

        JLabel siteLabel = new JLabel("Site Web :");
        siteLabel.setBounds(50, 80, 100, 30);
        panel.add(siteLabel);
        JTextField siteInput = new JTextField();
        siteInput.setBounds(160, 80, 380, 30);
        panel.add(siteInput);

        JLabel userInput = new JLabel("Identifiant / Email :");
        userInput.setBounds(50, 130, 120, 30);
        panel.add(userInput);
        JTextField usernameInput = new JTextField();
        usernameInput.setBounds(160, 130, 380, 30);
        panel.add(usernameInput);

        JLabel passLabel = new JLabel("Mot de passe :");
        passLabel.setBounds(50, 180, 100, 30);
        panel.add(passLabel);
        JTextField passwordInput = new JTextField();
        passwordInput.setBounds(160, 180, 250, 30);
        panel.add(passwordInput);

        JButton generatePopupCtx = new JButton("Générer");
        generatePopupCtx.setBounds(420, 180, 120, 30);
        generatePopupCtx.addActionListener(e -> {
            JDialog genDialog = new JDialog(this, "Générateur", true);
            genDialog.setSize(400, 450);
            genDialog.setLayout(null);
            genDialog.setLocationRelativeTo(this);

            JLabel lenLabel = new JLabel("Longueur :");
            lenLabel.setBounds(30, 30, 100, 30);
            genDialog.add(lenLabel);

            JTextField lenInput = new JTextField("12");
            lenInput.setBounds(150, 30, 50, 30);
            genDialog.add(lenInput);

            JCheckBox upperCb = new JCheckBox("Majuscules", true);
            upperCb.setBounds(30, 80, 150, 30);
            genDialog.add(upperCb);

            JCheckBox lowerCb = new JCheckBox("Minuscules", true);
            lowerCb.setBounds(30, 120, 150, 30);
            genDialog.add(lowerCb);

            JCheckBox numCb = new JCheckBox("Chiffres", true);
            numCb.setBounds(30, 160, 150, 30);
            genDialog.add(numCb);

            JCheckBox symCb = new JCheckBox("Symboles", false);
            symCb.setBounds(30, 200, 150, 30);
            genDialog.add(symCb);

            JTextField resOutput = new JTextField();
            resOutput.setEditable(false);
            resOutput.setBounds(30, 260, 320, 35);
            genDialog.add(resOutput);

            JButton btnGen = new JButton("Générer");
            btnGen.setBounds(30, 320, 140, 35);
            btnGen.addActionListener(evt -> {
                try {
                    int length = Integer.parseInt(lenInput.getText());
                    String pass = passwordGenerator.generatePassword(length,
                            upperCb.isSelected(), lowerCb.isSelected(),
                            numCb.isSelected(), symCb.isSelected());
                    resOutput.setText(pass);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(genDialog, "Longueur invalide.");
                }
            });
            genDialog.add(btnGen);

            JButton btnAccept = new JButton("Utiliser");
            btnAccept.setBounds(210, 320, 140, 35);
            btnAccept.addActionListener(evt -> {
                if (!resOutput.getText().isEmpty()) {
                    passwordInput.setText(resOutput.getText());
                    genDialog.dispose();
                }
            });
            genDialog.add(btnAccept);

            genDialog.setVisible(true);
        });
        panel.add(generatePopupCtx);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.setBounds(180, 260, 140, 40);
        saveButton.setFont(new Font("Dialog", Font.BOLD, 14));
        saveButton.addActionListener(e -> {
            String site = siteInput.getText().trim();
            String user = usernameInput.getText().trim();
            String pass = passwordInput.getText().trim();

            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            fakeDatabase.add(new PasswordEntry(site, user, pass));
            saveVault();
            refreshDashboardDisplay();

            siteInput.setText("");
            usernameInput.setText("");
            passwordInput.setText("");

            // Navigation vers le 1er onglet (Coffre-fort)
            if (tabbedPane != null) {
                tabbedPane.setSelectedIndex(0);
            }
        });
        panel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.setBounds(340, 260, 140, 40);
        cancelButton.addActionListener(e -> {
            siteInput.setText("");
            usernameInput.setText("");
            passwordInput.setText("");

            // Navigation vers le 1er onglet (Coffre-fort)
            if (tabbedPane != null) {
                tabbedPane.setSelectedIndex(0);
            }
        });
        panel.add(cancelButton);

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
                        .append(entry.getPassword()).append("\n");
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
                    if (data.length == 3) {
                        loadedData.add(new PasswordEntry(data[0], data[1], data[2]));
                    }
                }
            }

            this.currentRecoveryKey = savedKey;
            this.fakeDatabase = loadedData;
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
}