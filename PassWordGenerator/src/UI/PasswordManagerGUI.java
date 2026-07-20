package UI;

import crypto.CryptoUtils;
import crypto.PasswordGenerator;
import model.PasswordEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class PasswordManagerGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentMasterPassword = "";
    private final String FILE_NAME = "vault.enc";

    // Test data
    private ArrayList<PasswordEntry> fakeDatabase;

    private JTextArea displayArea;
    private PasswordGenerator passwordGenerator;

    public PasswordManagerGUI() {
        super("Mon Gestionnaire de Mots de Passe");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        fakeDatabase = new ArrayList<>();

        passwordGenerator = new PasswordGenerator();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Creation of the 3 screens
        JPanel loginPanel = createLoginPanel();
        JPanel dashboardPanel = createDashboardPanel();
        JPanel formPanel = createFormPanel();

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(dashboardPanel, "DASHBOARD");
        mainPanel.add(formPanel, "FORM");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private void initFakeData() {
        fakeDatabase = new ArrayList<>();
        fakeDatabase.add(new PasswordEntry("Google", "mon.email@gmail.com", "SuperPassword123!"));
        fakeDatabase.add(new PasswordEntry("GitHub", "DevJava2026", "CodeSecret456#"));
        fakeDatabase.add(new PasswordEntry("Netflix", "famille@stream.fr", "Chill&Popcorn789"));
    }

    // Updates the text of the displayed table
    private void refreshDashboardDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-25s | %-20s\n", "Site", "Identifiant", "Mot de passe"));
        sb.append("------------------------------------------------------------------\n");
        for (PasswordEntry entry : fakeDatabase) {
            sb.append(String.format("%-15s | %-25s | %-20s\n",
                    entry.getWebsite(), entry.getUsername(), entry.getPassword()));
        }
        displayArea.setText(sb.toString());
    }

    // -- SCREEN 1: LOGIN ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel label = new JLabel("Mot de passe maître :", SwingConstants.CENTER);
        label.setFont(new Font("Dialog", Font.BOLD, 18));
        label.setBounds(150, 130, 300, 30);
        panel.add(label);

        JPasswordField masterPasswordField = new JPasswordField();
        masterPasswordField.setBounds(150, 170, 250, 40);
        masterPasswordField.setFont(new Font("Dialog", Font.PLAIN, 18));
        panel.add(masterPasswordField);

        char defaultEchoChar = masterPasswordField.getEchoChar();

        JButton toggleVisibilityButton = new JButton("S");
        toggleVisibilityButton.setBounds(405, 170, 45, 40);
        toggleVisibilityButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        toggleVisibilityButton.setFocusPainted(false);

        toggleVisibilityButton.addActionListener(new ActionListener() {
            private boolean isPasswordVisible = false;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPasswordVisible) {
                    masterPasswordField.setEchoChar(defaultEchoChar);
                    toggleVisibilityButton.setText("S");
                    isPasswordVisible = false;
                } else {
                    masterPasswordField.setEchoChar((char) 0);
                    toggleVisibilityButton.setText("N");
                    isPasswordVisible = true;
                }
                masterPasswordField.repaint();
            }
        });
        panel.add(toggleVisibilityButton);

        JButton loginButton = new JButton("Déverrouiller");
        loginButton.setBounds(200, 250, 200, 40);
        loginButton.setFont(new Font("Dialog", Font.BOLD, 16));

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = new String(masterPasswordField.getPassword());
                if (input.isEmpty()) return;

                // We are trying to load and decrypt the vault with the password entered.
                if (loadVault(input)) {
                    currentMasterPassword = input; // We keep the password in memory for future backups.
                    refreshDashboardDisplay();     // We refresh the table with the real data read
                    cardLayout.show(mainPanel, "DASHBOARD");
                } else {
                    JOptionPane.showMessageDialog(PasswordManagerGUI.this,
                            "Mot de passe maître incorrect ou fichier corrompu !", "Échec du déchiffrement", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(loginButton);

        return panel;
    }

    // -- SCREEN 2: THE SAFE ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Mon Coffre-fort", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        titleLabel.setBounds(0, 20, 600, 40);
        panel.add(titleLabel);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // First display of false data
        refreshDashboardDisplay();

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBounds(30, 80, 520, 250);
        panel.add(scrollPane);

        // Button to go to the addition form
        JButton goToAddButton = new JButton("Ajouter un nouveau mot de passe");
        goToAddButton.setBounds(150, 360, 300, 40);
        goToAddButton.setFont(new Font("Dialog", Font.BOLD, 14));
        goToAddButton.addActionListener(e -> cardLayout.show(mainPanel, "FORM"));
        panel.add(goToAddButton);

        return panel;
    }

    // -- SCREEN 3: ADDITION FORM ---
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Ajouter une entrée", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setBounds(0, 20, 600, 30);
        panel.add(titleLabel);

        // Website field
        JLabel siteLabel = new JLabel("Site Web :");
        siteLabel.setBounds(50, 80, 100, 30);
        panel.add(siteLabel);
        JTextField siteInput = new JTextField();
        siteInput.setBounds(160, 80, 350, 30);
        panel.add(siteInput);

        // ID field
        JLabel userInput = new JLabel("Identifiant / Email :");
        userInput.setBounds(50, 130, 120, 30);
        panel.add(userInput);
        JTextField usernameInput = new JTextField();
        usernameInput.setBounds(160, 130, 350, 30);
        panel.add(usernameInput);

        // Password field
        JLabel passLabel = new JLabel("Mot de passe :");
        passLabel.setBounds(50, 180, 100, 30);
        panel.add(passLabel);
        JTextField passwordInput = new JTextField();
        passwordInput.setBounds(160, 180, 230, 30);
        panel.add(passwordInput);

        // Button to open the old password generator in a pop-up
        JButton generatePopupCtx = new JButton("Générer");
        generatePopupCtx.setBounds(400, 180, 110, 30);
        generatePopupCtx.addActionListener(e -> {
            // We open a JDialog (blocking secondary window)
            JDialog genDialog = new JDialog(PasswordManagerGUI.this, "Générateur", true);
            genDialog.setSize(400, 450);
            genDialog.setLayout(null);
            genDialog.setLocationRelativeTo(PasswordManagerGUI.this);

            // Simplified and compact version of the old graphical interface
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

            // Button to validate and inject the password into the main form
            JButton btnAccept = new JButton("Utiliser");
            btnAccept.setBounds(210, 320, 140, 35);
            btnAccept.addActionListener(evt -> {
                if (!resOutput.getText().isEmpty()) {
                    passwordInput.setText(resOutput.getText());
                    genDialog.dispose(); // Close the pop up
                }
            });
            genDialog.add(btnAccept);

            genDialog.setVisible(true);
        });
        panel.add(generatePopupCtx);

        // Save button
        JButton saveButton = new JButton("Enregistrer");
        saveButton.setBounds(160, 260, 150, 40);
        saveButton.setFont(new Font("Dialog", Font.BOLD, 14));
        saveButton.addActionListener(e -> {
            String site = siteInput.getText().trim();
            String user = usernameInput.getText().trim();
            String pass = passwordInput.getText().trim();

            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Adding to the test database
            fakeDatabase.add(new PasswordEntry(site, user, pass));
            saveVault();
            refreshDashboardDisplay();

            // Nettoyage du formulaire
            siteInput.setText("");
            usernameInput.setText("");
            passwordInput.setText("");

            // Retour au coffre-fort
            cardLayout.show(mainPanel, "DASHBOARD");
        });
        panel.add(saveButton);

        // Cancel button
        JButton cancelButton = new JButton("Annuler");
        cancelButton.setBounds(340, 260, 150, 40);
        cancelButton.addActionListener(e -> cardLayout.show(mainPanel, "DASHBOARD"));
        panel.add(cancelButton);

        return panel;
    }

    private void saveVault() {
        try {
            StringBuilder sb = new StringBuilder();
            // Separate each field with a special character (e.g., "|||") and each account with a line
            for (PasswordEntry entry : fakeDatabase) {
                sb.append(entry.getWebsite()).append("|||")
                        .append(entry.getUsername()).append("|||")
                        .append(entry.getPassword()).append("\n");
            }

            // If the list is empty, we store an empty string
            String plainText = sb.toString();

            // AES-256 encryption with current master password
            String cipherText = CryptoUtils.encrypt(plainText, currentMasterPassword);

            // Writing to the local vault.enc file
            try (PrintWriter out = new PrintWriter(FILE_NAME)) {
                out.print(cipherText);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Read and decrypt the file at startup
    private boolean loadVault(String masterPassword) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            // If the file does not yet exist (first use), we initialize a blank list
            fakeDatabase = new ArrayList<>();
            return true;
        }

        try {
            // Read the file’s encrypted (Base64) content
            String cipherText = new Scanner(file).useDelimiter("\\Z").next();

            // Try to decrypt with the provided password
            String plainText = CryptoUtils.decrypt(cipherText, masterPassword);

            fakeDatabase = new ArrayList<>();
            if (!plainText.trim().isEmpty()) {
                String[] lines = plainText.split("\n");
                for (String line : lines) {
                    String[] parts = line.split("\\|\\|\\|");
                    if (parts.length == 3) {
                        fakeDatabase.add(new PasswordEntry(parts[0], parts[1], parts[2]));
                    }
                }
            }
            return true; // Decryption successful
        } catch (Exception e) {
            // If the password is false or the file is corrupted, an exception is raised
            return false;
        }
    }
}