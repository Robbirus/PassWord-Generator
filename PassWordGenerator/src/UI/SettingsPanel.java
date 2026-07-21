package UI;

import model.AppSettings;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private JCheckBox darkModeCb;
    private JCheckBox showPassCb;
    private JComboBox<String> autoLockCombo;
    private JSpinner clipboardSpinner;

    public SettingsPanel(PasswordManagerGUI mainFrame) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Paramètres de l'application", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Hiding Passwords
        showPassCb = new JCheckBox("Afficher les mots de passe en clair par défaut");
        showPassCb.setSelected(AppSettings.isShowPasswordsByDefault());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(showPassCb, gbc);

        // 2. Dark Mode
        darkModeCb = new JCheckBox("Activer le Mode Sombre (FlatLaf)");
        darkModeCb.setSelected(AppSettings.isDarkMode());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(darkModeCb, gbc);

        // 3. Auto-lock
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Verrouillage automatique :"), gbc);

        String[] lockOptions = {"Désactivé", "1 minute", "5 minutes", "15 minutes", "30 minutes"};
        autoLockCombo = new JComboBox<>(lockOptions);
        int lockMin = AppSettings.getAutoLockMinutes();
        if (lockMin == 1) autoLockCombo.setSelectedIndex(1);
        else if (lockMin == 5) autoLockCombo.setSelectedIndex(2);
        else if (lockMin == 15) autoLockCombo.setSelectedIndex(3);
        else if (lockMin == 30) autoLockCombo.setSelectedIndex(4);
        else autoLockCombo.setSelectedIndex(0);

        gbc.gridx = 1;
        formPanel.add(autoLockCombo, gbc);
        row++;

        // 4. Clipboard Dump
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Délai de vidage du presse-papier (sec) :"), gbc);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(AppSettings.getClearClipboardDelay(), 5, 120, 5);
        clipboardSpinner = new JSpinner(spinnerModel);
        gbc.gridx = 1;
        formPanel.add(clipboardSpinner, gbc);

        add(formPanel, BorderLayout.CENTER);

        JButton btnSave = new JButton("💾 Enregistrer les préférences");
        btnSave.setFont(new Font("Dialog", Font.BOLD, 14));
        btnSave.addActionListener(e -> {
            // Backup in API Preferences
            AppSettings.setShowPasswordsByDefault(showPassCb.isSelected());
            AppSettings.setDarkMode(darkModeCb.isSelected());

            int minutes = switch (autoLockCombo.getSelectedIndex()) {
                case 1 -> 1;
                case 2 -> 5;
                case 3 -> 15;
                case 4 -> 30;
                default -> 0;
            };
            AppSettings.setAutoLockMinutes(minutes);
            AppSettings.setClearClipboardDelay((int) clipboardSpinner.getValue());

            // Redesign the global UI with the new theme and reload the table columns
            mainFrame.applyTheme();
            mainFrame.refreshDashboardDisplay();

            JOptionPane.showMessageDialog(this, "Paramètres enregistrés et appliqués !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel southPanel = new JPanel();
        southPanel.add(btnSave);
        add(southPanel, BorderLayout.SOUTH);
    }
}